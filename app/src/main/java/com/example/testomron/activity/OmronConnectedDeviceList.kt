package com.example.testomron.activity

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.testomron.App
import com.example.testomron.MainActivity
import com.example.testomron.R
import com.example.testomron.adapter.ConnectedDeviceAdapter
import com.example.testomron.databinding.ActivityOmronConnectedDeviceListBinding
import com.example.testomron.utility.Constants
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants

class OmronConnectedDeviceList : BaseActivity() {
    private lateinit var binding: ActivityOmronConnectedDeviceListBinding
    val TAG = "DeviceList"

    private var mListView: ListView? = null
    var mConnectedDeviceAdapter: ConnectedDeviceAdapter? = null
    private var mContext: Context? = null
    var fullDeviceList: List<HashMap<String?, String?>>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityOmronConnectedDeviceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this
        fullDeviceList = ArrayList()


        initViews()
        initClickListeners()
        reloadConfiguration()


        if (Build.VERSION.SDK_INT < 31) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    100
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    100
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {

                    // permission was granted,
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                    }
                } else {
                }
            }
        }
    }

    private val mMessageReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (this != null) LocalBroadcastManager.getInstance(this@OmronConnectedDeviceList)
                .unregisterReceiver(
                    this
                )

            // Get extra data included in the Intent
            val status = intent.getIntExtra(OmronConstants.OMRONConfigurationStatusKey, 0)
            if (status == OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileSuccess) {
                Log.d(TAG, "Config File Extract Success")
                loadDeviceList()
            } else if (status == OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileError) {
                Log.d(TAG, "Config File Extract Failure")
                showErrorLoadingDevices()
            } else if (status == OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileUpdateError) {
                Log.d(TAG, "Config File Update Failure")
                showErrorLoadingDevices()
            }
        }
    }

    // UI initializers
    private fun initViews() {
        mListView = findViewById(R.id.lv_devicelist) as ListView
    }

    private fun initClickListeners() {
        /*findViewById(R.id.iv_info).setOnClickListener(View.OnClickListener { showLibraryKeyDialog() })
        findViewById(R.id.iv_vital_data).setOnClickListener(View.OnClickListener {
            val toVitalData = Intent(
                this@OmronConnectedDeviceList,
                DataDeviceListingActivity::class.java
            )
            startActivity(toVitalData)
        })*/
    }

    private fun loadDeviceList() {
        fullDeviceList = java.util.ArrayList()
        val ctx: Context = App.getInstance().getApplicationContext()
        if (OmronPeripheralManager.sharedManager(ctx).retrieveManagerConfiguration(ctx) != null) {
            fullDeviceList = OmronPeripheralManager.sharedManager(ctx).retrieveManagerConfiguration(ctx)[OmronConstants.OMRONBLEConfigDeviceKey] as List<HashMap<String?, String?>>
            mConnectedDeviceAdapter = ConnectedDeviceAdapter(mContext, fullDeviceList)
            mListView!!.adapter = mConnectedDeviceAdapter
            mListView!!.divider = null
            mConnectedDeviceAdapter!!.notifyDataSetChanged()
        }
        showErrorLoadingDevices()
    }

    private fun reloadConfiguration() {
        // OmronConnectivityLibrary initialization and Api key setup.
        OmronPeripheralManager.sharedManager(this)
            .setAPIKey(getString(R.string.api_key), null)

        // Notification Listener for Configuration Availability
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver!!,
            IntentFilter(OmronConstants.OMRONBLEConfigDeviceAvailabilityNotification)
        )
    }

    private fun showErrorLoadingDevices() {
        if (fullDeviceList!!.size == 0) {
            val information =
                "Invalid Library API key configured\nOR\nNo devices supported for API Key\n\nPlease try again using ⓘ button. "
            val alertDialogBuilder = AlertDialog.Builder(mContext)
            alertDialogBuilder.setTitle("Info")
            alertDialogBuilder.setMessage(information)
            alertDialogBuilder.setNeutralButton(
                "OK"
            ) { arg0, arg1 -> }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

    fun showDeviceInfo(item: HashMap<String?, String?>) {

        // TODO: Navigate to new screen for device details
        val deviceInfo = """
             Category : ${item[OmronConstants.OMRONBLEConfigDevice.GroupID]}
             Model Type : ${item[OmronConstants.OMRONBLEConfigDevice.GroupIncludedGroupID]}
             """.trimIndent()
        val alertDialogBuilder = AlertDialog.Builder(mContext)
        alertDialogBuilder.setTitle("Info")
        alertDialogBuilder.setMessage(deviceInfo)
        alertDialogBuilder.setNeutralButton(
            "OK"
        ) { arg0, arg1 -> }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun selectDevice(item: HashMap<String?, String?>) {
        if (item[OmronConstants.OMRONBLEConfigDevice.Category]!!.toInt() == OmronConstants.OMRONBLEDeviceCategory.BLOODPRESSURE) {
            val noOfUsers = item[OmronConstants.OMRONBLEConfigDevice.Users]!!.toInt()
            // Category 0 is for Blood Pressure Devices
            if (noOfUsers > 1) {
                /*val toMain = Intent(this@OmronConnectedDeviceList, SelectUserActivity::class.java)
                toMain.putExtra(Constants.extraKeys.KEY_SELECTED_DEVICE, item)
                startActivity(toMain)*/
                Toast.makeText(this,"more => ${noOfUsers}",Toast.LENGTH_SHORT).show()
            } else if (noOfUsers == 1) {
                Toast.makeText(this,"less => ${noOfUsers}",Toast.LENGTH_SHORT).show()

                val toMain = Intent(this@OmronConnectedDeviceList, MainActivity::class.java)
                toMain.putExtra(Constants.extraKeys.KEY_SELECTED_DEVICE, item)
                startActivity(toMain)
            }
        }
    }


}