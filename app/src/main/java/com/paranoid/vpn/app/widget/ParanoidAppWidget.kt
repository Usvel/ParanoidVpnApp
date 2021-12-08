package com.paranoid.vpn.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.paranoid.vpn.app.R
import com.paranoid.vpn.app.common.ui.app.AppActivity


/**
 * Implementation of App Widget functionality.
 */
class ParanoidAppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            val intentQr = Intent(context, AppActivity::class.java)
            intentQr.putExtra("toQr", true)
            intentQr.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntentQr = PendingIntent.getActivity(
                context,
                0,
                intentQr,
                PendingIntent.FLAG_IMMUTABLE
            )
            val viewsQr = RemoteViews(context.packageName, R.layout.new_app_widget)
            viewsQr.setOnClickPendingIntent(R.id.to_qr, pendingIntentQr)
            appWidgetManager.updateAppWidget(appWidgetId, viewsQr)

            val intentTurn = Intent(context, AppActivity::class.java)
            intentTurn.putExtra("toTurn", true)
            intentTurn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntentTurn = PendingIntent.getActivity(
                context,
                1,
                intentTurn,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val viewsTurn = RemoteViews(context.packageName, R.layout.new_app_widget)
            viewsTurn.setOnClickPendingIntent(R.id.to_turn, pendingIntentTurn)
            appWidgetManager.updateAppWidget(appWidgetId, viewsTurn)

            val intentProxy = Intent(context, AppActivity::class.java)
            intentProxy.putExtra("toProxy", true)
            intentProxy.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntentProxy = PendingIntent.getActivity(
                context,
                2,
                intentProxy,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val viewsProxy = RemoteViews(context.packageName, R.layout.new_app_widget)
            viewsProxy.setOnClickPendingIntent(R.id.to_list, pendingIntentProxy)
            appWidgetManager.updateAppWidget(appWidgetId, viewsProxy)

            val intentService = Intent(context, AppActivity::class.java)
            intentService.putExtra("toAdvert", true)
            intentService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntentService = PendingIntent.getActivity(
                context,
                3,
                intentService,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val viewsService = RemoteViews(context.packageName, R.layout.new_app_widget)
            viewsService.setOnClickPendingIntent(R.id.to_service, pendingIntentService)
            appWidgetManager.updateAppWidget(appWidgetId, viewsService)

            val intentFavorite = Intent(context, AppActivity::class.java)
            intentFavorite.putExtra("toFavorite", true)
            intentFavorite.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val intentFavoriteService = PendingIntent.getActivity(
                context,
                4,
                intentFavorite,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val viewsFavorite = RemoteViews(context.packageName, R.layout.new_app_widget)
            viewsFavorite.setOnClickPendingIntent(R.id.to_favorite, intentFavoriteService)
            appWidgetManager.updateAppWidget(appWidgetId, viewsFavorite)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.new_app_widget)
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}