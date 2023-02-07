package com.celzero.interop

import android.content.ContentResolver
import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.celzero.interop.databinding.ActivityDomainRulesBinding

class DomainRulesActivity : AppCompatActivity() {

  private lateinit var binding: ActivityDomainRulesBinding

  companion object {
    private const val AUTHORITY = "com.celzero.bravedns.domainrulesprovider"

    /** The URI for the blocklists table. */
    private val DOMAIN_RULES_URI = Uri.parse("content://$AUTHORITY/domainrules")
  }

  private lateinit var adapter: DomainRulesAdapter
  private lateinit var lm: LoaderManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityDomainRulesBinding.inflate(layoutInflater)
    setContentView(binding.root)

    init()
  }

  private fun init() {
    val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    binding.list.layoutManager = linearLayoutManager
    adapter = DomainRulesAdapter()
    binding.list.adapter = adapter

    adapter.setContentResolver(contentResolver)

    lm = LoaderManager.getInstance(this)
    lm.initLoader(1, null, mLoaderCallbacks)
    contentResolver.registerContentObserver(DOMAIN_RULES_URI, true, cb)
  }

  private val cb: ContentObserver =
      object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
          adapter.notifyDataSetChanged()
          super.onChange(selfChange)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
          adapter.notifyDataSetChanged()
          super.onChange(selfChange, uri)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
          adapter.notifyItemChanged(0)
          adapter.notifyDataSetChanged()
          lm.restartLoader(1, null, mLoaderCallbacks)
          // super.onChange(selfChange, uri, flags)
        }

        override fun onChange(selfChange: Boolean, uris: MutableCollection<Uri>, flags: Int) {
          adapter.notifyDataSetChanged()
          super.onChange(selfChange, uris, flags)
        }
      }

  private val mLoaderCallbacks: LoaderManager.LoaderCallbacks<Cursor?> =
      object : LoaderManager.LoaderCallbacks<Cursor?> {
        override fun onCreateLoader(id: Int, @Nullable args: Bundle?): Loader<Cursor?> {
          adapter.setContentResolver(contentResolver)
          return CursorLoader(applicationContext, DOMAIN_RULES_URI, null, null, null, null)
        }

        override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
          Log.d("RethinkInterOp", "onLoadFinished() cursor: $data")
          adapter.setLogs(data)
        }

        override fun onLoaderReset(loader: Loader<Cursor?>) {
          adapter.setLogs(null)
        }
      }

  private class DomainRulesAdapter : RecyclerView.Adapter<DomainRulesAdapter.ViewHolder>() {
    private var mCursor: Cursor? = null
    private var cr: ContentResolver? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      if (mCursor == null) {
        Log.w("RethinkInterOp", "Error: Cursor is null")
        return
      }

      if (mCursor!!.moveToPosition(position)) {
        val name = mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("domain"))
        val status = mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("status"))
        Log.w("RethinkInterOp", "app: $name, $status")
        holder.textView.text = name
        holder.toggleButton.isChecked = status == 2
      }

      holder.toggleButton.setOnClickListener {
        if (cr == null) {
          Log.d("RethinkInterOp", "Invalid content resolver for update query")
          return@setOnClickListener
        }
        holder.toggleButton.isChecked = !holder.toggleButton.isChecked
        cr?.update(DOMAIN_RULES_URI, getContentValues(position), null, null)
      }

      holder.textView.setOnClickListener {
        if (cr == null) {
          Log.d("RethinkInterOp", "Invalid content resolver for update query")
          return@setOnClickListener
        }

        cr?.update(DOMAIN_RULES_URI, getContentValues(position), null, null)
      }
    }

    fun setContentResolver(contentResolver: ContentResolver) {
      this.cr = contentResolver
    }

    private fun getContentValues(position: Int): ContentValues? {
      Log.d("RethinkInterOp", "getContentValues() is cursor available? $mCursor")
      if (mCursor == null) return null

      mCursor!!.moveToPosition(position)
      val uid: Int = mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("uid"))

      // getContentValues gets called from list view, just to update the isSelected value in
      // Rethink's blocklist database. just toggle the isSelected value
      val status =
          if (mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("status")) == 0) {
            1
          } else {
            2
          }
      Log.d("RethinkInterOp", "Update firewall status to $status? $uid")
      val values =
          ContentValues().apply {
            put("uid", uid)
            put("domain", mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("domain")))
            put("ips", mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("ips")))
            put("status", status)
            put("type", mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("type")))
            put("modifiedTs", mCursor!!.getLong(mCursor!!.getColumnIndexOrThrow("modifiedTs")))
            put("deletedTs", mCursor!!.getLong(mCursor!!.getColumnIndexOrThrow("deletedTs")))
            put(
                "version",
                mCursor!!.getLong(mCursor!!.getColumnIndexOrThrow("version")))
          }

      Log.d("RethinkInterOp", "getContentValues() contentValues: $values")

      return values
    }

    override fun getItemCount(): Int {
      return if (mCursor == null) 0 else mCursor!!.count
    }

    fun setLogs(cursor: Cursor?) {
      if (cursor == null) return

      mCursor = cursor
      Log.d("RethinkInterOp", "setLogs() cursor: $cursor")
      notifyItemRangeChanged(0, mCursor!!.count)
      notifyDataSetChanged()
    }

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_view_item, parent, false)) {
      val textView: TextView = itemView.findViewById(R.id.textView)
      val toggleButton: ToggleButton = itemView.findViewById(R.id.toggleButton)
    }
  }
}
