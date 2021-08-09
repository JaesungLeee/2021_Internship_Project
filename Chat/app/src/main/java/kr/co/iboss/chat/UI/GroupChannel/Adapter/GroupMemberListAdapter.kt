package kr.co.iboss.chat.UI.GroupChannel.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.Member
import com.sendbird.android.SendBird
import com.sendbird.android.User
import kr.co.iboss.chat.UI.GroupChannel.GroupMemberInfoActivity
import kr.co.iboss.chat.Utils.ImageUtils
import kr.co.iboss.chat.databinding.ListItemGroupChatMemberListBinding

class GroupMemberListAdapter(private val mContext : Context, private val mChannelURL : String, private val isGroupChannel : Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"
        private val EXTRA_USER_ID = "EXTRA_USER_ID"
        private val EXTRA_USER_PROFILE_URL = "EXTRA_USER_PROFILE_URL"
        private val EXTRA_USER_NICKNAME = "EXTRA_USER_NICKNAME"
        private val EXTRA_USER_BLOCKED_BY_ME = "EXTRA_USER_BLOCKED_BY_ME"
    }

    private val mUserList : MutableList<User>
    private val mMember : Member? = null

    init {
        mUserList = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListItemGroupChatMemberListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupMemberHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as GroupMemberHolder).bindViews(mContext, holder, mUserList[position])
    }

    override fun getItemCount(): Int {
        return mUserList.size
    }

    fun setMemberList(members : List<User>) {
        mUserList.clear()
        mUserList.addAll(members)
        notifyDataSetChanged()
    }

    fun addMemberLast(member : User) {
        mUserList.add(member)
        notifyDataSetChanged()
    }

    private inner class GroupMemberHolder (val binding : ListItemGroupChatMemberListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindViews(context : Context, holder : GroupMemberHolder, user : User) {
            setMemberProfileImage(context, user)
            setMemberNickName(user)
            setBlocked(user)

        }
        fun setMemberProfileImage(context: Context, user: User) {
            ImageUtils.displayRoundImageFromUrl(context, user.profileUrl, binding.groupChatMemberProfileIV)
        }

        fun setMemberNickName(user: User) {
            binding.groupChatMemberNickNameTV.text = user.nickname
        }

        fun setBlocked(user: User) {
            if (isGroupChannel) {
                if (SendBird.getCurrentUser().userId == user.userId) {
                    binding.groupChatMemberBlockedContainer.visibility = View.GONE
                    binding.groupChatMemberBlockedTV.visibility = View.GONE
                }
                else {
                    binding.groupChatMemberBlockedContainer.visibility = View.VISIBLE
                    binding.groupChatMemberInfoIV.setOnClickListener {
                        val intent = Intent(mContext, GroupMemberInfoActivity::class.java).apply {
                            putExtra(EXTRA_CHANNEL_URL, mChannelURL)
                            putExtra(EXTRA_USER_ID, user.userId)
                            putExtra(EXTRA_USER_PROFILE_URL, user.profileUrl)
                            putExtra(EXTRA_USER_NICKNAME, user.nickname)
                            putExtra(EXTRA_USER_BLOCKED_BY_ME, (user as Member).isBlockedByMe)
                        }
                        mContext.startActivity(intent)
                    }
                }

                val isBlockedByUser = (user as Member).isBlockedByMe

                if (isBlockedByUser) {
                    binding.groupChatMemberBlockedIV.visibility = View.VISIBLE
                    binding.groupChatMemberBlockedTV.visibility = View.VISIBLE
                }
                else {
                    binding.groupChatMemberBlockedIV.visibility = View.GONE
                    binding.groupChatMemberBlockedTV.visibility = View.GONE
                }
            }
            else {
                binding.groupChatMemberBlockedIV.visibility = View.GONE
                binding.groupChatMemberBlockedContainer.visibility = View.GONE
            }
        }
    }







}