package kr.co.iboss.chat.HTTP

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("statusCode") val code : String,
    @SerializedName("statusMessage") val message : String,
    @SerializedName("data") val data : Data? = null
) {
    data class Data(
        @SerializedName("id") val userID : String,
        @SerializedName("nick_name") val userNickName : String,
        @SerializedName("email") val userEmail : String,
        @SerializedName("profile") val userProfileImage : String,
        @SerializedName("display_user_id") val displayUID : String
    )
}