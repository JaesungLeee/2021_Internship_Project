package kr.co.iboss.chat.Utils

import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird

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