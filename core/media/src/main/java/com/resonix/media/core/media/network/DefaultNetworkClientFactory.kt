package com.resonix.media.core.media.network

import com.resonix.media.core.media.network.clients.FtpClient
import com.resonix.media.core.media.network.clients.SftpClient
import com.resonix.media.core.media.network.clients.SmbClient
import com.resonix.media.core.media.network.clients.WebDavClient
import com.resonix.media.core.media.network.keys.SshKeyStore
import com.resonix.media.core.model.NetworkConnection
import com.resonix.media.core.model.NetworkProtocol
import org.koin.core.annotation.Single

@Single
class DefaultNetworkClientFactory(
    private val sshKeyStore: SshKeyStore,
) : NetworkClientFactory {
    override fun create(connection: NetworkConnection): NetworkClient = when (connection.protocol) {
        NetworkProtocol.SMB -> SmbClient(connection)
        NetworkProtocol.FTP -> FtpClient(connection)
        NetworkProtocol.SFTP -> SftpClient(connection, sshKeyStore)
        NetworkProtocol.WEBDAV -> WebDavClient(connection)
    }
}
