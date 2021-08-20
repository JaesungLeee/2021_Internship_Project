package kr.co.iboss.chat.UI.Settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sendbird.android.SendBird.DisconnectHandler
import com.sendbird.android.SendBirdException
import com.sendbird.android.SendBirdPushHelper.OnPushRequestCompleteListener
import kr.co.iboss.chat.BaseApplication
import kr.co.iboss.chat.UI.Auth.LoginHomeActivity
import kr.co.iboss.chat.Utils.ConnectionUtils
import kr.co.iboss.chat.Utils.PreferencesUtils
import kr.co.iboss.chat.Utils.PushUtils
import kr.co.iboss.chat.databinding.FragmentSettingsBinding

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {

    private var mFragSettingsBinding : FragmentSettingsBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //userId = it.getString(INTENT_USER_ID)
        }

        //Log.e("SETTINGS_FRAG_INTENT", "id : $userId")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        mFragSettingsBinding = binding
        return mFragSettingsBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonListener()
    }

    private fun buttonListener() {
        mFragSettingsBinding!!.settingsFragLogoutBtn.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        PushUtils.unregisterPushHandler(object : OnPushRequestCompleteListener {
            override fun onComplete(isActive: Boolean, token: String?) {
                ConnectionUtils.logout(DisconnectHandler {
                    PreferencesUtils(mFragSettingsBinding!!.root.context).setConnected(false)
                    val intent = Intent(BaseApplication.instance.context(), LoginHomeActivity::class.java)
                    startActivity(intent)

                    activity?.finish()
                })
            }

            override fun onError(e: SendBirdException) {}
        })
    }


}