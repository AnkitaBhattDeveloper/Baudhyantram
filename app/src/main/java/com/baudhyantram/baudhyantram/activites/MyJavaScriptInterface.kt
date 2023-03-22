package com.baudhyantram.baudhyantram.activites

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast


open class MyJavaScriptInterface
/** Instantiate the interface and set the context  */ internal constructor(var mContext: Context) {
    @JavascriptInterface // must be added for API 17 or higher
    fun showToast(toast: String?) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
        Log.e("TAG", "showToast: java script interface ")
    }
}