package kr.co.iboss.chat.Utils

import com.sendbird.android.SendBird
import kr.co.iboss.chat.BaseApplication

/*
 * Connection과 관련된 Method들을 관리하는 Utils File
 * Login, Logout, ConnectionHandler ..
 */
object ConnectionUtils {

    /**
     * 로그인 상태 판별
     * @return T/F
     */
    fun isLogin() : Boolean {
        return PreferencesUtils(BaseApplication.instance.context()).getConnected()
    }

    /* SendBird 서버와의 연결을 설정하는 Method */
    fun login(userId : String, handler : SendBird.ConnectHandler?) {
        SendBird.connect(userId) { user, e ->
            handler?.onConnected(user, e)
        }
    }

    /* SendBird 서버와의 연결을 해지하는 Method */
    fun logout(handler : SendBird.DisconnectHandler?) {
        SendBird.disconnect {
            handler?.onDisconnected()
        }
    }

    /* SendBird 서버 Connection Handler Method */
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

        // 재 연결 설정
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

    /* SendBird 서버 Remove Connection Handler Method */
    fun removeConnectionManagementHandler(handlerId: String) {
        SendBird.removeConnectionHandler(handlerId)
    }

    /* 다른 File에서 참조할 수 있도록 설정한 Interface */
    interface ConnectionManagementHandler {
        fun onConnected(reconnect : Boolean)
    }
}