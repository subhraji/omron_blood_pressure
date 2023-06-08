package com.example.testomron.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.testomron.R
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral

class ScannedDevicesAdapter(
    private var context: Context?,
    peripheralList: ArrayList<OmronPeripheral>?
) : BaseAdapter() {
    private var mPeripheralList: ArrayList<OmronPeripheral>? = peripheralList

    fun setPeripheralList(peripheralList: ArrayList<OmronPeripheral>?) {
        mPeripheralList = peripheralList
    }

    override fun getCount(): Int {
        return mPeripheralList!!.size
    }

    override fun getItem(position: Int): OmronPeripheral {
        return mPeripheralList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val inflator =
            context!!.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val item = getItem(position)
        val v: View
        v = createContentView(inflator, item, parent)
        return v
    }

    private fun createContentView(
        inflator: LayoutInflater,
        item: OmronPeripheral,
        parent: ViewGroup
    ): View {
        val v: View = inflator.inflate(R.layout.list_item_device_listing, parent, false)
        val tvMdelName = v.findViewById<View>(R.id.tv_model_name) as TextView
        val tvDeviceSeries = v.findViewById<View>(R.id.tv_device_series) as TextView
        tvMdelName.text = item.localName
        tvDeviceSeries.text = """
            ${item.uuid}
            RSSI : ${item.rssi}
            """.trimIndent()
        return v
    }
}