package kr.co.iboss.chat.UI.GroupChannel.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sendbird.android.AdminMessage
import com.sendbird.android.FileMessage
import com.sendbird.android.GroupChannel
import com.sendbird.android.UserMessage
import kr.co.iboss.chat.Utils.DateUtils
import kr.co.iboss.chat.Utils.TextUtils
import kr.co.iboss.chat.databinding.ListItemGroupChatRoomBinding

/**
 * GroupChatListFragment RecyclerView의 Adapter Class
 */
class GroupChatListAdapter(clickedListener: OnChannelClickedListener, longClickedListener: OnChannelLongClickedListener) : RecyclerView.Adapter<GroupChatListAdapter.GroupChatListHolder>() {
    interface OnChannelClickedListener {
        fun onItemClicked(channel : GroupChannel)
    }

    interface OnChannelLongClickedListener {
        fun onItemLongClicked(channel : GroupChannel)
    }

    private val channelClickedListener : OnChannelClickedListener
    private val channelLongClickListener : OnChannelLongClickedListener
    private var channels : MutableList<GroupChannel>

    init {
        channels = ArrayList()
        this.channelClickedListener = clickedListener
        this.channelLongClickListener = longClickedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : GroupChatListHolder {
        val binding = ListItemGroupChatRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupChatListHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupChatListHolder, position: Int) {
        holder.bindItems(channels[position], channelClickedListener, channelLongClickListener)
    }

    override fun getItemCount(): Int {
        return channels.size
    }

    /**
     * RecyclerView UI상에 그룹채널의 리스트가 보이게 하는 Method
     * @param channelList   그룹 채널 리스트
    */
    fun setGroupChannelList(channelList : MutableList<GroupChannel>) {
        this.channels = channelList
        notifyDataSetChanged()
    }

    /**
     * Scroll을 내려서 다음 리스트를 불러올 때의 Method
     * @param channel   그룹 채널
     */
    fun addLast(channel : GroupChannel) {
        channels.add(channel)
        notifyDataSetChanged()
    }

    /**
     * Channel의 정보가 수정되는 경우
     * @param channel   그룹 채널
     */
    fun updateInsert(channel : GroupChannel) {
        if (channel !is GroupChannel) return

        for (it in channels.indices) {
            if (channels[it].url == channel.url) {
                channels.remove(channels[it])
                channels.add(0, channel)
                notifyDataSetChanged()
                return
            }
        }
    }

    inner class GroupChatListHolder (var binding : ListItemGroupChatRoomBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(groupChannel : GroupChannel, clickedListener : OnChannelClickedListener, longClickedListener: OnChannelLongClickedListener) { //listener : OnChannelClickedListener
            // Channel Cover Image
            setChannelImage(groupChannel)

            // Title, Member Count
            if (groupChannel.name != null) {
                binding.groupChannelRoomTitleTV.text = groupChannel.name
            }
            else binding.groupChannelRoomTitleTV.text = TextUtils.getGroupChannelTitle(groupChannel)

            binding.groupChannelRoomTitleTV.text = groupChannel.name
            binding.groupChannelRoomMemberCountTV.text = groupChannel.memberCount.toString()

            // Unread Count
            val unreadCnt = groupChannel.unreadMessageCount
            if (unreadCnt == 0) {
                binding.groupChannelRoomUnreadCountTV.visibility = View.INVISIBLE
            }
            else {
                binding.groupChannelRoomUnreadCountTV.visibility = View.VISIBLE
                binding.groupChannelRoomUnreadCountTV.text = groupChannel.unreadMessageCount.toString()
            }

            // Message
            val lastMsg = groupChannel.lastMessage
            if (lastMsg != null) {
                binding.groupChannelRoomDateTV.visibility = View.VISIBLE
                binding.groupChannelRoomMessageTV.visibility = View.VISIBLE

                binding.groupChannelRoomDateTV.text = DateUtils.formatDateTime(lastMsg.createdAt)

                when (lastMsg) {
                    is UserMessage -> binding.groupChannelRoomMessageTV.text = lastMsg.message
                    is AdminMessage -> binding.groupChannelRoomMessageTV.text = lastMsg.message
                    else -> {
                        val lastMsgFormat = String.format("%s"+"님이 파일을 전송하였습니다.", (lastMsg as FileMessage).sender.nickname)
                        binding.groupChannelRoomMessageTV.text = lastMsgFormat
                    }
                }

            }
            else {  // 메시지가 없을 때, (생성되고 채팅이 없었던 channel)
                binding.groupChannelRoomDateTV.visibility = View.INVISIBLE
                binding.groupChannelRoomMessageTV.visibility = View.INVISIBLE
            }

            binding.groupChannelRoomContainer.setOnClickListener {
                clickedListener.onItemClicked(groupChannel)
            }

            binding.groupChannelRoomContainer.setOnLongClickListener {
                longClickedListener.onItemLongClicked(groupChannel)
                true
            }
        }

        private fun setChannelImage(channel : GroupChannel) {
            Glide
                .with(binding.root.context)
                .load(channel.coverUrl)
                .centerCrop()
                .into(binding.groupChannelRoomCoverIV)

        }
    }
}