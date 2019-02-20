package com.example.coroutinenetworkblockingsample

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val tag = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        Log.i(tag, "----- START -----")

        client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        Handler().post {
//            startNetworkEventByAsyncTask()
            startNetworkEventByCoroutine()
        }

//        Log.i(tag, "----- END -----")
    }

    private fun startNetworkEventByCoroutine() {

        repeat(20) {
            val taskName = "coroutine#$it"
            GlobalScope.launch(Dispatchers.Main) {

                withContext(Dispatchers.IO) {

                    Log.d(tag, "$taskName: start")

                    val response = doNetworkTask()
                    response.use {
                        Log.d(tag, "$taskName: response=" + response.code())
                    }
                }

                Log.d(tag, "$taskName: done")
            }
        }
    }

    private fun startNetworkEventByAsyncTask() {

        repeat(20) {
            val taskName = "async-task#$it"
            MyTask(taskName).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    class MyTask(private val taskName: String) : AsyncTask<Void, Void, Void>() {

        private val tag = javaClass.simpleName

        override fun doInBackground(vararg params: Void?): Void? {

            Log.d(tag, "$taskName: start")

            val response = doNetworkTask()
            response.use {
                Log.d(tag, "$taskName: response=" + response.code())
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            Log.d(tag, "$taskName: done")
        }

    }

    companion object {

        lateinit var client: OkHttpClient

        private fun doNetworkTask(): Response {
            val request = Request.Builder()
                .url("https://www.takke.jp/sleep.php")
                .build()

            return client.newCall(request).execute()
        }
    }
}
