package kr.co.iboss.chat.UI.GroupChannel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sendbird.android.GroupChannel
import kr.co.iboss.chat.R
import kr.co.iboss.chat.UI.GroupChannel.Adapter.InviteGroupMemberAdapter

class InviteGroupMemberActivity : AppCompatActivity() {

    private var mAdapter : InviteGroupMemberAdapter? = null
    private var mChannel : GroupChannel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_group_member)
    }
}