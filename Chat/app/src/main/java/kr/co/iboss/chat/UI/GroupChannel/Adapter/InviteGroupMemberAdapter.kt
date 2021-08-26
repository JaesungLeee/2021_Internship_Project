package kr.co.iboss.chat.UI.GroupChannel.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.User
import kr.co.iboss.chat.Utils.ImageUtils
import kr.co.iboss.chat.databinding.ListItemInviteGroupMembersBinding

/* 멤버 초대를 위한 InviteGroupMemberActivity의 RecyclerView에서 사용되는 Adapter Class */
class InviteGroupMemberAdapter(private val mContext : Context, private val mIsBlockedList : Boolean, private var mShowCheckBox : Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private lateinit var mSelectedUserIdList : MutableList<String>  // 체크박스에서 선택된 User의 Id를 담는 List
    }

    private var mUserList : MutableList<User>? = null   // 전체 User List

    private var mInviteGroupMemberHolder : InviteGroupMemberHolder? = null
    private var mCheckBoxStateListener : OnItemCheckedStateListener? = null

    init {
        mUserList = ArrayList()
        mSelectedUserIdList = ArrayList()
    }

    /* 체크박스 리스너 */
    interface OnItemCheckedStateListener {
        fun onItemChecked(user : User, isChecked : Boolean)
    }

    fun setCheckBoxStateListener(listener : OnItemCheckedStateListener) {
        mCheckBoxStateListener = listener
    }

    fun setUserList(users : MutableList<User>) {
        mUserList = users
        notifyDataSetChanged()
    }

    fun setShowCheckBox(showCheckBox: Boolean) {
        mShowCheckBox = showCheckBox
        if (mInviteGroupMemberHolder != null) {
            mInviteGroupMemberHolder!!.setShowCheckBox(showCheckBox)
        }
        notifyDataSetChanged()
    }

    /* 스크롤 될 떄 Call되는 Method */
    fun addLast(user : User) {
        mUserList!!.add(user)
        notifyDataSetChanged()
    }

    /**
     * 체크박스 선택 유무 판단 Method
     * 체크박스가 선택되어 있으면 True, 해제되어있으면 False
     * @param user  User
     * @return T/F
     */
    private fun isCheckBoxSelected(user : User) : Boolean {
        return mSelectedUserIdList.contains(user.userId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListItemInviteGroupMembersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InviteGroupMemberHolder(binding, mIsBlockedList, mShowCheckBox)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as InviteGroupMemberHolder).bindViews(mContext, mUserList!![position], isCheckBoxSelected(mUserList!![position]), mCheckBoxStateListener!!)
    }

    override fun getItemCount(): Int {
        return mUserList!!.size
    }

    private inner class InviteGroupMemberHolder(val binding: ListItemInviteGroupMembersBinding, private val mIsBlockedList : Boolean, private var mShowCheckBox: Boolean) : RecyclerView.ViewHolder(binding.root){

        init {
            this.setIsRecyclable(false)
        }

        fun setShowCheckBox(showCheckBox: Boolean) {
            mShowCheckBox = showCheckBox
        }

        fun bindViews(context: Context, user: User, isCheckBoxSelected: Boolean, listener: OnItemCheckedStateListener) {
            setUserAttribute(context, user)
            setBlockImageView()
            setCheckBoxStatus(isCheckBoxSelected)
            setSelectedUserIdList(user, listener)
        }

        private fun setUserAttribute(context: Context, user: User) {
            binding.inviteGroupMemberMemberNickNameTV.text = user.nickname

            ImageUtils.displayRoundImageFromUrl(context, user.profileUrl, binding.inviteGroupMemberMemberProfileIV)
        }

        /* Block된 User의 프로필 표시 */
        private fun setBlockImageView() {
            if (mIsBlockedList) binding.inviteGroupMemberMemberBlockedIV.visibility = View.VISIBLE
            else binding.inviteGroupMemberMemberBlockedIV.visibility = View.GONE
        }

        private fun setCheckBoxStatus(isCheckBoxSelected: Boolean) {
            if (mShowCheckBox) binding.inviteGroupMemberCheckBox.visibility = View.VISIBLE
            else binding.inviteGroupMemberCheckBox.visibility = View.GONE

            binding.inviteGroupMemberCheckBox.isChecked = isCheckBoxSelected

            if (mShowCheckBox) {
                binding.inviteGroupMemberItemContainer.setOnClickListener {
                    if (mShowCheckBox) {
                        binding.inviteGroupMemberCheckBox.isChecked = !binding.inviteGroupMemberCheckBox.isChecked
                    }
                }
            }
        }

        private fun setSelectedUserIdList(user : User, listener: OnItemCheckedStateListener) {
            binding.inviteGroupMemberCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                listener.onItemChecked(user, isChecked)

                if (isChecked) mSelectedUserIdList.add(user.userId)
                else mSelectedUserIdList.remove(user.userId)
            }
        }

    }
}