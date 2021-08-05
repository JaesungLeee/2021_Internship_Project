package kr.co.iboss.chat.UI.GroupChannel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.sendbird.android.*
import kr.co.iboss.chat.UI.GroupChannel.Adapter.GroupChatAdapter
import kr.co.iboss.chat.UI.MainActivity
import kr.co.iboss.chat.UI.Media.PhotoViewActivity
import kr.co.iboss.chat.UI.Media.VideoViewActivity
import kr.co.iboss.chat.Utils.ConnectionUtils
import kr.co.iboss.chat.Utils.FileUtils
import kr.co.iboss.chat.databinding.ActivityGroupChatBinding
import java.io.File

class GroupChatActivity : AppCompatActivity() {

    private val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"
    private val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHAT"
    private val CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT"
    private val STATE_CHANNEL_URL = "STATE_CHANNEL_URL"

    private val CHANNEL_LIST_LIMIT = 20
    private val INTENT_REQUEST_CHOOSE_MEDIA = 301
    private val PERMISSION_WRITE_EXTERNAL_STORAGE = 13
    private val STATE_NORMAL = 0
    private val STATE_EDIT = 1

    private lateinit var adapter : GroupChatAdapter
    private lateinit var recyclerView : RecyclerView
    private lateinit var mChannelURL : String

    private var mFileProgressHandlerMap: HashMap<BaseChannel.SendFileMessageWithProgressHandler, FileMessage>? = null
    private var mIMM : InputMethodManager? = null
    private var mCurrentState = STATE_NORMAL
    private var mEditingMessage: BaseMessage? = null
    private var mChannel : GroupChannel? = null

    private lateinit var binding : ActivityGroupChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mIMM = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mFileProgressHandlerMap = HashMap<BaseChannel.SendFileMessageWithProgressHandler, FileMessage>()

        mChannelURL = if (savedInstanceState != null) {
            savedInstanceState.getString(STATE_CHANNEL_URL).toString()
        } else {
            intent.getStringExtra(EXTRA_CHANNEL_URL).toString()
        }

        Log.e("URL", mChannelURL)


        setUpRecyclerView()
        setUpChatListAdapter()

