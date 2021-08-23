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

/*
 * 전체 User List를 보여주는 Fragment
 * RecyclerView로 List를 보임
 * 초기에 15명, 스크롤을 내리면 10명씩 추가로 보이게 구현
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
        val binding = FragmentFriendsBinding.inflate(inflater, container, false)
        mFragFriendsBinding = binding
        return mFragFriendsBinding!!.root
    }

    override fun onDestroyView() {
        mFragFriendsBinding = null // Memory Leak 관련 - Binding 해제
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        refresh()
    }

    /*
     * RecyclerView 초기 세팅 Method
     */
    private fun setUpRecyclerView() {
        mLinearLayoutManager = LinearLayoutManager(mFragFriendsBinding!!.root.context)
        mFriendsListAdapter = FriendsListAdapter(mFragFriendsBinding!!.root.context)
        mFragFriendsBinding!!.friendsFragFriendsListRV.layoutManager = mLinearLayoutManager
        mFragFriendsBinding!!.friendsFragFriendsListRV.adapter = mFriendsListAdapter
        mFragFriendsBinding!!.friendsFragFriendsListRV.addItemDecoration(DividerItemDecoration(mFragFriendsBinding!!.root.context, DividerItemDecoration.VERTICAL)) // Item간 구분선 넣기

        mFragFriendsBinding!!.friendsFragFriendsListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // Scroll을 내리면 다음 10명의 Friends 리스트를 불러옴
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

    /**
     * 초기 FriendsList 불러오는 Method
     * @param friendsCnt    요청 user
     * @TODO createUserListQuery 대안 적용 필요
     * @Deprecated createUserListQuery
     */
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

    /**
     * Scroll 시 다음 FriendsList 불러오는 Method
     * @param moreCnt   scroll 시 추가 요청 user
     */
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

