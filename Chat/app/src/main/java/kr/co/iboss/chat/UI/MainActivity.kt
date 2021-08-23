package kr.co.iboss.chat.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kr.co.iboss.chat.R
import kr.co.iboss.chat.UI.Friends.FriendsFragment
import kr.co.iboss.chat.UI.GroupChannel.GroupChatListFragment
import kr.co.iboss.chat.UI.Settings.SettingsFragment
import kr.co.iboss.chat.databinding.ActivityMainBinding

/*
 * 각 Fragment (GroupChatListFragment, FriendsFragment, SettingsFragment)의 Host Activity
 * UI로 BottomNavigationBar 구현
 * Fragment 전환, Data 전달 지원
 */
class MainActivity : AppCompatActivity() {

    //    private val allOpenChatListFragment by lazy { AllOpenChatListFragment() }
    private val friendsFragment by lazy { FriendsFragment() }
    private val groupChatListFragment by lazy { GroupChatListFragment() }
    //    private val openChatListFragment by lazy { OpenChatListFragment() }
    private val settingsFragment by lazy { SettingsFragment() }

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initFragmentNavigation()
        checkIntentExtra()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkIntentExtra()
    }

    /* BottomNavigationBar 세팅 */
    private fun initFragmentNavigation() {
        binding.mainBottomNavigation.run {
            setOnNavigationItemSelectedListener {
                when(it.itemId) {
//                    R.id.all_openChat_menu  -> setDataAtFragment(allOpenChatListFragment, userID)
//                    R.id.my_openChat_menu   -> setDataAtFragment(openChatListFragment, userID)
                    R.id.my_groupChat_menu  -> checkIntentExtra()
                    R.id.friends_menu       -> changeFragment(friendsFragment)
                    R.id.settings_menu      -> changeFragment(settingsFragment)
                }
                true
            }
            selectedItemId = R.id.my_groupChat_menu
        }
    }

    /* Push Notification 시 groupChannelURL 데이터 전달 */
    private fun checkIntentExtra() {
        val fragment = groupChatListFragment
        if (intent.hasExtra("groupChannelUrl")) {
            val extraChannelUrl = intent.getStringExtra("groupChannelUrl")
            Log.e("EXTRA", extraChannelUrl!!.toString())
            val bundle = Bundle()
            bundle.putString("groupChannelUrl", extraChannelUrl)
            fragment.arguments = bundle

            changeFragment(fragment)
        }
        Log.e("EXTRA", "NOT YET")
        changeFragment(fragment)
    }

    /* Fragment 전환 */
    private fun changeFragment(fragment : Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_Layout, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
}