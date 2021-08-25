package kr.co.iboss.chat.Utils

import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird

/*
 * 채팅방의 이름을 지정하는 TextUtils
 * 기본적으로는 사용자가 직접 지정한 채팅방 이름을 사용하지만, 지정한 이름이 없을 시 채팅방 Member들의 이름으로 채팅방 이름으로 지정
 */
object TextUtils {
    fun getGroupChannelTitle(channel: GroupChannel): String {
        val members = channel.members

        if (members.size < 2 || SendBird.getCurrentUser() == null) {
            return SendBird.getCurrentUser().nickname
        }

        else if (members.size == 2) {
            val buffer = StringBuffer()
            for (member in members) {
                if (member.userId == SendBird.getCurrentUser().userId) {
                    continue
                }
                buffer.append(", " + member.nickname)
            }

            return buffer.delete(0, 2).toString()
        }

        else {
            var cnt = 0
            val buffer = StringBuffer()

            for (member in members) {
                if (member.userId == SendBird.getCurrentUser().userId) {
                    continue
                }
                cnt++
                buffer.append(", " + member.nickname)

                if (cnt >= 5) {
                    break
                }
            }

            return buffer.delete(0, 2).toString()
        }
    }
}