        textListener()
        buttonHandler()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_CHANNEL_URL, mChannelURL)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        ConnectionUtils.removeConnectionManagementHandler(CONNECTION_HANDLER_ID)
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID)
        Log.e("ONPAUSE", "Done")
    }

    override fun onResume() {
        super.onResume()
        Log.e("ONRESUME", mChannelURL)

        ConnectionUtils.addConnectionManagementHandler(CONNECTION_HANDLER_ID, object : ConnectionUtils.ConnectionManagementHandler {
            override fun onConnected(reconnect: Boolean) {
                refresh()
            }

        })

        adapter.setContext(this)

        mChannelURL = getChannelURL()



        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, object : SendBird.ChannelHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMesage: BaseMessage) {
                if (baseChannel.url == mChannelURL) {
                    adapter.addFirst(baseMesage)
                    mChannel!!.markAsRead()
                }
            }

            override fun onMessageDeleted(baseChannel: BaseChannel?, msgId: Long) {
                super.onMessageDeleted(baseChannel, msgId)
                if (baseChannel!!.url == mChannelURL) {
                    adapter.delete(msgId)
                }
            }

            override fun onMessageUpdated(baseChannel: BaseChannel?, message: BaseMessage?) {
                super.onMessageUpdated(baseChannel, message)
                if (baseChannel!!.url == mChannelURL) {
                    adapter.update(message!!)
                }
            }

            override fun onReadReceiptUpdated(channel: GroupChannel?) {
                if (channel!!.url == mChannelURL) {
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        SendBird.setAutoBackgroundDetection(true)

        if (requestCode == INTENT_REQUEST_CHOOSE_MEDIA && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return
            }

            sendFileWithThumbnail(data.data)
        }
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

                onFileMessageClicked(message)
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
                if (inputmessage.isNotEmpty()) {
                    sendUserMessages(inputmessage)
                    binding.groupChannelMessageET.text.clear()

                }
            }
        }

        binding.groupChannelUploadContentsBtn.setOnClickListener { requestMedia() }
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
            GroupChannel.getChannel(mChannelURL) { groupChannel, e ->
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

    private fun requestMedia() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestSelfPermissions()
        }
        else {
            val intent = Intent()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.type = "*/*"

                val mediaType = arrayOf("image/*", "video/*")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mediaType)
            }
            else {
                intent.type = "image/* video/*"
            }

            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(Intent.createChooser(intent, "전송할 파일을 고르세요"), INTENT_REQUEST_CHOOSE_MEDIA)

            SendBird.setAutoBackgroundDetection(false)
        }
    }

    private fun requestSelfPermissions() {
        // 권한 승인을 거절 했을 때
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(binding.layoutGroupChatRoot, "파일 업로드 및 다운로드를 위해 저장소 접근 권한이 필요합니다.", Snackbar.LENGTH_LONG)
                .setAction("확인") {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_WRITE_EXTERNAL_STORAGE)
                }
                .show()
        }
        else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_WRITE_EXTERNAL_STORAGE)
        }
    }

    /**
     * @since 21.08.05
     *
     * File Message 전송 Method
     * : 이미지 Thumbnail 메시지를 사이즈에 맞게 보냄
     */
    private fun sendFileWithThumbnail(uri : Uri?) {
        val thumbnailSizes = ArrayList<FileMessage.ThumbnailSize>()
        thumbnailSizes.add(FileMessage.ThumbnailSize(240, 240))
        thumbnailSizes.add(FileMessage.ThumbnailSize(320, 320))

        val info = FileUtils.getFileInfo(this, uri!!)

        if (info == null) {
            Toast.makeText(this, "파일 불러오기에 실패 하였습니다.", Toast.LENGTH_LONG).show()
            return
        }

        val path = info["path"] as String
        val file = File(path)
        val name = file.name
        val mime = info["mime"] as String
        val size = info["size"] as Int

        if (path == "") {
            Toast.makeText(this, "저장소에 존재하지 않는 파일입니다.", Toast.LENGTH_LONG).show()
        }
        else {
            val progressHandler = object : BaseChannel.SendFileMessageWithProgressHandler {
                override fun onProgress(byteSent: Int, totalBytesSent: Int, totalBytesToSend: Int) {
                    val fileMessage = mFileProgressHandlerMap!![this]
                    if (fileMessage != null && totalBytesToSend > 0) {
                        val percent = totalBytesSent * 100 / totalBytesToSend
                        adapter.setFileProgressPercent(fileMessage, percent)
                    }
                }

                override fun onSent(fileMessage: FileMessage, e: SendBirdException?) {
                    if (e != null) {
                        Toast.makeText(this@GroupChatActivity, "에러 ${e.code} : ${e.message}", Toast.LENGTH_SHORT).show()
                        adapter.markMessageFailed(fileMessage.requestId)
                    }

                    adapter.markMessageSend(fileMessage)
                }

            }

            val tempFileMessage = mChannel!!.sendFileMessage(file, name, mime, size, "", null, thumbnailSizes, progressHandler)

            mFileProgressHandlerMap!![progressHandler] = tempFileMessage

            adapter.addTempFileMessageInfo(tempFileMessage, uri)
            adapter.addFirst(tempFileMessage)
        }
    }

    private fun onFileMessageClicked(message : FileMessage) {
        val type = message.type.lowercase()

        if (type.startsWith("image")) {
            val intent = Intent(this, PhotoViewActivity::class.java).apply {
                putExtra("url", message.url)
                putExtra("type", message.type)
            }
            startActivity(intent)
        }
        else if (type.startsWith("video")) {
            val intent = Intent(this, VideoViewActivity::class.java)
            intent.putExtra("url", message.url)
            startActivity(intent)
        }
        else showDownloadConfirmDialog(message)
    }

    private fun showDownloadConfirmDialog(message : FileMessage) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestSelfPermissions()
        }
        else {
            AlertDialog.Builder(this)
                .setMessage("파일을 다운로드 하시겠습니까?")
                .setPositiveButton("네") { dialog, which ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        FileUtils.downloadFile(this, message.url, message.name)
                    }
                }
                .setNegativeButton("아니오", null).show()
        }
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