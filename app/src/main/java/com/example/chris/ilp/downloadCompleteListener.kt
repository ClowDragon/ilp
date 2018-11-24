package com.example.chris.ilp

interface DownloadCompleteListener {
    fun downloadComplete(result: String)
}
object DownloadCompleteRunner : DownloadCompleteListener {
    var result : String? = "test1"
    override fun downloadComplete(result: String) {
        this.result = result
    }
}