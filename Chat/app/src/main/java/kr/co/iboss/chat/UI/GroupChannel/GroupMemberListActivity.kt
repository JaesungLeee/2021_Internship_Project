package kr.co.iboss.chat.UI.GroupChannel

import android.content.Intent
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

/*
 * 해당 그룹 채널에 참여중인 전체 Member의 리스트를  보이는 Activity
 */
class GroupMemberListActivity : AppCompatActivity() {

    companion object {
        private val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"
        private val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_MEMBER_LIST"
    }

    private var mListAdapter : GroupMemberListAdapter? = null
    private var mLayoutManager : LinearLayoutManager? = null
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

        buttonListener()
    }

    /**
     * 뒤로 가기 버튼 클릭 리스너 Method
     * : mChannelURL을 다시 전달하는 부분에 대한 Refactoring이 필요 할 것 같음
     */
    private fun buttonListener() {
        binding.chatInfoNavigateBeforeBtn.setOnClickListener {
            val intent = Intent(this, GroupChatActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL_URL, mChannelURL)
            }
            startActivity(intent)
            finish()
        }
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

    /*
     * Member List를 생성하는 Method
     * 자신을 가장 List의 맨 앞에 넣게됨
     */
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

        mListAdapter!!.setMemberList(chatMemberList)
    }

    private fun setUpRecyclerView() {
        mLayoutManager = LinearLayoutManager(this)
        mListAdapter = GroupMemberListAdapter(this, mChannelURL!!, true)
        binding.chatInfoMemberListRV.layoutManager = mLayoutManager
        binding.chatInfoMemberListRV.adapter = mListAdapter
        binding.chatInfoMemberListRV.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
}