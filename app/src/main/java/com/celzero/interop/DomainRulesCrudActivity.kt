package com.celzero.interop

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.celzero.interop.databinding.ActivityDomainRulesCrudBinding

class DomainRulesCrudActivity : AppCompatActivity() {
  private lateinit var binding: ActivityDomainRulesCrudBinding
  private val AUTHORITY = "com.celzero.bravedns.domainrulesprovider"
  private val URI_DOMAIN_RULES = Uri.parse("content://${AUTHORITY}/domainrules")

  private val URI_DELETE = Uri.parse("content://${AUTHORITY}/domainrules/delete")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityDomainRulesCrudBinding.inflate(layoutInflater)
    setContentView(binding.root)

    init()
  }

  private fun init() {
    binding.buttonFirst.setOnClickListener {
      val cursor = contentResolver.query(URI_DOMAIN_RULES, null, null, null, null)
      binding.textviewFirst.text = cursor!!.count.toString()
      cursor.close()
    }

    binding.buttonSecond.setOnClickListener {
      val uri = contentResolver.insert(URI_DOMAIN_RULES, getContentValues())
      binding.textviewSecond.text = uri?.toString() ?: "Empty"
    }

    binding.buttonThird.setOnClickListener {
      val num =
          contentResolver.delete(
              URI_DELETE, "domain = ? and uid = ?", arrayOf("www.amazon.in", "-1000"))
      binding.textviewThird.text = "Received count: $num"
    }
  }

  private fun getContentValues(): ContentValues {

    val values =
        ContentValues().apply {
          put("domain", "www.amazon.in")
          put("uid", -1000)
          put("ips", "")
          put("status", 1 /* Block */)
          put("type", 0)
          put("modifiedTs", System.currentTimeMillis())
          put("deletedTs", System.currentTimeMillis())
          put("version", 0L)
        }

    Log.d("ContentResolverSample", "getContentValues() contentValues: $values")

    return values
  }
}
