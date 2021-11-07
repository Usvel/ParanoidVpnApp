package com.paranoid.vpn.app.common.remote.base

interface Mapper<T, D> {
    fun mapToEntity(obj: T): D? {
        return null
    }

    fun mapFromEntity(obj: D): T? {
        return null
    }
}
