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
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.celzero.interop.databinding.ActivityBlocklistsBinding

class BlocklistsActivity : AppCompatActivity() {

  private lateinit var binding: ActivityBlocklistsBinding

  companion object {
    private const val AUTHORITY = "com.celzero.bravedns.blocklistprovider"

    /** The URI for the blocklists table. */
    private val URI_BLOCKLISTS = Uri.parse("content://$AUTHORITY/blocklists")
  }

  private lateinit var adapter: DnsLogsAdapter
  private lateinit var lm: LoaderManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityBlocklistsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    init()
  }

  private fun init() {

    val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    binding.list.layoutManager = linearLayoutManager
    adapter = DnsLogsAdapter()
    binding.list.adapter = adapter

    adapter.setContentResolver(contentResolver)

    lm = LoaderManager.getInstance(this)
    lm.initLoader(1, null, mLoaderCallbacks)
    contentResolver.registerContentObserver(URI_BLOCKLISTS, true, cb)
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
          // lm.restartLoader(1, null, mLoaderCallbacks)
          super.onChange(selfChange, uri, flags)
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
          return CursorLoader(applicationContext, URI_BLOCKLISTS, arrayOf("queryStr"), null, null, null)
        }

        override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
          Log.d("RethinkInterOp", "onLoadFinished() cursor: $data")
          adapter.setLogs(data)
        }

        override fun onLoaderReset(loader: Loader<Cursor?>) {
          adapter.setLogs(null)
        }
      }

  private class DnsLogsAdapter : RecyclerView.Adapter<DnsLogsAdapter.ViewHolder>() {
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

      Log.w("RethinkInterOp", "onBindViewHolder: $position")

      if (mCursor!!.moveToPosition(position)) {
        val name = mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("vname"))
        val isSelected = mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("isSelected"))
        holder.mText.text = "$name (${isSelected == 1})"
      }

      holder.mText.setOnClickListener {
        if (cr == null) {
          Log.d("RethinkInterOp", "Invalid content resolver for update query")
          return@setOnClickListener
        }

        cr?.update(URI_BLOCKLISTS, getContentValues(position), null, null)
      }
    }

    fun setContentResolver(contentResolver: ContentResolver) {
      this.cr = contentResolver
    }

    private fun getContentValues(position: Int): ContentValues? {
      Log.d("RethinkInterOp", "getContentValues() is cursor available? $mCursor")
      if (mCursor == null) return null

      mCursor!!.moveToPosition(position)
      val id: Int = mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("value"))

      // getContentValues gets called from list view, just to update the isSelected value in
      // Rethink's blocklist database. just toggle the isSelected value
      val isSelected =
          if (mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("isSelected")) == 1) {
            0
          } else {
            1
          }
      Log.d("RethinkInterOp", "for $id, update the selected as $isSelected")
      val values =
          ContentValues().apply {
            put("value", id)
            put("vname", mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("vname")))
            put("uname", mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("uname")))
            put("group", mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("group")))
            put("subg", mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("subg")))
            put("url", mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("url")))
            put("show", mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("show")))
            put("entries", mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("entries")))
            put("pack", mCursor!!.getString(mCursor!!.getColumnIndexOrThrow("pack")))
            put("simpleTagId", mCursor!!.getInt(mCursor!!.getColumnIndexOrThrow("simpleTagId")))
            put("isSelected", isSelected)
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
            LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)) {
      val mText: TextView = itemView.findViewById(android.R.id.text1)
    }
  }
}
