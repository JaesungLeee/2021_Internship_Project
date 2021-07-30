package kr.co.iboss.chat.UI.GroupChannel

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.*
import kr.co.iboss.chat.R
import kr.co.iboss.chat.UI.GroupChannel.Adapter.GroupChatAdapter
import kr.co.iboss.chat.UI.MainActivity
import kr.co.iboss.chat.Utils.ConnectionUtils
import kr.co.iboss.chat.databinding.ActivityGroupChatBinding

class GroupChatActivity : AppCompatActivity() {

    private val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"
    private val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHAT"
    private val CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT"
    private val CHANNEL_LIST_LIMIT = 20

    private val STATE_NORMAL = 0
    private val STATE_EDIT = 1

    private lateinit var adapter : GroupChatAdapter
    private lateinit var recyclerView : RecyclerView
    private lateinit var channelURL : String

    private lateinit var binding : ActivityGroupChatBinding

    private var mIMM : InputMethodManager? = null
    private var mCurrentState = STATE_NORMAL
    private var mEditingMessage: BaseMessage? = null
    private var mChannel : GroupChannel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mIMM = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        channelURL = intent.getStringExtra(EXTRA_CHANNEL_URL).toString()
        Log.e("URL", channelURL)


        setUpRecyclerView()
        setUpChatListAdapter()

        textListener()
        buttonHandler()

    }

    override fun onPause() {
        super.onPause()
        ConnectionUtils.removeConnectionManagementHandler(CONNECTION_HANDLER_ID)
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID)
        Log.e("ONPAUSE", "Done")
    }

    override fun onResume() {
        super.onResume()
        Log.e("ONRESUME", channelURL)

        ConnectionUtils.addConnectionManagementHandler(CONNECTION_HANDLER_ID, object : ConnectionUtils.ConnectionManagementHandler {
            override fun onConnected(reconnect: Boolean) {
                refresh()
            }

        })

        adapter.setContext(this)

        channelURL = getChannelURL()



        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, object : SendBird.ChannelHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMesage: BaseMessage) {
                if (baseChannel.url == channelURL) {
                    adapter.addFirst(baseMesage)
                    mChannel!!.markAsRead()
                }
            }

            override fun onMessageDeleted(baseChannel: BaseChannel?, msgId: Long) {
                super.onMessageDeleted(baseChannel, msgId)
                if (baseChannel!!.url == channelURL) {
                    adapter.delete(msgId)
                }
            }

            override fun onMessageUpdated(baseChannel: BaseChannel?, message: BaseMessage?) {
                super.onMessageUpdated(baseChannel, message)
                if (baseChannel!!.url == channelURL) {
                    adapter.update(message!!)
                }
            }

            override fun onReadReceiptUpdated(channel: GroupChannel?) {
                if (channel!!.url == channelURL) {
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun setUpRecyclerView() {
        adapter = GroupChatAdapter(this)
        recyclerView = binding.groupChannelChatRV
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        recyclerView.layoutManager = layoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (layoutManager.findLastVisibleItemPosition() == adapter.itemCount - 1) {
                    adapter.loadPreviousMessages(CHANNEL_LIST_LIMIT, null)
                }
            }
        })
    }

    private fun setUpChatListAdapter() {
        adapter.setItemClickListener(object : GroupChatAdapter.OnItemClickListener {
            override fun onUserMessageClick(message: UserMessage) {
                if (adapter.isFailedMessage(message)) {
                    retryFailedMessage(message)
                    return
                }

                if (adapter.isTempMessage(message)) {
                    return
                }
            }

            override fun onFileMessageClick(message: FileMessage) {
                if (adapter.isFailedMessage(message)) {
                    retryFailedMessage(message)
                    return
                }

                if (adapter.isTempMessage(message)) {
                    return
                }

                onFileMessageClick(message)
            }
        })


        adapter.setItemLongClickListener(object : GroupChatAdapter.OnItemLongClickListener {
            override fun onUserMessageLongClick(message: UserMessage, position: Int) {
                showMessageOptionDialog(message, position)
            }

            override fun onFileMessageLongClick(message: FileMessage) {

            }
        })
    }

    private fun textListener() {
        binding.groupChannelMessageET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {  }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {  }

            override fun afterTextChanged(editText: Editable?) {
                if (editText != null) {
                    binding.groupChannelSendMessageBtn.isEnabled = editText.isNotEmpty()
                }
            }
        })
    }

    private fun buttonHandler() {
        binding.groupChannelNavigateBeforeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.groupChannelSendMessageBtn.isEnabled = false
        binding.groupChannelSendMessageBtn.setOnClickListener {
            if (mCurrentState == STATE_EDIT) {
                val inputMessage = binding.groupChannelMessageET.text.toString()
                if (mEditingMessage != null) {
                    editMessage(mEditingMessage, inputMessage)
                }
            }
            else {
                val inputmessage = binding.groupChannelMessageET.text.toString()
                if (inputmessage.length > 0) {
                    sendUserMessages(inputmessage)
                    binding.groupChannelMessageET.text.clear()

                }
            }
        }
    }

    private fun showMessageOptionDialog(message : BaseMessage, position: Int) {
        val dialogOptions = arrayOf("수정하기", "삭제하기")

        val builder = AlertDialog.Builder(this)
        builder.setItems(dialogOptions) {dialog, idx ->
            if (idx == 0) {
                setState(STATE_EDIT, message, position)
            }
            else if (idx == 1) {
                deleteMessage(message)
            }
        }

        builder.create().show()
    }

    private fun retryFailedMessage(message: BaseMessage) {
        AlertDialog.Builder(this)
            .setMessage("메시지 전송에 실패 하였습니다.")
            .setPositiveButton("다시 보내기") { dialog, idx ->
                if (idx == DialogInterface.BUTTON_POSITIVE) {
                    if (message is UserMessage) {
                        val inputMessage = message.message
                        sendUserMessages(inputMessage)
                    }
                    else if (message is FileMessage) {
                        val uri = adapter.getTempFileMessageUri(message)
                        sendFileWithThumbnail(uri)
                    }
                    adapter.removeFailedMessage(message)
                }
            }
            .setNegativeButton("전송 취소하기") { dialog, idx ->
                if (idx == DialogInterface.BUTTON_NEGATIVE) {
                    adapter.removeFailedMessage(message)
                }
            }.show()
    }

    private fun setState(state : Int, editMessage : BaseMessage, position: Int) {
        when (state) {
            STATE_NORMAL -> {
                mCurrentState = STATE_NORMAL
                binding.groupChannelUploadContentsBtn.visibility = View.VISIBLE
                binding.groupChannelMessageET.setText("")
            }

            STATE_EDIT -> {
                mCurrentState = STATE_EDIT
                mEditingMessage = editMessage

                binding.groupChannelUploadContentsBtn.visibility = View.INVISIBLE

                var message = (editMessage as UserMessage).message
                if (message == null) {
                    message = ""
                }

                binding.groupChannelMessageET.setText(message)
                if (message.length > 0) {
                    binding.groupChannelMessageET.setSelection(0, message.length)
                }

                binding.groupChannelMessageET.requestFocus()
                binding.groupChannelMessageET.postDelayed({
                    mIMM!!.showSoftInput(binding.groupChannelMessageET, 0)
                    recyclerView.postDelayed({
                        recyclerView.scrollToPosition(position)
                    }, 500)
                }, 100)
            }
        }
    }

    private fun refresh() {
        if (mChannel == null) {
            GroupChannel.getChannel(channelURL) { groupChannel, e ->
                if (e != null) {
                    e.printStackTrace()
                    return@getChannel
                }

                mChannel= groupChannel
                adapter.setChannel(mChannel!!)
                adapter.loadLatestMessages(CHANNEL_LIST_LIMIT, BaseChannel.GetMessagesHandler { list, e ->
                    adapter.markAllMessageAsRead()
                })

            }
        }
        else {
            mChannel!!.refresh(GroupChannel.GroupChannelRefreshHandler { e ->
                if (e != null) {
                    e.printStackTrace()
                    return@GroupChannelRefreshHandler
                }

                adapter.loadLatestMessages(CHANNEL_LIST_LIMIT, BaseChannel.GetMessagesHandler { list, e ->
                    adapter.markAllMessageAsRead()
                })
            })
        }
    }

    private fun sendUserMessages(text: String) {
        val tempUserMessage = mChannel!!.sendUserMessage(text, BaseChannel.SendUserMessageHandler { userMessage, e ->
            if (e != null) {
                Log.e("SENDMESSAGE", "SEND_FAILED")
                return@SendUserMessageHandler
            }

            adapter.markMessageSend(userMessage)
        })

        adapter.addFirst(tempUserMessage)
    }

    private fun sendFileWithThumbnail(uri : Uri?) {
        val thumbnailSizes = ArrayList<FileMessage.ThumbnailSize>()
        thumbnailSizes.add(FileMessage.ThumbnailSize(240, 240))
        thumbnailSizes.add(FileMessage.ThumbnailSize(320, 320))


    }

