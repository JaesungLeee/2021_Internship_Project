package kr.co.iboss.chat.HTTP.DTO

import com.google.gson.annotations.SerializedName

data class SocialRequestDTO(
    @SerializedName("loginType") var loginType : String,
    @SerializedName("token") var token : String
)
