package com.example.apirequestcoroutinesample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.apirequestcoroutinesample.databinding.ActivityMainBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.math.BigInteger

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var okHttpClient = OkHttpClient()
    var url = "https://reqres.in/api/users/1" //Url of the used API


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showResponse("Loading...")
        lifecycleScope.launch {
            log("onResume lifecycleScope.launch Thread = ${Thread.currentThread().name}")

            //val response = doLongOperation()

            val response = findFactorial(50000).toString()

            //val response = fetchApiWithContext(url)

            //val response = fetchApiAsync(url).await()

            //val response = fetchApiError(url).await() // does not work correctly

            showResponse(response)
        }
    }

    /*
  This suspend function will do some doLongOperation with lifecycleScope.async(Dispatchers.IO)
  and return the String Deferred<String>
  Obs: it is need the call .await the get the String returned
   */
    private suspend fun doLongOperation() : String {

        // doLongOperation() is launch scope, so does not return nothing
        lifecycleScope.launch(Dispatchers.IO) {
            delay(10_000)
            log("Finished Coroutine doLongOperation Thread = ${Thread.currentThread().name}")
        }

        return "nothing"
    }



    /*
 This suspend function will do a factorial calculate with withContext(Dispatchers.Default)
 and return the String Deferred<String>
 Obs: it is need the call .await the get the String returned
 Obs: If use Dispatchers.Main the smartphone must be freeze
  */
    private suspend fun findFactorial(input: Int) : BigInteger =
        withContext(Dispatchers.Default) {
            val factorial = factorial(input)
            log("findFactorial of $input finished Thread = ${Thread.currentThread().name}")
            return@withContext factorial
        }

    /*
    This suspend function will fetch the API withContext Dispatchers.IO and return the String
     */
    private suspend fun fetchApiWithContext(url: String) : String {

        return withContext(Dispatchers.IO) {
            var result : String = "before"
            log("fetchApi Thread = ${Thread.currentThread().name}")
            delay(5000)
            val builder = Request.Builder()
            builder.url(url)
            val request = builder.build()

            try {
                val response: Response = okHttpClient.newCall(request).execute()
                result =  response.body().string()
                log("fetchApi response = $response")
                return@withContext result
            } catch (e: Exception) {
                val error = "fetchApi error = $e Thread = ${Thread.currentThread().name}"
                log(error)
                e.printStackTrace()
                return@withContext error
            }
        }
    }


    /*
    This suspend function will fetch the API with lifecycleScope.async(Dispatchers.IO)
    and return the String Deferred<String>
    Obs: it is need the call .await the get the String returned
     */
    private suspend fun fetchApiAsync(url: String) : Deferred<String> {

        return lifecycleScope.async(Dispatchers.IO) {
            var result : String = "before"
            log("fetchApi Thread = ${Thread.currentThread().name}")
            delay(5000)
            val builder = Request.Builder()
            builder.url(url)
            val request = builder.build()

            try {
                val response: Response = okHttpClient.newCall(request).execute()
                //log("fetchApi response = ${response.toString()} responseBody = ${response.body().string()} Thread = ${Thread.currentThread().name}")
                result =  response.body().string()
                log("fetchApi response = $response")
                return@async result
            } catch (e: Exception) {
                val error = "fetchApi error = $e Thread = ${Thread.currentThread().name}"
                log(error)
                e.printStackTrace()
                return@async error
            }
        }
    }

    /* *
      I do now, but this function does not work, it can not return the String response
    */
    private suspend fun fetchApiError(url: String) : Deferred<String> {

        return lifecycleScope.async(Dispatchers.IO) {
            var result : String = "antes"
            log("fetchApi Thread = ${Thread.currentThread().name}")
            delay(2000)
            val builder = Request.Builder()
            builder.url(url)
            val request = builder.build()

            try {
                val response: Response = okHttpClient.newCall(request).execute()
                // if you log the next line the method does not work, compare the fetchApiAsync to see the diff
                log("fetchApi response = ${response.toString()} responseBody = ${response.body().string()} Thread = ${Thread.currentThread().name}")
                result =  response.body().string()
                return@async result
            } catch (e: Exception) {
                val error = "fetchApi error = $e Thread = ${Thread.currentThread().name}"
                log(error)
                e.printStackTrace()
                return@async error
            }
        }
    }

    private fun showResponse(r : String) {
        log("showResponse r = $r - Thread = ${Thread.currentThread().name}")
        binding.textView.text = r
    }

    private fun factorial(number: Int): BigInteger {
        var factorial: BigInteger = BigInteger.ONE

        for (curNum in 1..number) {
            //log( "factorial x $curNum on ${Thread.currentThread().name}")
            factorial = factorial.multiply(BigInteger.valueOf(curNum.toLong()))
        }

        return factorial
    }

    private fun log(l: String) {
        Log.d("layon.f", l)
    }
}