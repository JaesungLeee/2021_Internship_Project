package kr.co.iboss.chat.UI.GroupChannel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.*
import kr.co.iboss.chat.UI.GroupChannel.Adapter.GroupChatListAdapter
import kr.co.iboss.chat.Utils.ConnectionUtils
import kr.co.iboss.chat.databinding.FragmentGroupChatListBinding

/**
 * A simple [Fragment] subclass.
 * Use the [GroupChatListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class GroupChatListFragment : Fragment(), GroupChatListAdapter.OnChannelClickedListener {

    companion object {
        private val EXTRA_CHANNEL_URL           = "EXTRA_CHANNEL_URL"
        private val EXTRA_CHANNEL_NAME          = "EXTRA_CHANNEL_NAME"
        private val CHANNEL_HANDLER_ID          = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST"
        private val CONNECTION_HANDLER_ID       = "CONNECTION_HANDLER_GROUP_CHANNEL_LIST"
        private val CHANNEL_LIMIT               = 10
    }

    lateinit var gChatListAdapter : GroupChatListAdapter
    private var gChannelListQuery : GroupChannelListQuery? = null

    private var fragGroupChatListBinding : FragmentGroupChatListBinding? = null
    private var groupChannelUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupChannelUrl = it.getString("groupChannelUrl")
        }

        if (groupChannelUrl != null) {
            val channelIntent = Intent(fragGroupChatListBinding!!.root.context, GroupChatActivity::class.java)
            channelIntent.putExtra("groupChannelUrl", groupChannelUrl)
            startActivity(channelIntent)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.e("LIFECYCLE", "ONCREATEVIEW")
        // Inflate the layout for this fragment
        val binding = FragmentGroupChatListBinding.inflate(inflater, container, false)
        fragGroupChatListBinding = binding
        return fragGroupChatListBinding!!.root
    }

    override fun onDestroyView() {
        fragGroupChatListBinding = null     // 메모리 관리 (Fragment가 사라질때 binding 객체가 사라짐)
        Log.e("LIFECYCLE", "ONDESTROYVIEW")
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.e("LIFECYCLE", "ONVIEWCREATED")

        gChatListAdapter = GroupChatListAdapter(this)
        setUpRecyclerView()
        refresh()

        buttonListener()
    }

    override fun onResume() {
        Log.e("LIFECYCLE", "ONRESUME")

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
        Log.e("LIFECYCLE", "ONPAUSE")

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
        fragGroupChatListBinding!!.groupChannelRV.layoutManager = LinearLayoutManager(fragGroupChatListBinding!!.root.context)
        fragGroupChatListBinding!!.groupChannelRV.addItemDecoration(DividerItemDecoration(fragGroupChatListBinding!!.root.context, DividerItemDecoration.VERTICAL))

        fragGroupChatListBinding!!.groupChannelRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (LinearLayoutManager(fragGroupChatListBinding!!.root.context).findLastVisibleItemPosition() == gChatListAdapter!!.itemCount - 1) {
                    loadNextChannelList()
                }
            }
        })

        refresh()
    }

//    private fun leaveChannel(channel : GroupChannel) {
//        channel.leave(GroupChannel.GroupChannelLeaveHandler { e ->
//            if (e != null) {
//                e.printStackTrace()
//                return@GroupChannelLeaveHandler
//            }
//
//            refresh()
//        })
//    }

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

    override fun onItemClicked(channel: GroupChannel) {
        activity?.let {
            val intent = Intent(fragGroupChatListBinding!!.root.context, GroupChatActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL_URL, channel.url)
                putExtra(EXTRA_CHANNEL_NAME, channel.name)
            }
            startActivity(intent)
        }
    }
}