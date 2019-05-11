package team.gotohel.lifeguard.api

import android.util.Log
import io.reactivex.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import team.gotohel.lifeguard.BuildConfig
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

class RecipesClientTextHtml() {

    companion object {
        private val BASE_URL_API_SERVER_DEFAULT = "https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/"

        private var apiClient: RecipesClientTextHtml? = null
        fun getInstance(): RecipesClientTextHtml {
            if (apiClient == null) {
                apiClient = RecipesClientTextHtml()
            }
            return apiClient!!
        }

        fun getContentsFromWeb(html: String): Single<List<Pair<String, String?>>> {
            return Single.create<List<Pair<String, String?>>> {
                try {
                    // 해당 웹페이지의 document
                    val document = Jsoup.parse(html)
                    val r =  document.select(".spoonacular-name")
                        .map {
                            val name = it.html()
                            val metric = it.parent().select(".spoonacular-metric").firstOrNull()?.html()
                            Pair(name, metric)
                        }

                    it.onSuccess(r)
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (!it.isDisposed) it.onError(e)
                }
            }
        }
    }

    interface APIService {
        @GET("recipes/{recipesId}/ingredientWidget")
        fun getIngredientByRecipes(@Path("recipesId") recipesId: Int): Single<String>
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
            .addConverterFactory(ScalarsConverterFactory.create())
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
                    .addHeader("X-RapidAPI-Host", "spoonacular-recipe-food-nutrition-v1.p.rapidapi.com")
                    .addHeader("X-RapidAPI-Key", "53d35b92b9mshd54982400472c00p14f890jsn4f68762441b2")
                    .addHeader("Accept", "text/html")
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
