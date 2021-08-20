package kr.co.iboss.chat.UI.Friends

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.User
import kr.co.iboss.chat.Utils.ImageUtils
import kr.co.iboss.chat.databinding.ListItemFriendsBinding

class FriendsListAdapter(private val mContext : Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mUsers : MutableList<User>? = null
    private var mOptionClickedListener : OnOptionClickedListener? = null

    interface OnOptionClickedListener {
        fun onOptionClicked()
    }

    init {
        mUsers = ArrayList()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ListItemFriendsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendsListHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FriendsListHolder).bindViews(mContext, holder, mUsers!![position])
    }

    override fun getItemCount(): Int {
        return mUsers!!.size
    }

    fun setOptionClickedListener(listener : OnOptionClickedListener) {
        mOptionClickedListener = listener
    }

    fun setFriendsList(users : MutableList<User>) {
        mUsers = users
        notifyDataSetChanged()
    }

    fun addLast(user : User) {
        mUsers!!.add(user)
        notifyDataSetChanged()
    }

    private inner class FriendsListHolder (val binding : ListItemFriendsBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindViews(context : Context, holder : FriendsListHolder, user : User) {
            setFriendsAttribute(context, user)
        }

        private fun setFriendsAttribute(context : Context, user : User) {
            binding.friendsNickNameTV.text = user.nickname

            ImageUtils.displayRoundImageFromUrl(context, user.profileUrl, binding.friendsProfileImageIV)
        }

    }
}