package com.example.chris.ilp

import android.content.Context
import android.os.AsyncTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class DownloadFileTask(private val caller : MainActivity.DownloadCompleteListener) :
        AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg urls: String): String = try {
        loadFileFromNetwork(urls[0])
    } catch (e: IOException) {
        "Unable to load content. Check your network connection"
    }
    private fun loadFileFromNetwork(urlString: String): String {
        val auth = FirebaseAuth.getInstance()
        val dbRef = FirebaseDatabase.getInstance().reference
        val stream : InputStream = downloadUrl(urlString)
        /* read the input stream and save it to database. */
        val allText = stream.bufferedReader().use(BufferedReader::readText)
        val result = StringBuilder()
        result.append(allText)
        dbRef.child("users").child(auth.currentUser?.uid.toString()).child("map").setValue(result.toString())
        return result.toString()
    }

    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        // Also available: HttpsURLConnection
        conn.readTimeout = 10000 // milliseconds
        conn.connectTimeout = 15000 // milliseconds
        conn.requestMethod = "GET"
        conn.doInput = true
        conn.connect() // Starts the query
        return conn.inputStream
    }
    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        caller.downloadComplete(result)
    }

}