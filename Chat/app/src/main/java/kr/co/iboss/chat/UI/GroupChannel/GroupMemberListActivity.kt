package kr.co.iboss.chat.UI.GroupChannel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.GroupChannel
import com.sendbird.android.Member
import com.sendbird.android.SendBird
import kr.co.iboss.chat.R
import kr.co.iboss.chat.UI.GroupChannel.Adapter.GroupMemberListAdapter
import kr.co.iboss.chat.Utils.ConnectionUtils
import kr.co.iboss.chat.databinding.ActivityGroupMemberListBinding

class GroupMemberListActivity : AppCompatActivity() {

    companion object {
        private val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"
        private val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_MEMBER_LIST"
    }

    private var adapter : GroupMemberListAdapter? = null
    private var recyclerView : RecyclerView? = null
    private var layoutManager : LinearLayoutManager? = null
    private var mChannelURL : String? = null
    private var mChannel : GroupChannel? = null

    private lateinit var binding : ActivityGroupMemberListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupMemberListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mChannelURL = intent.getStringExtra(EXTRA_CHANNEL_URL)

        setUpRecyclerView()
    }

    override fun onResume() {
        super.onResume()

        ConnectionUtils.addConnectionManagementHandler(CONNECTION_HANDLER_ID, object : ConnectionUtils.ConnectionManagementHandler {
            override fun onConnected(reconnect: Boolean) {
                getChannelUrl(mChannelURL)
            }

        })
    }

    override fun onPause() {
        super.onPause()

        ConnectionUtils.removeConnectionManagementHandler(CONNECTION_HANDLER_ID)
    }

    private fun getChannelUrl(channelURL: String?) {
        GroupChannel.getChannel(channelURL, GroupChannel.GroupChannelGetHandler { groupChannel, e ->
            if (e != null) {
                return@GroupChannelGetHandler
            }

            mChannel = groupChannel

            refresh()
        })
    }

    private fun refresh() {
        mChannel!!.refresh(GroupChannel.GroupChannelRefreshHandler { e ->
            if (e != null) {
                return@GroupChannelRefreshHandler
            }

            setGroupMemberList(mChannel!!.members)
        })
    }

    private fun setGroupMemberList(memberList : List<Member>) {
        val chatMemberList = ArrayList<Member>()

        for (me in memberList) {
            if (me.userId == SendBird.getCurrentUser().userId) {
                chatMemberList.add(me)
                break
            }
        }

        for (other in memberList) {
            if (other.userId == SendBird.getCurrentUser().userId) {
                continue
            }
            chatMemberList.add(other)
        }

        adapter!!.setMemberList(chatMemberList)
    }

    private fun setUpRecyclerView() {
        adapter = GroupMemberListAdapter(this, mChannelURL!!, true)
        recyclerView = binding.chatInfoMemberListRV
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = adapter
        recyclerView!!.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
}