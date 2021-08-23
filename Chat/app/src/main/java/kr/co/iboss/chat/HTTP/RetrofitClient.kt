package kr.co.iboss.chat.HTTP

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*
 * Base URL, GsonConverter 적용
 * Retrofit Builder 생성하는 Object Class
 */
object RetrofitClient {

    private val BASE_URL = "https://www.i-boss.co.kr/"

    val instance : RetrofitAPI by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RetrofitAPI::class.java)
    }
}