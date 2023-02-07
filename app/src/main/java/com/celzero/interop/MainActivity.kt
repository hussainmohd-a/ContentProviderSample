package com.celzero.interop

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.celzero.interop.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    init()
  }

  private fun init() {
    binding.btnBlocklistsObserver.setOnClickListener { launchBlocklistsActivity() }
    binding.btnBlocklistsCrud.setOnClickListener { launchBlocklistCrudActivity() }
    binding.btnAppsObserver.setOnClickListener { launchAppsActivity() }
    binding.btnAppsCrud.setOnClickListener { launchAppsCrudActivity() }
    binding.btnDomainsObserver.setOnClickListener { launchDomainRulesActivity() }
    binding.btnDomainsCrud.setOnClickListener { launchDomainRulesCrudActivity() }
  }

  private fun launchBlocklistsActivity() {
    val intent = Intent(this, BlocklistsActivity::class.java)
    startActivity(intent)
  }

  private fun launchBlocklistCrudActivity() {
    val intent = Intent(this, BlocklistsCrudActivity::class.java)
    startActivity(intent)
  }

  private fun launchAppsActivity() {
    val intent = Intent(this, AppsActivity::class.java)
    startActivity(intent)
  }

  private fun launchAppsCrudActivity() {
    val intent = Intent(this, AppsCrudActivity::class.java)
    startActivity(intent)
  }

  private fun launchDomainRulesActivity() {
    val intent = Intent(this, DomainRulesActivity::class.java)
    startActivity(intent)
  }

  private fun launchDomainRulesCrudActivity() {
    val intent = Intent(this, DomainRulesCrudActivity::class.java)
    startActivity(intent)
  }
}
