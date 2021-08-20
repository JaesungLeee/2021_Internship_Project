package kr.co.iboss.chat.UI.Friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.SendBird
import com.sendbird.android.UserListQuery
import kr.co.iboss.chat.databinding.FragmentFriendsBinding

/**
 * A simple [Fragment] subclass.
 * Use the [FriendsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendsFragment : Fragment() {

    companion object {
        private val FREINDS_LIMIT_COUNT = 15
    }

    private var mFragFriendsBinding : FragmentFriendsBinding? = null
    private var mFriendListQuery : UserListQuery? = null
    private var mFriendsListAdapter : FriendsListAdapter? = null
    private var mLinearLayoutManager : LinearLayoutManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val binding = FragmentFriendsBinding.inflate(inflater, container, false)
        mFragFriendsBinding = binding
        return mFragFriendsBinding!!.root
    }

    override fun onDestroyView() {
        mFragFriendsBinding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        refresh()
    }

    private fun setUpRecyclerView() {
        mLinearLayoutManager = LinearLayoutManager(mFragFriendsBinding!!.root.context)
        mFriendsListAdapter = FriendsListAdapter(mFragFriendsBinding!!.root.context)
        mFragFriendsBinding!!.friendsFragFriendsListRV.layoutManager = mLinearLayoutManager
        mFragFriendsBinding!!.friendsFragFriendsListRV.adapter = mFriendsListAdapter
        mFragFriendsBinding!!.friendsFragFriendsListRV.addItemDecoration(DividerItemDecoration(mFragFriendsBinding!!.root.context, DividerItemDecoration.VERTICAL))

        mFragFriendsBinding!!.friendsFragFriendsListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (LinearLayoutManager(mFragFriendsBinding!!.root.context).findLastVisibleItemPosition() == mFriendsListAdapter!!.itemCount - 1) {
                    loadNextFriendsList(10)
                }
            }
        })

        refresh()
    }

    private fun refresh() {
        refreshFriendsList(FREINDS_LIMIT_COUNT)
    }

    private fun refreshFriendsList(friendsCnt : Int) {
        mFriendListQuery = SendBird.createUserListQuery()
        mFriendListQuery!!.setLimit(friendsCnt)

        mFriendListQuery!!.next { list, e ->
            if (e != null) {
                return@next
            }

            mFriendsListAdapter!!.setFriendsList(list)

        }
    }

    private fun loadNextFriendsList(moreCnt : Int) {
        mFriendListQuery!!.setLimit(moreCnt)
        mFriendListQuery!!.next { friendsList, e ->
            if (e != null ) {
                e.printStackTrace()
                return@next
            }

            for (friend in friendsList) {
                mFriendsListAdapter!!.addLast(friend)
            }
        }
    }
}