//    private fun getMessages() {
//        val previousMessageListQuery = mChannel!!.createPreviousMessageListQuery()
//
//        previousMessageListQuery.load(100, true) { messages, e ->
//            if (e != null) {
//                Log.e("GETMESSAGE", "LOAD_FAILED")
//                return@load
//            }
//
//            adapter.loadMessages(messages)
//        }
//    }

    private fun editMessage(message: BaseMessage?, editedMessage : String) {
        mChannel!!.updateUserMessage(message!!.messageId, editedMessage, null, null, BaseChannel.UpdateUserMessageHandler { userMessage, e ->
            if (e != null) {
                Toast.makeText(this, "Error ${e.code} : ${e.message}", Toast.LENGTH_LONG).show()
                return@UpdateUserMessageHandler
            }

            adapter.loadLatestMessages(CHANNEL_LIST_LIMIT, BaseChannel.GetMessagesHandler { list, e ->
                adapter.markAllMessageAsRead()
            })
        })
    }

    private fun deleteMessage(message: BaseMessage) {
        mChannel!!.deleteMessage(message, BaseChannel.DeleteMessageHandler { e ->
            if (e != null) {
                Toast.makeText(this, "Error ${e.code} : ${e.message}", Toast.LENGTH_LONG).show()
                return@DeleteMessageHandler
            }

            adapter.loadLatestMessages(CHANNEL_LIST_LIMIT, BaseChannel.GetMessagesHandler { list, e ->
                adapter.markAllMessageAsRead()
            })
        })
    }

    private fun getChannelURL(): String {
        val intent = this.intent
        return intent.getStringExtra(EXTRA_CHANNEL_URL)!!
    }
}