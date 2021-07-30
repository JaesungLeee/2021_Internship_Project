package kr.co.iboss.chat.UI

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

class MainActivity : AppCompatActivity() {

    companion object {
        private val INTENT_USER_ID              = "INTENT_USER_ID"
    }


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

        val userID = intent.getStringExtra(INTENT_USER_ID).toString()
        Log.e("MAIN_INTENT", userID)
        initFragmentNavigation(userID)
    }

    private fun initFragmentNavigation(userID: String) {
        binding.mainBottomNavigation.run {
            setOnNavigationItemSelectedListener {
                when(it.itemId) {
//                    R.id.all_openChat_menu  -> setDataAtFragment(allOpenChatListFragment, userID)
//                    R.id.my_openChat_menu   -> setDataAtFragment(openChatListFragment, userID)
                    R.id.my_groupChat_menu  -> setDataAtFragment(groupChatListFragment, userID)
                    R.id.friends_menu       -> setDataAtFragment(friendsFragment, userID)
                    R.id.settings_menu      -> setDataAtFragment(settingsFragment, userID)
                }
                true
            }
            selectedItemId = R.id.my_groupChat_menu
        }
    }

    private fun setDataAtFragment(fragment: Fragment, userID: String) {
        val bundle = Bundle()
        bundle.putString(INTENT_USER_ID, userID)

        fragment.arguments = bundle
        changeFragment(fragment)
    }

    private fun changeFragment(fragment : Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_Layout, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
}