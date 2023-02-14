package com.baudhyantram.baudhyantram.activites

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.baudhyantram.baudhyantram.constant.App
import com.baudhyantram.baudhyantram.databinding.ActivityMainBinding
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var context: Context

    //val webUrl: String = "https://jansath.com/"
    var current_url: String = App.URL
    private var mUploadMessage: ValueCallback<Uri>? = null

    private val appUpdateManager: AppUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        context = this

        UpdateApp()


        binding.swipeRefresh.setOnRefreshListener {
            showProgressBar()
            bindWeb(current_url)
            binding.swipeRefresh.isRefreshing = false
        }

        bindWeb(App.URL)
       /* binding.webView.setOnTouchListener { v, event ->
            binding.webView.performClick()
            val hr = (v as WebView).hitTestResult
            Log.e("TAG", "onCreate: touch event listerner $hr ")
            false
        }*/

    }

    @SuppressLint("JavascriptInterface")
    fun bindWeb(webUrl: String) {
        var loadingFinished = true;
        var redirect = false;
        binding.webView.apply {
            loadUrl(webUrl)
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            setBackgroundColor(Color.TRANSPARENT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settings.safeBrowsingEnabled = true  // api 26
            }

            /*   //loadUrl("file:///android_asset/file.html")
               addJavascriptInterface(object : Any() {
                   fun performClick(string: String) {
                       Log.e("TAG", "performClick: $string")
                   }
               }, "OK")*/

        }
        binding.webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String
            ): Boolean {

                if (url.contains("tel:") || url.contains("whatsapp:")) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    Log.e("TAG", "shouldOverrideUrlLoading: whstapp and telephone ${intent.data}")
                    startActivity(intent)

                }

                if (url.startsWith("http:") || url.startsWith("https:")) {
                    if (url.contains("https://m.facebook.com/"))
                        isPackageInstalled("com.facebook.android", context, url)
                    else if (url.contains("https://www.instagram.com/"))
                        isPackageInstalled("com.instagram.android", context, url)
                    else if (url.contains("https://api.whatsapp.com/"))
                        isPackageInstalled("com.whatsapp", context, url)
                    else if (url.contains("https://twitter.com/"))
                        isPackageInstalled("com.twitter.android", context, url)
                    else
                        view?.loadUrl(url)


                }
                 if (url.contains("https://baudhyantram.com/index.php/course-registration/")) {
                      binding.webView.addJavascriptInterface(JSBridge,"Bridge")
                  }

                return true

            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                showProgressBar()
                current_url = url.toString()
                val uri = Uri.parse(current_url)
                Log.e("TAG", "onPageStarted:task id = ${taskId} ")

                /*  if ((current_url).contains("tel:")) {
                      Log.e("TAG", "shouldOverrideUrlLoading: current url $current_url ")
                      val intent = Intent(Intent.ACTION_VIEW)
                      intent.data = Uri.parse(current_url)
                      startActivity(intent)
                      //finish()
                  } else
                      showProgressBar()
  */

                Log.e("TAG", "onPageStarted:$current_url ")
                Log.e("TAG", "isPackageInstalled: ${uri.path}")
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                hideProgressBar()
                injectJavaScript(view)
                super.onPageFinished(view, url)
            }

        }

    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack())
            binding.webView.goBack()
        else
            super.onBackPressed()
    }

    fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE

    }

    fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        binding.webView.visibility = View.INVISIBLE

    }


    fun UpdateApp() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if ((it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) && it.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                )
            )
                appUpdateManager.startUpdateFlowForResult(
                    it,
                    AppUpdateType.IMMEDIATE,
                    this,
                    App.UPDATE_RESULT_CODE
                )
            Log.e("TAG", "UpdateApp: Success to check for an update  ")
        }
            .addOnFailureListener {
                Log.e("TAG", "UpdateApp: Failed to check for an update  ")
            }
    }


    fun isPackageInstalled(packageName: String, context: Context, url: String) {
        val uri = Uri.parse(url)

        val linking = Intent(Intent.ACTION_VIEW, uri)
        linking.setPackage(packageName)
        Log.e("TAG", "isPackageInstalled: $packageName ")
        try {
            startActivity(linking)
            Log.e("TAG", "isPackageInstalled: linking $linking ")

        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            Log.e("TAG", "isPackageInstalled:  exception ${e.message}")

        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
                appUpdateManager.startUpdateFlowForResult(
                    it,
                    AppUpdateType.IMMEDIATE,
                    this,
                    App.UPDATE_RESULT_CODE
                )

        }
    }

/*  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      super.onActivityResult(requestCode, resultCode, data)
      if (resultCode == App.UPDATE_RESULT_CODE)
          finish()
  }*/


    fun onFormResubmission(
        view: WebView?,
        dontResend: String,
        resend: String
    ) {

        Log.e("TAG", "onFormResubmission: ${view?.contentDescription}")
        val path = Uri.parse(view?.loadUrl(App.URL).toString())
        Log.e("TAG", "onFormResubmission:path  ${path}")
        Log.e("TAG", "onFormResubmission: dont resend ${dontResend}")
        val i = Intent(Intent.ACTION_GET_CONTENT)
        Log.e("TAG", "onFormResubmission: resend ${resend}")
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "text/*"
        Log.e("TAG", "onFormResubmission: ${i.data.toString()}")
        startActivityForResult(Intent.createChooser(i, "Choose File"), 1)
    }

    private fun injectJavaScript(view: WebView?)
    {
        Log.e("TAG", "injectJavaScript: ", )
       /* view!!.loadUrl("""
            javascript:(function(){
            let btUploadPhoto = document.querySelector(".random");
            btUploadPhoto.addEventListener("click",function(){})
            Bridge.calledFromJS();
          
            })
            })()
        """)*/
    }


object JSBridge{
    fun calledFromJS()
    {
        Log.e("TAG", "calledFromJS: ", )
    }
}


}

