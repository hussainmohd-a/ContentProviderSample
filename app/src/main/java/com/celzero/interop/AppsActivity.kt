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
import com.celzero.interop.databinding.ActivityAppsBinding

class AppsActivity : AppCompatActivity() {

  private lateinit var binding: ActivityAppsBinding

  companion object {
    private const val AUTHORITY = "com.celzero.bravedns.appprovider"

    /** The URI for the blocklists table. */
    private val APP_URI = Uri.parse("content://$AUTHORITY/apps")
  }

  private lateinit var adapter: AppsAdapter
  private lateinit var lm: LoaderManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityAppsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    init()
  }

  private fun init() {
    val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    binding.list.layoutManager = linearLayoutManager
    adapter = AppsAdapter()
    binding.list.adapter = adapter

    adapter.setContentResolver(contentResolver)

    lm = LoaderManager.getInstance(this)
    lm.initLoader(1, null, mLoaderCallbacks)
    contentResolver.registerContentObserver(APP_URI, true, cb)
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
          return CursorLoader(applicationContext, APP_URI, arrayOf("queryStr"), null, null, null)
        }

        override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
          Log.d("RethinkInterOp", "onLoadFinished() cursor: $data")
          adapter.setLogs(data)
        }

        override fun onLoaderReset(loader: Loader<Cursor?>) {
          adapter.setLogs(null)
        }
      }

  private class AppsAdapter : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {
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
        val name = mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("appName"))
        val firewallStatus = mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("firewallStatus"))
        Log.w("RethinkInterOp", "app: $name, $firewallStatus")
        holder.textView.text = name
        holder.toggleButton.isChecked = firewallStatus == 1
      }

      holder.toggleButton.setOnClickListener {
        if (cr == null) {
          Log.d("RethinkInterOp", "Invalid content resolver for update query")
          return@setOnClickListener
        }
        holder.toggleButton.isChecked = !holder.toggleButton.isChecked
        cr?.update(APP_URI, getContentValues(position), null, null)
      }

      holder.textView.setOnClickListener {
        if (cr == null) {
          Log.d("RethinkInterOp", "Invalid content resolver for update query")
          return@setOnClickListener
        }

        cr?.update(APP_URI, getContentValues(position), null, null)
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
          if (mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("firewallStatus")) == 0) {
            1
          } else {
            0
          }
      Log.d("RethinkInterOp", "Update firewall status to $status? $uid")
      val values =
          ContentValues().apply {
            put("uid", uid)
            put("packageName", mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("packageName")))
            put("appName", mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("appName")))
            put("isSystemApp", mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("isSystemApp")))
            put("firewallStatus", status)
            put("appCategory", mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("appCategory")))
            put("wifiDataUsed", mCursor!!.getLong(mCursor!!.getColumnIndexOrThrow("wifiDataUsed")))
            put(
                "mobileDataUsed",
                mCursor!!.getLong(mCursor!!.getColumnIndexOrThrow("mobileDataUsed")))
            put("metered", mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("metered")))
            put(
                "screenOffAllowed",
                mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("screenOffAllowed")))
            put(
                "backgroundAllowed",
                mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("backgroundAllowed")))
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
