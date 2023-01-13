package com.celzero.interop

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.celzero.interop.databinding.ActivityCrudBinding

class BlocklistsCrudActivity : AppCompatActivity() {

  private lateinit var binding: ActivityCrudBinding

  private val AUTHORITY = "com.celzero.bravedns.contentprovider"
  private val URI_SAMPLE = Uri.parse("content://${AUTHORITY}/blocklists")

  private val URI_DELETE = Uri.parse("content://${AUTHORITY}/blocklists/1340")
  private val GET_STAMP = "content://${AUTHORITY}/blocklists/stamp/get"
  private val UPDATE_STAMP = "content://$AUTHORITY/blocklists/stamp/update"

  private val sampleStamp = "1:4AIAgAABAHAgAA=="
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityCrudBinding.inflate(layoutInflater)
    setContentView(binding.root)

    init()
  }

  private fun init() {
    binding.buttonFirst.setOnClickListener {
      val cursor = contentResolver.query(URI_SAMPLE, null, null, null, null)
      if (cursor!!.moveToFirst()) {
        val strBuild = StringBuilder()
        while (!cursor.isAfterLast) {
          val value = cursor.getColumnIndex("value")
          strBuild.append("${cursor.getString(value)}-".trimIndent())
          cursor.moveToNext()
        }
        binding.textviewFirst.text = strBuild
      } else {
        binding.textviewFirst.text = "No Records Found"
      }
    }

    binding.buttonSecond.setOnClickListener {
      val uri = contentResolver.insert(URI_SAMPLE, getContentValues())
      binding.textviewSecond.text = uri?.toString() ?: "Empty"
    }

    binding.buttonThird.setOnClickListener {
      val num = contentResolver.delete(URI_DELETE, null, null)
      binding.textviewThird.text = "Received num: $num"
    }

    binding.buttonFourth.setOnClickListener {
      val bundle = contentResolver.call(URI_SAMPLE, GET_STAMP, null, null)
      binding.textviewFourth.text = bundle?.getString("stamp") ?: "Empty"
    }

    binding.buttonFifth.setOnClickListener {
      val bundle = Bundle()
      bundle.putString("stamp", sampleStamp)
      contentResolver.call(URI_SAMPLE, UPDATE_STAMP, sampleStamp, bundle)
    }
  }

  private fun getContentValues(): ContentValues {

    val values =
        ContentValues().apply {
          put("value", 1340)
          put("vname", "Test")
          put("uname", "Test")
          put("group", "Test")
          put("subg", "Test")
          put("url", "Test")
          put("show", 1)
          put("entries", 1)
          put("pack", "Test")
          put("simpleTagId", 1)
          put("isSelected", 0)
        }

    Log.d("ContentResolverSample", "getContentValues() contentValues: $values")

    return values
  }
}
