package team.gotohel.lifeguard.api

import android.util.Log
import io.reactivex.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import team.gotohel.lifeguard.BuildConfig
import team.gotohel.lifeguard.MyApplication
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * Created by HwaminSon on 2018-02-27.
 */

/**
 * fields -> @Field (메소드 앞에 @FormUrlEncoded 를 붙여야 한다.)
 * parameters -> @Query
 * path -> @Path
 */

class NutritionixClient() {

    companion object {
        private val BASE_URL_API_SERVER_DEFAULT = "https://nutritionix-api.p.rapidapi.com/"


        private var apiClient: NutritionixClient? = null
        fun getInstance(): NutritionixClient {
            if (apiClient == null) {
                apiClient = NutritionixClient()
            }
            return apiClient!!
        }
    }

    interface APIService {
        @GET("v1_1/item")
        fun searchFoodByUpc(@Query("upc") upc: String): Single<NutritionixResponse>
    }

    var retrofit: Retrofit
    val call: APIService
    val okHttpClient : OkHttpClient.Builder

    init {
        okHttpClient = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(AuthenticationInterceptor())

        val logging = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) { // development build
            logging.level = HttpLoggingInterceptor.Level.BODY
        } else { // production build
            logging.level = HttpLoggingInterceptor.Level.BASIC
        }

        okHttpClient.addInterceptor(logging)

        //set normal rest adapter
        retrofit = Retrofit.Builder()
                .client(okHttpClient.build())
                .baseUrl(BASE_URL_API_SERVER_DEFAULT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        call = retrofit.create(APIService::class.java)
    }

    internal class AuthenticationInterceptor() : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            try {
                val raw = chain.request()
                val authorized = raw.newBuilder()
                    .addHeader("X-RapidAPI-Host", "nutritionix-api.p.rapidapi.com")
                    .addHeader("X-RapidAPI-Key", MyApplication.API_KEY_RAKUTEN)
                    .addHeader("Accept", "application/json")
                        .build()

                val response = chain.proceed(authorized)

                val statusCode = response.code()
                if (statusCode == 401) {
//                    Log.d("인증 에러", "authKey : $authKey")
                }

                return response

            } catch (e: Exception) {

                //공통적인 오류 상황에 대한 안내 처리를 이곳에서 해준다.

                if (e is ConnectException || e is SocketTimeoutException) {
                    //기기가 네트워크에 연결되지 않거나, 서버가 완전히 죽어버린 상황
                    e.printStackTrace()
                    Log.e("APIClient", "Connection Problem")

                } else {
                    e.printStackTrace()
                }

                throw e
            }
        }
    }
}
