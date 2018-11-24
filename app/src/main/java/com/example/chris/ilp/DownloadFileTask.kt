package com.example.chris.ilp

import android.os.AsyncTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class DownloadFileTask(private val caller : DownloadCompleteListener) :
        AsyncTask<String, Void, String>() {


    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference



    override fun doInBackground(vararg urls: String): String = try {
        loadFileFromNetwork(urls[0])
    } catch (e: IOException) {
        "Unable to load content. Check your network connection"
    }
    private fun loadFileFromNetwork(urlString: String): String {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference
        val stream : InputStream = downloadUrl(urlString)
/* Read input from stream, build result as a string */
        val result = stream.bufferedReader().use {
            it.readText()
        }
        val userId = auth.currentUser?.uid
        dbRef.child("users").child(userId).child("map").setValue(result)
        return result
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