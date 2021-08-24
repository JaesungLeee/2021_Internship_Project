package kr.co.iboss.chat.UI.GroupChannel.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.dinuscxj.progressbar.CircleProgressBar
import com.sendbird.android.*
import kr.co.iboss.chat.Utils.DateUtils
import kr.co.iboss.chat.Utils.ImageUtils
import kr.co.iboss.chat.databinding.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * GroupChatActivity RecyclerView의 Adapter Class
 */
class GroupChatAdapter(private var mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /* 메시지 타입별 Flag 선언*/
    companion object {
        private val VIEW_TYPE_USER_MESSAGE_ME                = 10
        private val VIEW_TYPE_USER_MESSAGE_OTHER             = 11
        private val VIEW_TYPE_FILE_MESSAGE_ME                = 21
        private val VIEW_TYPE_FILE_MESSAGE_OTHER             = 22
        private val VIEW_TYPE_FILE_IMAGE_MESSAGE_ME          = 23
        private val VIEW_TYPE_FILE_IMAGE_MESSAGE_OTHER       = 24
        private val VIEW_TYPE_ADMIN_MESSAGE                  = 30
    }

    private var mChannel : GroupChannel? = null
    private val messages : MutableList<BaseMessage> // Message가 담길 리스트
    private val mFileMessageMap: HashMap<FileMessage, CircleProgressBar>

    private var itemClickListener : OnItemClickListener? = null
    private var itemLongClickListener : OnItemLongClickListener? = null

    private var isMessageListLoading: Boolean = false
    private val failedMessages = ArrayList<String>()
    private val mTempFileMessageUriTable = Hashtable<String, Uri>()

    interface OnItemLongClickListener {
        fun onUserMessageLongClick(message : UserMessage, position: Int)

        fun onFileMessageLongClick(message : FileMessage)
    }

    interface OnItemClickListener {
        fun onUserMessageClick(message : UserMessage)

        fun onFileMessageClick(message : FileMessage)
    }

    init {
        messages = ArrayList()
        mFileMessageMap = HashMap()
    }

    fun setContext(context: Context) {
        mContext = context
    }

    fun setChannel(channel : GroupChannel) {
        mChannel = channel
    }

    /**
     * 메시지 수신 시 Call되는 Method
     * @param message   수신되는 메시지 : BaseMessage
     */
    fun addFirst(message : BaseMessage) {
        messages.add(0, message)    // Index를 0으로 주어 리스트의 맨 앞에 넣고 UI 상으로 하단에 가장 최근에 전송한 메시지가 보임
        notifyDataSetChanged()
    }

    /**
     * 메시지 삭제 시 Call되는 Method
     * @param msgId     삭제 대상 메시지의 고유 Id
     */
    fun delete(msgId : Long) {
        for (message in messages) {
            if (message.messageId == msgId) {
                messages.remove(message)
                notifyDataSetChanged()
                break
            }
        }
    }

    /**
     * 메시지 수정 시 Call되는 Method
     * @param message   수정 대상 메시지 : BaseMessage
     */
    fun update(message : BaseMessage) {
        var baseMessage : BaseMessage

        for (idx in messages.indices) {
            baseMessage = messages[idx]

            if (message.messageId == baseMessage.messageId) {
                messages.removeAt(idx)      // 해당 메시지 삭제
                messages.add(idx, message)  // idx 위치에 다시 수정 메시지 삽입
                notifyDataSetChanged()
                break
            }
        }
    }

    /**
     * RecyclerView Item의 타입을 결정하는 Override Method
     * 메시지 종류에 따라 UI상에 보여질 Item을 구분함
     * @param position      특정 메시지
     * @return VIEW_TYPE    타입 결정
     */
    override fun getItemViewType(position: Int): Int {
        val message = messages.get(position)

        return when(message) {
            is UserMessage ->
                if (message.sender.userId == SendBird.getCurrentUser().userId) {
                    VIEW_TYPE_USER_MESSAGE_ME
                } else VIEW_TYPE_USER_MESSAGE_OTHER
            is FileMessage ->
                if (message.type.startsWith("image")) {
                    if (message.sender.userId == SendBird.getCurrentUser().userId) {
                        VIEW_TYPE_FILE_IMAGE_MESSAGE_ME
                    } else VIEW_TYPE_FILE_IMAGE_MESSAGE_OTHER
                }
                else {
                    if (message.sender.userId == SendBird.getCurrentUser().userId) {
                        VIEW_TYPE_FILE_MESSAGE_ME
                    } else VIEW_TYPE_FILE_MESSAGE_OTHER
                }
            is AdminMessage ->
                VIEW_TYPE_ADMIN_MESSAGE
            else -> -1
        }
    }

    /**
     * VIEW_TYPE 별 ViewHolder를 매칭 시키는 Override Method
     * @param parent    부모 View Group
     * @param viewType  VIEW_TYPE : Integer
     * @return ViewHolder
     * @see #getItemViewType()
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_USER_MESSAGE_ME -> {
                val binding = ListItemGroupChatUserMeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return MyUserMessageHolder(binding)
            }
            VIEW_TYPE_USER_MESSAGE_OTHER -> {
                val binding = ListItemGroupChatUserOtherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return OtherUserMessageHolder(binding)
            }
            VIEW_TYPE_FILE_MESSAGE_ME -> {
                val binding = ListItemGroupChatFileMeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return MyFileMessageHolder(binding)
            }
            VIEW_TYPE_FILE_MESSAGE_OTHER -> {
                val binding = ListItemGroupChatFileOtherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return OtherFileMessageHolder(binding)
            }
            VIEW_TYPE_FILE_IMAGE_MESSAGE_ME -> {
                val binding = ListItemGroupFileImageMessageMeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return MyImageFileMessageHolder(binding)
            }
            VIEW_TYPE_FILE_IMAGE_MESSAGE_OTHER -> {
                val binding = ListItemGroupFileImageMessageOtherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return OtherImageFileMessageHolder(binding)
            }
            VIEW_TYPE_ADMIN_MESSAGE -> {
                val binding = ListItemGroupChatAdminMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return AdminMessageHolder(binding)
            }

            // 다시 확인해봐야함
            else -> {
                val binding = ListItemGroupChatUserMeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return MyUserMessageHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        var isNewDay = false
        var isContinuous = false
        var isTempMessage = false
        var isFailedMessage = false
        var tempFileMessageUri : Uri? = null

        if (position < messages.size - 1) {
            val prevMsg = messages[position + 1]

            if (!DateUtils.isSameDate(message.createdAt, prevMsg.createdAt)) {
                isNewDay = true
                isContinuous = false
            }
            else {
                isContinuous = isContinuous(message, prevMsg)
            }
        }
        else if (position == messages.size - 1) {
            isNewDay = true
        }

        isTempMessage = isTempMessage(message)
        isFailedMessage = isFailedMessage(message)
        tempFileMessageUri = getTempFileMessageUri(message)

        when (holder.itemViewType) {
            VIEW_TYPE_USER_MESSAGE_ME -> (holder as MyUserMessageHolder).bindViews(mContext, message as UserMessage, mChannel, isContinuous, isNewDay, isTempMessage, isFailedMessage, itemClickListener, itemLongClickListener, position)
            VIEW_TYPE_USER_MESSAGE_OTHER -> (holder as OtherUserMessageHolder).bindViews(mContext, message as UserMessage, mChannel, isNewDay, isContinuous, itemClickListener, itemLongClickListener, position)
            VIEW_TYPE_FILE_MESSAGE_ME -> (holder as MyFileMessageHolder).bindViews(mContext, message as FileMessage, mChannel, isNewDay, isTempMessage, isFailedMessage, tempFileMessageUri, itemClickListener)
            VIEW_TYPE_FILE_MESSAGE_OTHER -> (holder as OtherFileMessageHolder).bindViews(mContext, message as FileMessage, mChannel, isNewDay, isContinuous, itemClickListener)
            VIEW_TYPE_FILE_IMAGE_MESSAGE_ME -> (holder as MyImageFileMessageHolder).bindViews(mContext, message as FileMessage, mChannel, isNewDay, isContinuous, isTempMessage, tempFileMessageUri, itemClickListener)
            VIEW_TYPE_FILE_IMAGE_MESSAGE_OTHER -> (holder as OtherImageFileMessageHolder).bindViews(mContext, message as FileMessage, mChannel, isNewDay, isContinuous, itemClickListener)
            VIEW_TYPE_ADMIN_MESSAGE -> (holder as AdminMessageHolder).bindViews(mContext, message as AdminMessage, mChannel, isNewDay)
            else -> {}
        }


    }

    override fun getItemCount() = messages.size

    /**
     * 메시지 마다 고유의 Id가 존재
     * messageId가 0인 경우 비어있는 메시지로 판별
     * @param message   메시지 Object
     * @return T/F
     */
    fun isTempMessage(message: BaseMessage) : Boolean = message.messageId == 0L

    /**
     * 전송 실패된 Message id의 리스트의 index를 검사
     * index가 존재하면 isFailedMessage (return true)
     * @param   message : BaseMessage
     * @return  Boolean
     */
    fun isFailedMessage(message: BaseMessage) : Boolean {
        if (!isTempMessage(message)) {
            return false
        }

        if (message is UserMessage) {
            val messageIdx = failedMessages.indexOf(message.requestId)
            if (messageIdx >= 0) {
                return true
            }
        }
        else if (message is FileMessage) {
            val messageIdx = failedMessages.indexOf(message.requestId)
            if (messageIdx >= 0) {
                return true
            }
        }

        return false
    }

    private fun addFailedMessage(requestId : String) {
        failedMessages.add(requestId)
        notifyDataSetChanged()
    }

    /* 전송 실패된 메시지를 failedMessages 리스트에 담는 Method */
    fun markMessageFailed(requestId: String) {
        failedMessages.add(requestId)
        notifyDataSetChanged()
    }

    /* 전송 실패된 메시지 삭제하는 Method */
    fun removeFailedMessage(message : BaseMessage) {
        if (message is UserMessage) {
            failedMessages.remove(message.requestId)
            messages.remove(message)
        }
        else if (message is FileMessage) {
            failedMessages.remove(message.requestId)
            messages.remove(message)
        }

        notifyDataSetChanged()
    }

    /**
     * 채팅 UI Scroll 시 이전 메시지 불러오는 Method
     * @param limit     로딩할 이전 메시지 갯수
     * @param handler   BaseChannel Handler
     */
    fun loadPreviousMessages(limit : Int, handler : BaseChannel.GetMessagesHandler?) {
        if (isMessageListLoading) {
            return
        }

        var oldestMessageCreated = Long.MAX_VALUE
        if (messages.size > 0) {
            oldestMessageCreated = messages[messages.size - 1].createdAt        // 로딩할 메시지가 있으면 해당 메시지의 timestamp를 Long type으로 세팅
        }

        /* 이전 메시지를 Timestamp 기준으로 가져옴 */
        isMessageListLoading = true
        mChannel!!.getPreviousMessagesByTimestamp(oldestMessageCreated, false, limit, true, BaseChannel.MessageTypeFilter.ALL, null, BaseChannel.GetMessagesHandler { list, e ->
            handler?.onResult(list, e)

            isMessageListLoading = false
            if (e != null) {
                e.printStackTrace()
                return@GetMessagesHandler
            }

            for (message in list) {
                messages.add(message)
            }

            notifyDataSetChanged()
        } )
    }

    /* 수정, 삭제 이후 Call되는 최근 메시지 불러오는 Method */
    fun loadLatestMessages(limit : Int, handler : BaseChannel.GetMessagesHandler?) {
        if (isMessageListLoading) {
            return
        }

        isMessageListLoading = true
        mChannel!!.getPreviousMessagesByTimestamp(Long.MAX_VALUE, true, limit, true, BaseChannel.MessageTypeFilter.ALL, null, BaseChannel.GetMessagesHandler { list, e ->
            handler?.onResult(list, e)

            isMessageListLoading = false
            if (e != null) {
                e.printStackTrace()
                return@GetMessagesHandler
            }

            if (list.size <= 0) {
                return@GetMessagesHandler
            }

            for (message in messages) {
                if (isTempMessage(message) || isFailedMessage(message)) {
                    list.add(0, message)
                }
            }

            messages.clear()

            for (message in list) {
                messages.add(message)
            }

            notifyDataSetChanged()
        })
    }

    fun markAllMessageAsRead() {
        if (mChannel != null) {
            mChannel!!.markAsRead()
        }
    }

    fun markMessageSend(message: BaseMessage) {
        var msg : BaseMessage

        for (i in messages.indices.reversed()) {
            msg = messages[i]

            if (message is UserMessage && msg is UserMessage) {
                if (msg.requestId == message.requestId) {
                    messages[i] = message
                    notifyDataSetChanged()
                    return
                }
            }
            else if (message is FileMessage && msg is FileMessage) {
                if (msg.requestId == message.requestId) {
                    mTempFileMessageUriTable.remove(message.requestId)
                    messages[i] = message
                    notifyDataSetChanged()
                    return
                }
            }
        }
    }

    fun setFileProgressPercent(message: FileMessage, percent: Int) {
        var msg: BaseMessage
        for (i in messages.indices.reversed()) {
            msg = messages[i]
            if (msg is FileMessage) {
                if (message.requestId == msg.requestId) {
                    val circleProgressBar = mFileMessageMap[message]
                    circleProgressBar!!.progress = percent
                    break
                }
            }
        }
    }

    fun addTempFileMessageInfo(message: FileMessage, uri: Uri) {
        mTempFileMessageUriTable[message.requestId] = uri
    }

    fun getTempFileMessageUri(message : BaseMessage) : Uri? {
        if (!isTempMessage(message)) {
            return null
        }

        return if (message is FileMessage) {
            null
        } else mTempFileMessageUriTable[message.requestId]
    }

    /**
     * @since   21.07.21
     * @param   CurrentMsg : 현재 메시지
     * @param   PrevMsg    : 이전 메시지
     * @return  Boolean
     *
     * 연속적인 메시지인지 확인
     * true -> Nickname과 ProfileImage View Invisible 처리
     * false -> Nickname과 ProfileImage View Visible 처리
     */
    private fun isContinuous(currentMsg : BaseMessage?, prevMsg : BaseMessage?) : Boolean {
        var currentUser : User? = null
        var precedingUser : User? = null

        if (currentMsg == null || prevMsg == null) {
            return false
        }

        if (currentMsg is AdminMessage && prevMsg is AdminMessage) {
            return true
        }

        if (currentMsg is UserMessage) {
            currentUser = currentMsg.sender
        }
        else if (currentMsg is FileMessage) {
            currentUser = currentMsg.sender
        }

        if (prevMsg is UserMessage) {
            precedingUser = prevMsg.sender
        }
        else if (prevMsg is FileMessage) {
            precedingUser = prevMsg.sender
        }

        return !(currentUser == null || precedingUser == null) && currentUser.userId == precedingUser.userId
    }

    fun setItemLongClickListener(listener : OnItemLongClickListener) {
        itemLongClickListener = listener
    }

    fun setItemClickListener(listener : OnItemClickListener) {
        itemClickListener = listener
    }

    /* 사용자가 보낸 UserMessage ViewHolder */
    private inner class MyUserMessageHolder (val binding : ListItemGroupChatUserMeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindViews(context : Context?, message : UserMessage, channel : GroupChannel?, isNewDay: Boolean, isTempMessage : Boolean, isContinuous: Boolean, isFailedMessage : Boolean, clickListener : OnItemClickListener?, longClickListener : OnItemLongClickListener?, position : Int) {
            setMessage(message)
            setChatTime(message)
            setChatDate(isNewDay, isContinuous, message)
            setUnreadCnt(isFailedMessage, isTempMessage, channel!!, message)
            setClickListener(message, position, clickListener, longClickListener)
        }

        fun setMessage(message: UserMessage) {
            binding.groupChannelChatMessageMeTV.text = message.message
        }

        fun setChatTime(message: UserMessage) {
            binding.groupChannelChatTimeMeTV.text = DateUtils.formatTime(message.createdAt)
        }

        fun setChatDate(isNewDay: Boolean, isContinuous: Boolean, message: UserMessage) {
            if (isNewDay) {
                binding.groupChannelChatDateMeTV.visibility = View.VISIBLE
                binding.groupChannelChatDateMeTV.text = DateUtils.formatDate(message.createdAt)
            }
            else binding.groupChannelChatDateMeTV.visibility = View.GONE

            if (isContinuous) {
                binding.groupChannelChatPadding.visibility = View.GONE
            } else binding.groupChannelChatPadding.visibility = View.VISIBLE
        }

        fun setUnreadCnt(isFailedMessage: Boolean, isTempMessage: Boolean, channel: GroupChannel, message: UserMessage) {
            if (isFailedMessage) {
                binding.groupChannelChatUnreadCntMeTV.text = "전송 실패"
                binding.groupChannelChatUnreadCntMeTV.visibility = View.VISIBLE
            }
            else if (isTempMessage) {
                binding.groupChannelChatUnreadCntMeTV.text = "전송 중"
                binding.groupChannelChatUnreadCntMeTV.visibility = View.VISIBLE
            }
            else {
                if (channel != null) {
                    val unreadCnt = channel.getUnreadMemberCount(message)

                    if (unreadCnt > 0) {
                        binding.groupChannelChatUnreadCntMeTV.text = unreadCnt.toString()
                        binding.groupChannelChatUnreadCntMeTV.visibility = View.VISIBLE
                    }
                    else binding.groupChannelChatUnreadCntMeTV.visibility = View.INVISIBLE
                }
            }
        }

        fun setClickListener(message : UserMessage, position : Int, clickListener: OnItemClickListener?, longClickListener: OnItemLongClickListener?) {
            if (clickListener != null) {
                binding.groupChannelChatMessageMeTV.setOnClickListener{ clickListener.onUserMessageClick(message)}
            }

            if (longClickListener != null) {
                binding.groupChannelChatMessageMeTV.setOnLongClickListener {
                    longClickListener.onUserMessageLongClick(message, position)
                    true
                }
            }
        }
    }

    /* 상대가 보내는 UserMessage ViewHolder */
    private inner class OtherUserMessageHolder(val binding: ListItemGroupChatUserOtherBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindViews(context: Context, userMessage: UserMessage, channel: GroupChannel?, isNewDay: Boolean, isContinuous: Boolean, clickListener : OnItemClickListener?, longClickListener : OnItemLongClickListener?, position: Int) {
            setMessage(userMessage)
            setChatTime(userMessage)
            setChatDate(isNewDay, userMessage)
            setUnreadCnt(userMessage, channel)
            setSenderProfile(context, isContinuous, userMessage)
            setClickListener(userMessage, position, clickListener, longClickListener)
        }

        private fun setSenderProfile(context: Context, isContinuous: Boolean, userMessage: UserMessage) {
            if (isContinuous) {
                binding.groupChannelChatProfileOtherIV.visibility = View.INVISIBLE
                binding.groupChannelChatNickNameOtherTV.visibility = View.INVISIBLE
            }
            else {
                binding.groupChannelChatProfileOtherIV.visibility = View.VISIBLE
                Glide.with(context)
                    .load(userMessage.sender.profileUrl)
                    .apply(RequestOptions().centerCrop())
                    .into(binding.groupChannelChatProfileOtherIV)

                binding.groupChannelChatNickNameOtherTV.visibility = View.VISIBLE
                binding.groupChannelChatNickNameOtherTV.text = userMessage.sender.nickname
            }
        }

        private fun setUnreadCnt(userMessage: UserMessage, channel: GroupChannel?) {
            if (channel != null) {
                val unreadCnt = channel.getUnreadMemberCount(userMessage)

                if (unreadCnt > 0) {
                    binding.groupChannelChatUnreadCntOtherTV.visibility = View.VISIBLE
                    binding.groupChannelChatUnreadCntOtherTV.text = unreadCnt.toString()
                }
                else binding.groupChannelChatUnreadCntOtherTV.visibility = View.INVISIBLE
            }
        }

        private fun setChatDate(isNewDay: Boolean, userMessage: UserMessage) {
            if (isNewDay) {
                binding.groupChannelChatDateOtherTV.visibility = View.VISIBLE
                binding.groupChannelChatDateOtherTV.text = DateUtils.formatDate(userMessage.createdAt)
            }
            else binding.groupChannelChatDateOtherTV.visibility = View.GONE
        }

        private fun setChatTime(userMessage: UserMessage) {
            binding.groupChannelChatTimeOtherTV.text = DateUtils.formatTime(userMessage.createdAt)
        }

        private fun setMessage(userMessage: UserMessage) {
            binding.groupChannelChatMessageOtherTV.text = userMessage.message
        }

        private fun setClickListener(userMessage: UserMessage, position: Int, clickListener: OnItemClickListener?, longClickListener: OnItemLongClickListener?) {
            if (clickListener != null) {
                binding.groupChannelChatMessageOtherTV.setOnClickListener { clickListener.onUserMessageClick(userMessage) }
            }

            if (longClickListener != null) {
                binding.groupChannelChatMessageOtherTV.setOnLongClickListener {
                    longClickListener.onUserMessageLongClick(userMessage, position)
                    true
                }
            }
        }
    }

    /* 사용자가 보낸 FileMessage ViewHolder */
    private inner class MyFileMessageHolder(val binding: ListItemGroupChatFileMeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindViews(context: Context?, fileMessage: FileMessage, channel: GroupChannel?, isNewDay: Boolean, isTempMessage: Boolean, isFailedMessage: Boolean, tempFileMessageUri: Uri?, listener : OnItemClickListener? ) {
            setFileName(fileMessage)
            setChatTime(fileMessage)
            setChatDate(isNewDay, fileMessage)
            setClickListener(listener, fileMessage)
            setUnreadCnt(isFailedMessage, isTempMessage, fileMessage, channel)
        }

        private fun setFileName(fileMessage: FileMessage) {
            binding.groupChannelChatFileNameMeTV.text = fileMessage.name
        }

        private fun setChatTime(fileMessage: FileMessage) {
            binding.groupChannelChatTimeMeTV.text = DateUtils.formatTime(fileMessage.createdAt)
        }

        private fun setChatDate(isNewDay: Boolean, fileMessage: FileMessage) {
            if (isNewDay) {
                binding.groupChannelChatDateMeTV.visibility = View.VISIBLE
                binding.groupChannelChatDateMeTV.text = DateUtils.formatDate(fileMessage.createdAt)
            }
            else {
                binding.groupChannelChatDateMeTV.visibility = View.GONE
            }
        }

        private fun setUnreadCnt(isFailedMessage: Boolean, isTempMessage: Boolean, fileMessage: FileMessage, channel: GroupChannel?) {
            if (isFailedMessage) {
                binding.groupChannelChatUnreadCntMeTV.text = "전송 실패"
                binding.groupChannelChatUnreadCntMeTV.visibility = View.VISIBLE


            }
            else if (isTempMessage) {
                binding.groupChannelChatUnreadCntMeTV.text = "전송 중"
                binding.groupChannelChatUnreadCntMeTV.visibility = View.INVISIBLE
            }
            else {
                if (channel != null) {
                    val unreadCnt = channel.getUnreadMemberCount(fileMessage)

                    if (unreadCnt > 0) {
                        binding.groupChannelChatUnreadCntMeTV.text = unreadCnt.toString()
                        binding.groupChannelChatUnreadCntMeTV.visibility = View.VISIBLE
                    }
                    else binding.groupChannelChatUnreadCntMeTV.visibility = View.INVISIBLE
                }
            }
        }

        private fun setClickListener(listener: OnItemClickListener?, fileMessage: FileMessage) {
            if (listener != null) {
                binding.groupChannelChatFileDownloadMeBtn.setOnClickListener { listener.onFileMessageClick(fileMessage) }
            }
        }
    }

    /* 상대가 보내는 FileMessage ViewHolder */
    private inner class OtherFileMessageHolder(val binding: ListItemGroupChatFileOtherBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindViews(context: Context, fileMessage: FileMessage, channel: GroupChannel?, isNewDay: Boolean, isContinuous: Boolean, listener: OnItemClickListener?) {
            setFileName(fileMessage)
            setChatTime(fileMessage)
            setChatDate(isNewDay, fileMessage)
            setUnreadCnt(channel, fileMessage)
            setSenderProfile(context, isContinuous, fileMessage)
            setClickListener(listener, fileMessage)
        }

        private fun setFileName(fileMessage: FileMessage) {
            binding.groupChannelChatFileNameOtherTV.text = fileMessage.name
        }

        private fun setChatTime(fileMessage: FileMessage) {
            binding.groupChannelChatTimeOtherTV.text = DateUtils.formatTime(fileMessage.createdAt)
        }

        private fun setChatDate(isNewDay: Boolean, fileMessage: FileMessage) {
            if (isNewDay) {
                binding.groupChannelChatDateOtherTV.visibility = View.VISIBLE
                binding.groupChannelChatDateOtherTV.text = DateUtils.formatDate(fileMessage.createdAt)
            }
            else {
                binding.groupChannelChatDateOtherTV.visibility = View.GONE
            }
        }

        private fun setUnreadCnt(channel: GroupChannel?, fileMessage: FileMessage) {
            if (channel != null) {
                val unreadCnt = channel.getUnreadMemberCount(fileMessage)

                if (unreadCnt > 0) {
                    binding.groupChannelChatUnreadCntOtherTV.text = unreadCnt.toString()
                    binding.groupChannelChatUnreadCntOtherTV.visibility = View.VISIBLE
                }
                else binding.groupChannelChatUnreadCntOtherTV.visibility = View.INVISIBLE
            }
        }

        private fun setSenderProfile(context: Context, isContinuous: Boolean, fileMessage: FileMessage) {
            if (isContinuous) {
                binding.groupChannelChatProfileOtherIV.visibility = View.INVISIBLE
                binding.groupChannelChatNickNameOtherTV.visibility = View.INVISIBLE
            }
            else {
                binding.groupChannelChatProfileOtherIV.visibility = View.VISIBLE
                Glide.with(context)
                    .load(fileMessage.sender.profileUrl)
                    .apply(RequestOptions().centerCrop())
                    .into(binding.groupChannelChatProfileOtherIV)

                binding.groupChannelChatNickNameOtherTV.visibility = View.VISIBLE
                binding.groupChannelChatNickNameOtherTV.text = fileMessage.sender.nickname
            }
        }

        private fun setClickListener(listener: OnItemClickListener?, fileMessage: FileMessage) {
            if (listener != null) {
                binding.groupChannelChatFileDownloadOtherBtn.setOnClickListener { listener.onFileMessageClick(fileMessage) }
            }
        }
    }

    /* 사용자가 보낸 FileMessage (Image) ViewHolder */
    private inner class MyImageFileMessageHolder(val binding: ListItemGroupFileImageMessageMeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindViews(context: Context, fileMessage: FileMessage, channel: GroupChannel?, isNewDay: Boolean, isTempMessage: Boolean, isFailedMessage: Boolean, tempFileMessageUri: Uri?, listener: OnItemClickListener?) {
            setChatTime(fileMessage)
            setChatDate(isNewDay, fileMessage)
            setUnreadCnt(channel, fileMessage, isFailedMessage, isTempMessage)
            setImage(context, fileMessage, isTempMessage, tempFileMessageUri)
            setListener(fileMessage, listener)
        }

        private fun setChatTime(fileMessage: FileMessage) {
            binding.groupChannelChatTimeMeTV.text = DateUtils.formatTime(fileMessage.createdAt)
        }

        private fun setChatDate(isNewDay: Boolean, fileMessage: FileMessage) {
            if (isNewDay) {
                binding.groupChannelChatDateMeTV.visibility = View.VISIBLE
                binding.groupChannelChatDateMeTV.text = DateUtils.formatDate(fileMessage.createdAt)
            } else binding.groupChannelChatDateMeTV.visibility = View.GONE
        }

        private fun setUnreadCnt(channel: GroupChannel?, fileMessage: FileMessage, isFailedMessage: Boolean, isTempMessage: Boolean) {
            if (isFailedMessage) {
                binding.groupChannelChatUnreadCntMeTV.text = "전송 실패"
                binding.groupChannelChatUnreadCntMeTV.visibility = View.VISIBLE



            }
            else if (isTempMessage) {
                binding.groupChannelChatUnreadCntMeTV.text = "전송 중"
                binding.groupChannelChatUnreadCntMeTV.visibility = View.GONE


            }
            else {

                if (channel != null) {
                    val unreadCnt = channel.getUnreadMemberCount(fileMessage)

                    if (unreadCnt > 0) {
                        binding.groupChannelChatUnreadCntMeTV.text = unreadCnt.toString()
                        binding.groupChannelChatUnreadCntMeTV.visibility = View.VISIBLE
                    }
                    else binding.groupChannelChatUnreadCntMeTV.visibility = View.INVISIBLE
                }
            }
        }

        private fun setImage(context: Context, fileMessage: FileMessage, isTempMessage: Boolean, tempFileMessageUri: Uri?) {
            val fileThumbnailImage = binding.groupChannelChatImageMeIV
            if (isTempMessage && tempFileMessageUri != null) {

                ImageUtils.displayImageFromUrl(context, tempFileMessageUri.toString(), fileThumbnailImage, null)
            }
            else {
                val thumbnails = fileMessage.thumbnails as ArrayList<FileMessage.Thumbnail>

                if (thumbnails.size > 0) {
                    if (fileMessage.type.lowercase().contains("gif")) {
                        ImageUtils.displayGifImageFromUrl(context, fileMessage.url, fileThumbnailImage, thumbnails[0].url, fileThumbnailImage.drawable)
                    }
                    else ImageUtils.displayImageFromUrl(context, thumbnails[0].url, fileThumbnailImage, fileThumbnailImage.drawable)
                }
                else {
                    if (fileMessage.type.lowercase().contains("gif")) {
                        ImageUtils.displayGifImageFromUrl(context, fileMessage.url, fileThumbnailImage, null as String?, fileThumbnailImage.drawable)
                    }
                    else {
                        ImageUtils.displayImageFromUrl(context, fileMessage.url, fileThumbnailImage, fileThumbnailImage.drawable)
                    }
                }
            }

        }

        private fun setListener(fileMessage: FileMessage, listener: OnItemClickListener?) {
            if (listener != null) {
                binding.groupChannelChatMeCV.setOnClickListener { listener.onFileMessageClick(fileMessage) }
            }
        }
    }

    /* 상대가 보낸 FileMessage (Image) ViewHolder */
    private inner class OtherImageFileMessageHolder(val binding: ListItemGroupFileImageMessageOtherBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindViews(context: Context, fileMessage: FileMessage, channel: GroupChannel?, isNewDay: Boolean, isContinuous: Boolean, listener : OnItemClickListener?) {
            setChatTime(fileMessage)
            setChatDate(isNewDay, fileMessage)
            setUnreadCnt(channel, fileMessage)
            setImage(context, fileMessage)
            setSenderProfile(context, isContinuous, fileMessage)
            setListener(listener, fileMessage)
        }

        private fun setChatTime(fileMessage: FileMessage) {
            binding.groupChannelChatTimeOtherTV.text = DateUtils.formatTime(fileMessage.createdAt)
        }

        private fun setChatDate(isNewDay: Boolean, fileMessage: FileMessage) {
            if (isNewDay) {
                binding.groupChannelChatDateOtherTV.visibility = View.VISIBLE
                binding.groupChannelChatDateOtherTV.text = DateUtils.formatDate(fileMessage.createdAt)
            } else binding.groupChannelChatDateOtherTV.visibility = View.GONE
        }

        private fun setUnreadCnt(channel: GroupChannel?, fileMessage: FileMessage) {
            if (channel != null) {
                val unreadCnt = channel.getUnreadMemberCount(fileMessage)

                if (unreadCnt > 0) {
                    binding.groupChannelChatUnreadCntOtherTV.text = unreadCnt.toString()
                    binding.groupChannelChatUnreadCntOtherTV.visibility = View.VISIBLE
                }
                else binding.groupChannelChatUnreadCntOtherTV.visibility = View.INVISIBLE
            }
        }

        private fun setImage(context: Context, fileMessage: FileMessage) {
            val thumbnails = fileMessage.thumbnails as ArrayList<FileMessage.Thumbnail>
            val fileThumbnailImage = binding.groupChannelChatImageOtherIV

            if (thumbnails.size > 0) {
                if (fileMessage.type.toLowerCase().contains("gif")) {
                    ImageUtils.displayGifImageFromUrl(context, fileMessage.url, fileThumbnailImage, thumbnails[0].url, fileThumbnailImage.drawable)
                } else {
                    ImageUtils.displayImageFromUrl(context, thumbnails[0].url, fileThumbnailImage, fileThumbnailImage.drawable)
                }
            } else {
                if (fileMessage.type.toLowerCase().contains("gif")) {
                    ImageUtils.displayGifImageFromUrl(context, fileMessage.url, fileThumbnailImage, null as String?, fileThumbnailImage.drawable)
                } else {
                    ImageUtils.displayImageFromUrl(context, fileMessage.url, fileThumbnailImage, fileThumbnailImage.drawable)
                }
            }
        }

        private fun setSenderProfile(context: Context, isContinuous: Boolean, fileMessage: FileMessage) {
            if (isContinuous) {
                binding.groupChannelChatProfileOtherIV.visibility = View.INVISIBLE
                binding.groupChannelChatNickNameOtherTV.visibility = View.INVISIBLE
            }
            else {
                binding.groupChannelChatProfileOtherIV.visibility = View.VISIBLE
                Glide.with(context)
                    .load(fileMessage.sender.profileUrl)
                    .apply(RequestOptions().centerCrop())
                    .into(binding.groupChannelChatProfileOtherIV)

                binding.groupChannelChatNickNameOtherTV.visibility = View.VISIBLE
                binding.groupChannelChatNickNameOtherTV.text = fileMessage.sender.nickname
            }
        }

        private fun setListener(listener: OnItemClickListener?, fileMessage: FileMessage) {
            if (listener != null) {
                binding.groupChannelChatOtherCV.setOnClickListener { listener.onFileMessageClick(fileMessage) }
            }
        }
    }

    /* AdminMessage ViewHolder */
    private inner class AdminMessageHolder(val binding : ListItemGroupChatAdminMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindViews(context: Context, adminMessage: AdminMessage, channel: GroupChannel?, isNewDay: Boolean) {
            setMessage(adminMessage)
            setChatDate(isNewDay, adminMessage)
        }

        private fun setMessage(adminMessage: AdminMessage) {
            binding.groupChannelChatAdminMessageTV.text = adminMessage.message
        }

        private fun setChatDate(isNewDay: Boolean, adminMessage: AdminMessage) {
            if (isNewDay) {
                binding.groupChannelChatDateAdminTV.visibility = View.INVISIBLE
                binding.groupChannelChatDateAdminTV.text = DateUtils.formatDate(adminMessage.createdAt)
            }
            else binding.groupChannelChatDateAdminTV.visibility = View.GONE
        }
    }
}