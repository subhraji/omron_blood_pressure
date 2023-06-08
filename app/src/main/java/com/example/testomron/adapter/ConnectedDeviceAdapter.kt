package com.example.testomron.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.testomron.R
import com.example.testomron.activity.OmronConnectedDeviceList
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants

class ConnectedDeviceAdapter(
    private var context: Context?,
    deviceList: List<HashMap<String?, String?>>?
) : BaseAdapter() {
    var mDeviceList: List<HashMap<String?, String?>>? = deviceList
    var mOmronConnectedDeviceList: OmronConnectedDeviceList? = null

    init {
        mOmronConnectedDeviceList = context as OmronConnectedDeviceList?
    }

    override fun getCount(): Int {
        return mDeviceList!!.size
    }

    override fun getItem(position: Int): HashMap<String?, String?> {
        return mDeviceList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val inflator =
            context!!.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val item = getItem(position)
        val v: View
        v = createContentView(convertView, inflator, item, parent!!)!!
        return v
    }

    private fun createContentView(
        convertView: View?,
        inflator: LayoutInflater,
        item: java.util.HashMap<String?, String?>,
        parent: ViewGroup
    ): View? {
        var v = convertView
        val holder: ViewHolder
        if (convertView == null) {
            v = inflator.inflate(R.layout.list_item_device_listing, null)
            holder = ViewHolder()
            holder.tvMdelName = v!!.findViewById<View>(R.id.tv_model_name) as TextView
            holder.tvDeviceSeries = v.findViewById<View>(R.id.tv_device_series) as TextView
            holder.llBg = v.findViewById<View>(R.id.ll_bg) as LinearLayout
            v.tag = holder
        } else {
            holder = v!!.tag as ViewHolder
        }
        holder.tvMdelName!!.text = item[OmronConstants.OMRONBLEConfigDevice.ModelDisplayName]
        /*if (item[OmronConstants.OMRONBLEConfigDevice.ModelDisplayName].equals(
                "Connected Devices",
                ignoreCase = true
            )
        ) {
            val preferencesManager = PreferencesManager(context)
            val count =
                "Count : " + if (preferencesManager.getSavedDeviceList() != null) preferencesManager.getSavedDeviceList()
                    .size() else 0
            holder.tvDeviceSeries!!.text = count
        } else {
            holder.tvDeviceSeries!!.text = item[OmronConstants.OMRONBLEConfigDevice.Identifier]
        }*/
        /*if (item[OmronConstants.OMRONBLEConfigDevice.Thumbnail] != null) {
            val res = context!!.resources
            val resourceId = res.getIdentifier(
                item[OmronConstants.OMRONBLEConfigDevice.Thumbnail],
                "drawable",
                context!!.packageName
            )
            holder.ivInfo!!.setImageResource(resourceId)
        } else {
            holder.ivInfo!!.setImageResource(R.drawable.ic_info_outline)
        }*/
        holder.llBg!!.setOnClickListener { mOmronConnectedDeviceList?.selectDevice(item) }
        holder.llBg!!.setOnLongClickListener {
            mOmronConnectedDeviceList?.showDeviceInfo(item)
            return@setOnLongClickListener true
        }
        return v
    }

    class ViewHolder {
        var tvMdelName: TextView? = null
        var tvDeviceSeries: TextView? = null
        var llBg: LinearLayout? = null
        var ivInfo: ImageView? = null
    }
}