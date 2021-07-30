package kr.co.iboss.chat.HTTP.DTO

import com.google.gson.annotations.SerializedName

data class EmailRequestDTO(
    @SerializedName("id") var id : String,
    @SerializedName("pw") var pw : String
)
