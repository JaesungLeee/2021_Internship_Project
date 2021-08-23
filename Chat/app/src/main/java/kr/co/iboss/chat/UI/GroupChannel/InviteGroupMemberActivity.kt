package kr.co.iboss.chat.UI.GroupChannel

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird
import com.sendbird.android.User
import com.sendbird.android.UserListQuery
import kr.co.iboss.chat.UI.GroupChannel.Adapter.InviteGroupMemberAdapter
import kr.co.iboss.chat.databinding.ActivityInviteGroupMemberBinding

class InviteGroupMemberActivity : AppCompatActivity() {
    companion object {
        private val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"
        private val MEMBERS_LIMIT_COUNT = 15
    }

    private lateinit var binding : ActivityInviteGroupMemberBinding

    private var mAdapter : InviteGroupMemberAdapter? = null
    private var mChannelURL : String? = null

    private var mMemberListQuery : UserListQuery? = null
    private var mSelectedUserIdList : MutableList<String>? = null


    init {
        this.mSelectedUserIdList = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInviteGroupMemberBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mChannelURL = intent.getStringExtra(EXTRA_CHANNEL_URL)

        setUpAdapter()
        setUpRecyclerView()
        loadFirstUserList(MEMBERS_LIMIT_COUNT)
        buttonListener()
    }

    private fun setUpAdapter() {
        mAdapter = InviteGroupMemberAdapter(this, false, true)
        mAdapter!!.setCheckBoxStateListener(object : InviteGroupMemberAdapter.OnItemCheckedStateListener {
            override fun onItemChecked(user: User, isChecked: Boolean) {
                if (isChecked) mSelectedUserIdList!!.add(user.userId)
                else mSelectedUserIdList!!.remove(user.userId)

                // 선택된 사람이 있을 때 초대버튼 활성화
                binding.groupMemberInviteBtn.isEnabled = mSelectedUserIdList!!.size > 0
            }
        })
    }

    private fun setUpRecyclerView() {
        val mLayoutManager = LinearLayoutManager(this)
        binding.inviteGroupMemberMemberListRV.layoutManager = mLayoutManager
        binding.inviteGroupMemberMemberListRV.adapter = mAdapter
        binding.inviteGroupMemberMemberListRV.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        binding.inviteGroupMemberMemberListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (mLayoutManager.findLastVisibleItemPosition() == mAdapter!!.itemCount - 1) {
                    loadNextUserList(10)
                }
            }
        })
    }

    private fun buttonListener() {
        binding.groupMemberInviteNavigateBeforeBtn.setOnClickListener {
            finish()
        }
        binding.groupMemberInviteBtn.setOnClickListener {
            if (mSelectedUserIdList!!.size > 0) inviteGroupMemberWithUserId()
            else Toast.makeText(this, "초대할 멤버를 선택해주세요.", Toast.LENGTH_LONG).show()
        }
        binding.groupMemberInviteBtn.isEnabled = false
    }

    private fun inviteGroupMemberWithUserId() {
        GroupChannel.getChannel(mChannelURL, GroupChannel.GroupChannelGetHandler { groupChannel, e ->
            if (e != null) {
                return@GroupChannelGetHandler
            }

            groupChannel.inviteWithUserIds(mSelectedUserIdList, GroupChannel.GroupChannelInviteHandler { e ->
                if (e != null) {
                    return@GroupChannelInviteHandler
                }

                finish()
            })
        })
    }
    private fun loadFirstUserList(size: Int) {
        mMemberListQuery = SendBird.createUserListQuery()

        mMemberListQuery!!.setLimit(size)
        mMemberListQuery!!.next(UserListQuery.UserListQueryResultHandler { list, e ->
            if (e != null) {
                return@UserListQueryResultHandler
            }

            mAdapter!!.setUserList(list)
        })
    }

    private fun loadNextUserList(size : Int) {
        mMemberListQuery!!.setLimit(size)

        mMemberListQuery!!.next(UserListQuery.UserListQueryResultHandler { list, e ->
            if (e != null) {
                return@UserListQueryResultHandler
            }

            for (user in list) {
                mAdapter!!.addLast(user)
            }
        })
    }



}