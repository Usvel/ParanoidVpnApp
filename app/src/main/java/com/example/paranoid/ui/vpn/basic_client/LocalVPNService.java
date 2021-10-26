package com.example.paranoid.ui.vpn.basic_client;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.paranoid.ui.vpn.VPNFragment;
//import com.example.paranoid.ui.vpn.VPNFragmentKt;
import com.example.paranoid.ui.vpn.basic_client.bio.BioTcpHandler;
import com.example.paranoid.ui.vpn.basic_client.bio.BioUdpHandler;
import com.example.paranoid.ui.vpn.basic_client.bio.NioSingleThreadTcpHandler;
import com.example.paranoid.ui.vpn.basic_client.config.Config;
import com.example.paranoid.ui.vpn.basic_client.protocol.tcpip.Packet;
import com.example.paranoid.ui.vpn.basic_client.util.ByteBufferPool;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalVPNService extends VpnService {
    private static final String TAG = LocalVPNService.class.getSimpleName();
    private static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything

    private ParcelFileDescriptor vpnInterface = null;

    private PendingIntent pendingIntent;

    private BlockingQueue<Packet> deviceToNetworkUDPQueue;
    private BlockingQueue<Packet> deviceToNetworkTCPQueue;
    private BlockingQueue<ByteBuffer> networkToDeviceQueue;
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        setupVPN();
        deviceToNetworkUDPQueue = new ArrayBlockingQueue<Packet>(1000);
        deviceToNetworkTCPQueue = new ArrayBlockingQueue<Packet>(1000);
        networkToDeviceQueue = new ArrayBlockingQueue<>(1000);

        executorService = Executors.newFixedThreadPool(20);
        executorService.submit(new BioUdpHandler(deviceToNetworkUDPQueue, networkToDeviceQueue, this));
        //executorService.submit(new BioTcpHandler(deviceToNetworkTCPQueue, networkToDeviceQueue, this));
        executorService.submit(new NioSingleThreadTcpHandler(deviceToNetworkTCPQueue, networkToDeviceQueue, this));

        executorService.submit(new VPNRunnable(vpnInterface.getFileDescriptor(),
                deviceToNetworkUDPQueue, deviceToNetworkTCPQueue, networkToDeviceQueue));
    }

    private void setupVPN() {
        try {
            if (vpnInterface == null) {
                Builder builder = new Builder();
                builder.addAddress(VPN_ADDRESS, 32);
                builder.addRoute(VPN_ROUTE, 0);
                builder.addDnsServer(Config.dns);
                if (Config.testLocal) {
                    builder.addAllowedApplication("com.example.paranoid");
                }
                vpnInterface = builder.setSession("com.example.paranoid").setConfigureIntent(pendingIntent).establish();
            }
        } catch (Exception e) {
            Log.e(TAG, "error", e);
            System.exit(0);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Intent intent1 = new Intent(this, VPNFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);

        Notification notification = new NotificationCompat.Builder(this, "Channel_id1")
                .setContentTitle("Example")
                .setContentText("App is running")
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);


        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "Channel_id1", "Foreground service", NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
        cleanup();
        Log.i(TAG, "Stopped");
    }

    private void cleanup() {
        deviceToNetworkTCPQueue = null;
        deviceToNetworkUDPQueue = null;
        networkToDeviceQueue = null;
        closeResources(vpnInterface);
    }

    // TODO: Move this to a "utils" class for reuse
    private static void closeResources(Closeable... resources) {
        for (Closeable resource : resources) {
            try {
                resource.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private static class VPNRunnable implements Runnable {
        private static final String TAG = "paranoid";

        private FileDescriptor vpnFileDescriptor;

        private BlockingQueue<Packet> deviceToNetworkUDPQueue;
        private BlockingQueue<Packet> deviceToNetworkTCPQueue;
        private BlockingQueue<ByteBuffer> networkToDeviceQueue;

        public VPNRunnable(FileDescriptor vpnFileDescriptor,
                           BlockingQueue<Packet> deviceToNetworkUDPQueue,
                           BlockingQueue<Packet> deviceToNetworkTCPQueue,
                           BlockingQueue<ByteBuffer> networkToDeviceQueue) {
            this.vpnFileDescriptor = vpnFileDescriptor;
            this.deviceToNetworkUDPQueue = deviceToNetworkUDPQueue;
            this.deviceToNetworkTCPQueue = deviceToNetworkTCPQueue;
            this.networkToDeviceQueue = networkToDeviceQueue;
        }


        static class WriteVpnThread implements Runnable {
            FileChannel vpnOutput;
            private BlockingQueue<ByteBuffer> networkToDeviceQueue;

            WriteVpnThread(FileChannel vpnOutput, BlockingQueue<ByteBuffer> networkToDeviceQueue) {
                this.vpnOutput = vpnOutput;
                this.networkToDeviceQueue = networkToDeviceQueue;
            }

            @Override
            public void run() {
                while (true) {
                    try {
                        ByteBuffer bufferFromNetwork = networkToDeviceQueue.take();
                        bufferFromNetwork.flip();
                        while (bufferFromNetwork.hasRemaining()) {
                            int w = vpnOutput.write(bufferFromNetwork);
                            if (w > 0) {
                                VPNFragment.getDownByte().addAndGet(w);
                            }
                            if (Config.logRW) {
                                Log.d(TAG, "vpn write " + w);
                            }
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "WriteVpnThread fail", e);
                    }
                }

            }
        }

        @Override
        public void run() {
            Log.i(TAG, "Started");
            FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
            FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();
            Thread t = new Thread(new WriteVpnThread(vpnOutput, networkToDeviceQueue));
            t.start();
            try {
                ByteBuffer bufferToNetwork = null;
                while (!Thread.interrupted()) {
                    bufferToNetwork = ByteBufferPool.acquire();
                    int readBytes = vpnInput.read(bufferToNetwork);

                    VPNFragment.getUpByte().addAndGet(readBytes);

                    if (readBytes > 0) {
                        bufferToNetwork.flip();

                        Packet packet = new Packet(bufferToNetwork);
                        if (packet.isUDP()) {
                            if (Config.logRW) {
                                Log.i(TAG, "read udp" + readBytes);
                            }
                            deviceToNetworkUDPQueue.offer(packet);
                        } else if (packet.isTCP()) {
                            if (Config.logRW) {
                                Log.i(TAG, "read tcp " + readBytes);
                            }
                            deviceToNetworkTCPQueue.offer(packet);
                        } else {
                            Log.w(TAG, String.format("Unknown packet protocol type %d", packet.ip4Header.protocolNum));
                        }
                    } else {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                Log.w(TAG, e.toString(), e);
            } finally {
                closeResources(vpnInput, vpnOutput);
            }
        }
    }

    private BroadcastReceiver stopBr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("stop_kill".equals(intent.getAction())) {
                onDestroy();
            }
        }
    };
}

