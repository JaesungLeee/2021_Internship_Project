package kr.co.iboss.chat.UI.GroupChannel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sendbird.android.ConnectionManager
import com.sendbird.android.GroupChannel
import com.sendbird.android.Member
import com.sendbird.android.SendBird
import kr.co.iboss.chat.R
import kr.co.iboss.chat.Utils.ConnectionUtils
import kr.co.iboss.chat.Utils.ImageUtils
import kr.co.iboss.chat.databinding.ActivityGroupMemberInfoBinding

class GroupMemberInfoActivity : AppCompatActivity() {

    companion object {
        private val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_MEMBER_LIST"
        private val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"
        private val EXTRA_USER_ID = "EXTRA_USER_ID"
        private val EXTRA_USER_PROFILE_URL = "EXTRA_USER_PROFILE_URL"
        private val EXTRA_USER_NICKNAME = "EXTRA_USER_NICKNAME"
        private val EXTRA_USER_BLOCKED_BY_ME = "EXTRA_USER_BLOCKED_BY_ME"
    }

    private lateinit var binding : ActivityGroupMemberInfoBinding

    private var mChannelURL : String? = null
    private var mUserId : String? = null
    private var mChannel : GroupChannel? = null
    private var mMember : Member? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupMemberInfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mChannelURL = intent.getStringExtra(EXTRA_CHANNEL_URL)
        mUserId = intent.getStringExtra(EXTRA_USER_ID)
        val nickName = intent.getStringExtra(EXTRA_USER_NICKNAME)
        val blockedByMe = intent.getBooleanExtra(EXTRA_USER_BLOCKED_BY_ME, false)
        val profileUrl = intent.getStringExtra(EXTRA_USER_PROFILE_URL)

        switchListener()

        refreshUser(profileUrl, nickName, blockedByMe)
    }

    override fun onResume() {
        super.onResume()

        ConnectionUtils.addConnectionManagementHandler(CONNECTION_HANDLER_ID, object : ConnectionUtils.ConnectionManagementHandler {
            override fun onConnected(reconnect: Boolean) {
                getUserFromURL(mChannelURL)
            }
        })
    }

    override fun onPause() {
        super.onPause()

        ConnectionUtils.removeConnectionManagementHandler(CONNECTION_HANDLER_ID)
    }

    private fun getUserFromURL(url : String?) {
        GroupChannel.getChannel(url, GroupChannel.GroupChannelGetHandler { groupChannel, e ->
            if (e != null) {
                return@GroupChannelGetHandler
            }

            mChannel = groupChannel

            refreshChannel()
        })
    }

    private fun refreshChannel() {
        mChannel!!.refresh(GroupChannel.GroupChannelRefreshHandler { e ->
            if (e != null) {
                return@GroupChannelRefreshHandler
            }

            for (member in mChannel!!.members) {
                if (member.userId == mUserId) {
                    mMember = member
                    break
                }
            }

            refreshUser(mMember!!.profileUrl, mMember!!.nickname, mMember!!.isBlockedByMe)
        })
    }

    private fun refreshUser(url: String?, nickname: String?, isBlockedByMe: Boolean) {
        ImageUtils.displayRoundImageFromUrl(this, url, binding.groupMemberInfoProfileIV)
        binding.groupMemberInfoNickNameTV.text = nickname
        binding.groupMemberInfoBlockSwitch.isChecked = isBlockedByMe
    }

    private fun switchListener() {
        binding.groupMemberInfoBlockSwitch.setOnClickListener {
            if (binding.groupMemberInfoBlockSwitch.isChecked) {
                // block
                SendBird.blockUser(mMember, SendBird.UserBlockHandler { user, e->
                    if (e != null) {
                        binding.groupMemberInfoBlockSwitch.isChecked = false
                        return@UserBlockHandler
                    }
                })
            }
            else {
                // unblock
                SendBird.unblockUser(mMember, SendBird.UserUnblockHandler { e ->
                    if (e != null) {
                        binding.groupMemberInfoBlockSwitch.isChecked = true
                        return@UserUnblockHandler
                    }
                })
            }
        }
    }


}