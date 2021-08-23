package kr.co.iboss.chat.HTTP

import kr.co.iboss.chat.HTTP.DTO.EmailRequestDTO
import kr.co.iboss.chat.HTTP.DTO.SocialRequestDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/*
 * HTTP 통신에 사용 될 Method를 정의한 Interface
 * HTTP POST 방식 사용
 */
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