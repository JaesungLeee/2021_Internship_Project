package kr.co.iboss.chat.Utils

import com.sendbird.android.SendBird

object ConnectionUtils {

    fun addConnectionManagementHandler(handlerId : String, handler: ConnectionManagementHandler?) {
        SendBird.addConnectionHandler(handlerId, object : SendBird.ConnectionHandler {
            override fun onReconnectStarted() {

            }

            override fun onReconnectSucceeded() {
                handler?.onConnected(true)
            }

            override fun onReconnectFailed() {

            }
        })

        if (SendBird.getConnectionState() == SendBird.ConnectionState.OPEN) {
            handler?.onConnected(false)
        }

        else if (SendBird.getConnectionState() == SendBird.ConnectionState.CLOSED) {
            handler?.onConnected(false)
        }
    }

    fun removeConnectionManagementHandler(handlerId: String) {
        SendBird.removeConnectionHandler(handlerId)
    }

    interface ConnectionManagementHandler {
        fun onConnected(reconnect : Boolean)
    }
}