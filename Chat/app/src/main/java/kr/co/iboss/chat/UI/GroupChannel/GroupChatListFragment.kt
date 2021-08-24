package kr.co.iboss.chat.UI.GroupChannel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.*
import kr.co.iboss.chat.UI.GroupChannel.Adapter.GroupChatListAdapter
import kr.co.iboss.chat.UI.MainActivity
import kr.co.iboss.chat.Utils.ConnectionUtils
import kr.co.iboss.chat.databinding.FragmentGroupChatListBinding

/*
 * 사용자가 참여 중인 GroupChannel의 전체 리스트를 보여주는 Fragment
 */
class GroupChatListFragment : Fragment(), GroupChatListAdapter.OnChannelClickedListener, GroupChatListAdapter.OnChannelLongClickedListener {

    companion object {
        private val EXTRA_CHANNEL_URL           = "EXTRA_CHANNEL_URL"
        private val EXTRA_CHANNEL_NAME          = "EXTRA_CHANNEL_NAME"
        private val CHANNEL_HANDLER_ID          = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST"
        private val CONNECTION_HANDLER_ID       = "CONNECTION_HANDLER_GROUP_CHANNEL_LIST"
        private val CHANNEL_LIMIT               = 10
    }

    lateinit var mainActivityContext : MainActivity
    lateinit var gChatListAdapter : GroupChatListAdapter
    private var gChannelListQuery : GroupChannelListQuery? = null

    private var fragGroupChatListBinding : FragmentGroupChatListBinding? = null
    private var groupChannelUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupChannelUrl = it.getString("groupChannelUrl")   // Push 상태일 때 MainActivity에서 전달받은 groupChannelUrl 수신
        }

        if (groupChannelUrl != null) {      // Push 상태일 때 groupChannelUrl이 있으면 자동으로 화면 전환
            val channelIntent = Intent(mainActivityContext, GroupChatActivity::class.java)
            channelIntent.putExtra("groupChannelUrl", groupChannelUrl)
            startActivity(channelIntent)

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mainActivityContext = context as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentGroupChatListBinding.inflate(inflater, container, false)
        fragGroupChatListBinding = binding
        return fragGroupChatListBinding!!.root
    }

    override fun onDestroyView() {
        fragGroupChatListBinding = null     // 메모리 관리 (Fragment가 사라질때 binding 객체가 사라짐)
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        gChatListAdapter = GroupChatListAdapter(this, this)
        setUpRecyclerView()
        refresh()

        buttonListener()
    }

    override fun onResume() {
        ConnectionUtils.addConnectionManagementHandler(CONNECTION_HANDLER_ID, object : ConnectionUtils.ConnectionManagementHandler {
            override fun onConnected(reconnect: Boolean) {
                refresh()
            }

        })

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, object : SendBird.ChannelHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage) {}

            override fun onChannelChanged(channel: BaseChannel?) {
                gChatListAdapter.updateInsert(channel as GroupChannel)
            }

            override fun onTypingStatusUpdated(channel: GroupChannel?) {
                gChatListAdapter.notifyDataSetChanged()
            }

        })

        super.onResume()
    }

    override fun onPause() {
        ConnectionUtils.removeConnectionManagementHandler(CONNECTION_HANDLER_ID)
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID)
        super.onPause()
    }

    private fun buttonListener() {
        fragGroupChatListBinding!!.groupChannelListSwipeLayout.setOnRefreshListener {
            fragGroupChatListBinding!!.groupChannelListSwipeLayout.isRefreshing = true
            refresh()
        }
    }

    private fun setUpRecyclerView() {

        fragGroupChatListBinding!!.groupChannelRV.adapter = gChatListAdapter
        fragGroupChatListBinding!!.groupChannelRV.layoutManager = LinearLayoutManager(mainActivityContext)
        fragGroupChatListBinding!!.groupChannelRV.addItemDecoration(DividerItemDecoration(mainActivityContext,  DividerItemDecoration.VERTICAL))

        fragGroupChatListBinding!!.groupChannelRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (LinearLayoutManager(mainActivityContext).findLastVisibleItemPosition() == gChatListAdapter!!.itemCount - 1) {
                    loadNextChannelList()
                }
            }
        })

        refresh()
    }

    private fun refresh() {
        refreshChannelList(CHANNEL_LIMIT)
    }

    private fun refreshChannelList(channelsCnt : Int) {
        gChannelListQuery = GroupChannel.createMyGroupChannelListQuery()
        gChannelListQuery!!.limit = channelsCnt
        gChannelListQuery!!.isIncludeEmpty = true

        gChannelListQuery!!.next(GroupChannelListQuery.GroupChannelListQueryResultHandler { list, e ->
            if (e != null) {
                e.printStackTrace()
                return@GroupChannelListQueryResultHandler
            }

//            Log.e("CHECK", list.toString())
            gChatListAdapter.setGroupChannelList(list)

        })

        if (fragGroupChatListBinding!!.groupChannelListSwipeLayout.isRefreshing) {
            fragGroupChatListBinding!!.groupChannelListSwipeLayout.isRefreshing = false
        }
    }

    private fun loadNextChannelList() {
        gChannelListQuery!!.next(GroupChannelListQuery.GroupChannelListQueryResultHandler { channelList, e ->
            if (e != null) {
                e.printStackTrace()
                return@GroupChannelListQueryResultHandler
            }

            for (channel in channelList) {
                gChatListAdapter.addLast(channel)
            }
        })

    }

    /* 채팅방을 길게 눌렀을 때 Dialog 생성 Method */
    private fun showLeaveChannelDialog(channel: GroupChannel) {
        val dialogOptions = arrayOf("채팅방 나가기")

        val builder = AlertDialog.Builder(mainActivityContext)
        builder.setItems(dialogOptions) {dialog, which ->
            if (which == 0) {
                leaveChannel(channel)
            }
        }

        builder.create().show()
    }

    private fun leaveChannel(channel: GroupChannel) {
        channel.leave { e ->
            if (e != null) {
                return@leave
            }

            refresh()
        }
    }

    override fun onItemClicked(channel: GroupChannel) {
        activity?.let {
            val intent = Intent(mainActivityContext, GroupChatActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL_URL, channel.url)
                putExtra(EXTRA_CHANNEL_NAME, channel.name)
            }
            startActivity(intent)
        }
    }

    override fun onItemLongClicked(channel: GroupChannel) {
        showLeaveChannelDialog(channel)
    }
}