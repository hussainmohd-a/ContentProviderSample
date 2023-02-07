package com.celzero.interop

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.celzero.interop.databinding.ActivityAppsCrudBinding

class AppsCrudActivity : AppCompatActivity() {

  private lateinit var binding: ActivityAppsCrudBinding

  private val AUTHORITY = "com.celzero.bravedns.appprovider"
  private val URI_APP = Uri.parse("content://${AUTHORITY}/apps")

  private val URI_DELETE = Uri.parse("content://${AUTHORITY}/apps/1234567")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityAppsCrudBinding.inflate(layoutInflater)
    setContentView(binding.root)

    init()
  }

  private fun init() {
    binding.buttonFirst.setOnClickListener {
      val cursor = contentResolver.query(URI_APP, null, null, null, null)
      binding.textviewFirst.text = cursor!!.count.toString()
      cursor.close()
    }

    binding.buttonSecond.setOnClickListener {
      val uri = contentResolver.insert(URI_APP, getContentValues())
      binding.textviewSecond.text = uri?.toString() ?: "Empty"
    }

    binding.buttonThird.setOnClickListener {
      val num = contentResolver.delete(URI_DELETE, null, null)
      binding.textviewThird.text = "Received num: $num"
    }
  }

  private fun getContentValues(): ContentValues {

    val values =
        ContentValues().apply {
          put("uid", 1234567)
          put("packageName", "com.test.test")
          put("appName", "Invalid app")
          put("isSystemApp", 0)
          put("firewallStatus", 0)
          put("appCategory", "Invalid")
          put("wifiDataUsed", 0L)
          put("mobileDataUsed", 0L)
          put("connectionStatus", 0)
          put("screenOffAllowed", 0)
          put("backgroundAllowed", 0)
        }

    Log.d("ContentResolverSample", "getContentValues() contentValues: $values")

    return values
  }
}
