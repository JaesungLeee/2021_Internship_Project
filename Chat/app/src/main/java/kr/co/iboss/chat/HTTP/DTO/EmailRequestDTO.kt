package kr.co.iboss.chat.HTTP.DTO

import com.google.gson.annotations.SerializedName

/*
 * 일반 계정 로그인 Request DTO
 */
data class EmailRequestDTO(
    @SerializedName("id") var id : String,
    @SerializedName("pw") var pw : String
)
