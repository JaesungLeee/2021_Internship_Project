package kr.co.iboss.chat.Utils

import com.sendbird.android.SendBird
import kr.co.iboss.chat.BaseApplication

object ConnectionUtils {

    fun isLogin() : Boolean {
        return PreferencesUtils(BaseApplication.instance.context()).getConnected()
    }

    fun login(userId : String, handler : SendBird.ConnectHandler?) {
        SendBird.connect(userId) { user, e ->
            handler?.onConnected(user, e)
        }
    }

    fun logout(handler : SendBird.DisconnectHandler?) {
        SendBird.disconnect {
            handler?.onDisconnected()
        }
    }

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
            val userId = PreferencesUtils(BaseApplication.instance.context()).getUserId()
            SendBird.connect(userId) { user, e ->
                if (e != null) {
                    return@connect
                }
                handler?.onConnected(false)
            }
        }
    }

    fun removeConnectionManagementHandler(handlerId: String) {
        SendBird.removeConnectionHandler(handlerId)
    }

    interface ConnectionManagementHandler {
        fun onConnected(reconnect : Boolean)
    }
}