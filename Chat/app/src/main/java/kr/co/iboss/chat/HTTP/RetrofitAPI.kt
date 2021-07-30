package kr.co.iboss.chat.HTTP

import kr.co.iboss.chat.HTTP.DTO.EmailRequestDTO
import kr.co.iboss.chat.HTTP.DTO.SocialRequestDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitAPI {
    @POST("chat/member/info")
    fun userLogin(
        @Body loginRequest: EmailRequestDTO
    ) : Call<LoginResponse>

    @POST("chat/member/info")
    fun socialLogin(
        @Body socialRequest: SocialRequestDTO
    ) : Call<LoginResponse>
}