package com.resonix.media.core.media.network

import com.resonix.media.core.model.NetworkConnection

fun interface NetworkClientFactory {
    fun create(connection: NetworkConnection): NetworkClient
}
