package kr.co.iboss.chat.HTTP.DTO

import com.google.gson.annotations.SerializedName

/*
 * 소셜 로그인 Request DTO
 */
data class SocialRequestDTO(
    @SerializedName("loginType") var loginType : String,
    @SerializedName("token") var token : String
)
