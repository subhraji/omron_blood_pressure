package com.example.testomron

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.testomron.adapter.ScannedDevicesAdapter
import com.example.testomron.database.OmronDBConstans
import com.example.testomron.databinding.ActivityMainBinding
import com.example.testomron.utility.Constants
import com.example.testomron.utility.PreferencesManager
import com.example.testomron.utility.Utilities
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.DeviceConfiguration.OmronPeripheralManagerConfig
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Interface.*
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronErrorInfo
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mContext: Context? = null
    private val TAG = "OmronSampleApp"

    private var mLvScannedList: ListView? = null
    private var mPeripheralList: ArrayList<OmronPeripheral>? = null
    private var mScannedDevicesAdapter: ScannedDevicesAdapter? = null
    private var mSelectedPeripheral: OmronPeripheral? = null

    private var selectedUsers = ArrayList<Int?>()

    private var mRlDeviceListView: RelativeLayout? = null
    private  var mRlTransferView:RelativeLayout? = null
    private var mTvTImeStamp: TextView? = null
    private  var mTvSystolic:TextView? = null
    private  var mTvDiastolic:TextView? = null
    private  var mTvPulseRate:TextView? = null
    private  var mTvUserSelected:TextView? = null
    private var mTvDeviceInfo: TextView? = null
    private  var mTvDeviceLocalName:TextView? = null
    private  var mTvDeviceUuid:TextView? = null
    private  var mTvStatusLabel:TextView? = null
    private  var mTvErrorCode:TextView? = null
    private  var mTvErrorDesc:TextView? = null
    private var mProgressBar: ProgressBar? = null
    
    private val preferencesManager: PreferencesManager? = null

    private var scanBtn: Button? = null
    private var transferBtn: Button? = null

    var device: HashMap<String?, String?>? = null
    var personalSettings: HashMap<String?, String?>? = null

    private var isScan: Boolean? = null

    private val TIME_INTERVAL = 1000

    var mHandler: Handler? = null
    var mRunnable: Runnable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this

        isScan = false

        /*// Selected users
        selectedUsers =
            (intent.getSerializableExtra(Constants.extraKeys.KEY_SELECTED_USER) as ArrayList<Int?>?)!!
        if (selectedUsers == null) {
            selectedUsers = ArrayList()
            selectedUsers.add(1)
        }*/

        // Selected device
        device =
            intent.getSerializableExtra(Constants.extraKeys.KEY_SELECTED_DEVICE) as HashMap<String?, String?>?

        //Personal settings like height, weight etc for activity devices.
        personalSettings =
            intent.getSerializableExtra(Constants.extraKeys.KEY_PERSONAL_SETTINGS) as HashMap<String?, String?>?

        initViews()
        showDeviceListView()
        initClickListeners()
        initLists()

        // Permissions for HeartGuide devices
        requestPermissions()

        // Start OmronPeripheralManager
        startOmronPeripheralManager(false, true)
    }

    override fun onResume() {
        super.onResume()


        // Activity Tracker - Testing of notification
        if (device!![OmronConstants.OMRONBLEConfigDevice.Category]!!.toInt() == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY) {
            Utilities.scheduleNotification(Utilities.getNotification("Test Notification"), 5000)
        }
    }

    private fun requestPermissions() {

        // Activity Tracker
        if (device!![OmronConstants.OMRONBLEConfigDevice.Category]!!.toInt() == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    1
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults == null || grantResults.size <= 0) {
            return
        }
        when (requestCode) {
            1 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CALL_LOG),
                    2
                )
            }
            2 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    3
                )
            }
            3 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_SMS),
                    4
                )
            }
            4 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.RECEIVE_SMS),
                    5
                )
            }
        }
    }

    private fun startOmronPeripheralManager(isHistoricDataRead: Boolean, isPairing: Boolean) {
        val peripheralConfig =
            OmronPeripheralManager.sharedManager(App.getInstance().applicationContext).configuration
        Log.d(TAG, "Library Identifier : " + peripheralConfig.libraryIdentifier)

        // Filter device to scan and connect (optional)
        if (device != null && device!![OmronConstants.OMRONBLEConfigDevice.GroupID] != null && device!![OmronConstants.OMRONBLEConfigDevice.GroupIncludedGroupID] != null) {

            // Add item
            val filterDevices: MutableList<HashMap<String?, String?>?> =
                ArrayList()
            filterDevices.add(device)
            OmronPeripheralManagerConfig.deviceFilters = filterDevices
        }
        var deviceSettings: ArrayList<HashMap<*, *>?>? = ArrayList()

        // Blood pressure settings (optional)
        deviceSettings = getBloodPressureSettings(deviceSettings, isPairing)

        OmronPeripheralManagerConfig.deviceSettings = deviceSettings

        // Set Scan timeout interval (optional)
        OmronPeripheralManagerConfig.timeoutInterval = Constants.CONNECTION_TIMEOUT
        // Set User Hash Id (mandatory)
        OmronPeripheralManagerConfig.userHashId =
            "<email_address_of_user>" // Set logged in user email

        // Disclaimer: Read definition before usage
        if (device!![OmronConstants.OMRONBLEConfigDevice.Category]!!.toInt() != OmronConstants.OMRONBLEDeviceCategory.ACTIVITY) {
            // Reads all data from device.
            OmronPeripheralManagerConfig.enableAllDataRead = isHistoricDataRead
        }

        // Pass the last sequence number of reading  tracked by app - "SequenceKey" for each vital data
        val sequenceNumbersForTransfer = HashMap<Int, Int>()
        sequenceNumbersForTransfer[1] = 42
        sequenceNumbersForTransfer[2] = 8
        OmronPeripheralManagerConfig.sequenceNumbersForTransfer = sequenceNumbersForTransfer

        // Set configuration for OmronPeripheralManager
        OmronPeripheralManager.sharedManager(App.getInstance().applicationContext).configuration =
            peripheralConfig

        //Initialize the connection process.
        OmronPeripheralManager.sharedManager(App.getInstance().applicationContext).startManager()

        // Notification Listener for BLE State Change
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver,
            IntentFilter(OmronConstants.OMRONBLECentralManagerDidUpdateStateNotification)
        )
    }

    private fun startScanning() {

        // Start OmronPeripheralManager
        startOmronPeripheralManager(false, true)

        // Set State Change Listener
        setStateChanges()
        if (isScan!!) {
            scanBtn!!.text = "SCAN"

            // Stop Scanning for Devices using OmronPeripheralManager
            OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
                .stopScanPeripherals { resultInfo ->
                    runOnUiThread {
                        if (resultInfo.resultCode == 0) {
                            resetDeviceList()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                """
                            Error Code : ${resultInfo.resultCode}
                            Error Detail Code : ${resultInfo.detailInfo}
                            """.trimIndent(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        enableDisableButton(true)
                    }
                }
        } else {
            resetVitalDataResult()
            showDeviceListView()
            resetErrorMessage()
            mProgressBar!!.visibility = View.VISIBLE
            mTvDeviceLocalName!!.text = ""
            mTvDeviceUuid!!.text = ""
            scanBtn!!.text = "STOP SCAN"

            // Start Scanning for Devices using OmronPeripheralManager
            OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
                .startScanPeripherals { peripheralList, resultInfo ->
                    runOnUiThread {
                        if (resultInfo.resultCode == 0) {
                            mPeripheralList = peripheralList
                            if (mScannedDevicesAdapter != null) {
                                mScannedDevicesAdapter?.setPeripheralList(mPeripheralList)
                                mScannedDevicesAdapter?.notifyDataSetChanged()
                            }
                        } else {
                            isScan = !isScan!!
                            scanBtn!!.text = "SCAN"
                            showTransferView()
                            resetErrorMessage()
                            enableDisableButton(true)
                            resetDeviceList()
                            mTvErrorCode!!.text =
                                resultInfo.resultCode.toString() + " / " + resultInfo.detailInfo
                            mTvErrorDesc!!.text = resultInfo.messageInfo
                        }
                        enableDisableButton(true)
                    }
                }
        }
        isScan = !isScan!!
    }


    private fun stopOmronPeripheralManager() {
        OmronPeripheralManager.sharedManager(App.getInstance().applicationContext).stopManager()
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            // Get extra data included in the Intent
            val status = intent.getIntExtra(OmronConstants.OMRONBLEBluetoothStateKey, 0)
            if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateUnknown) {
                Log.d(TAG, "Bluetooth is in unknown state")
            } else if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateOff) {
                Log.d(TAG, "Bluetooth is currently powered off")
            } else if (status == OmronConstants.OMRONBLEBluetoothState.OMRONBLEBluetoothStateOn) {
                Log.d(TAG, "Bluetooth is currently powered on")
            }
        }
    }

    private fun setStateChanges() {

        // Listen to Device state changes using OmronPeripheralManager
        OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
            .onConnectStateChange { state ->
                runOnUiThread {
                    var status = "-"
                    if (state == OmronConstants.OMRONBLEConnectionState.CONNECTING) {
                        status = "Connecting..."
                    } else if (state == OmronConstants.OMRONBLEConnectionState.CONNECTED) {
                        status = "Connected"
                    } else if (state == OmronConstants.OMRONBLEConnectionState.DISCONNECTING) {
                        status = "Disconnecting..."
                    } else if (state == OmronConstants.OMRONBLEConnectionState.DISCONNECTED) {
                        status = "Disconnected"
                    }
                    setStatus(status)
                }
            }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_BACK ->
                    // Stop Scanning for Devices using OmronPeripheralManager
                    OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
                        .stopScanPeripherals { runOnUiThread { resetDeviceList() } }
                else -> {}
            }
        }
        return super.dispatchKeyEvent(event)
    }


    /**
     * Method to connect to the device
     *
     * @param omronPeripheral
     */
    private fun connectPeripheral(omronPeripheral: OmronPeripheral) {
        initLists()
        isScan = false
        mSelectedPeripheral = omronPeripheral
        scanBtn!!.text = "SCAN"
        runOnUiThread {
            showTransferView()
            resetErrorMessage()
            resetVitalDataResult()
            enableDisableButton(false)
            setStatus("Connecting...")

            // Pair to Device using OmronPeripheralManager
            OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
                .connectPeripheral(
                    omronPeripheral
                ) { peripheral, resultInfo ->
                    connectionUpdateWithPeripheral(
                        peripheral,
                        resultInfo,
                        false
                    )
                }
        }
    }

    /**
     * Method  to connect to device. Method has option to have few seconds wait to select the user
     *
     * @param omronPeripheral
     */
    private fun connectPeripheralWithWait(omronPeripheral: OmronPeripheral) {
        initLists()
        isScan = false
        mSelectedPeripheral = omronPeripheral
        scanBtn!!.text = "SCAN"
        runOnUiThread {
            showTransferView()
            resetErrorMessage()
            resetVitalDataResult()
            enableDisableButton(false)
            setStatus("Connecting...")

            // Pair to Device using OmronPeripheralManager
            OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
                .connectPeripheral(
                    omronPeripheral, true
                ) { peripheral, resultInfo ->
                    connectionUpdateWithPeripheral(
                        peripheral,
                        resultInfo,
                        true
                    )
                }
        }
    }

    /**
     * Method to resume the connection after the wait is over
     *
     * @param omronPeripheral
     */
    private fun resumeConnection(omronPeripheral: OmronPeripheral) {
        if (selectedUsers.size > 1) {
            OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
                .resumeConnectPeripheral(
                    omronPeripheral, selectedUsers
                ) { peripheral, resultInfo ->
                    connectionUpdateWithPeripheral(
                        peripheral,
                        resultInfo,
                        false
                    )
                }
        } else {
            OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
                .resumeConnectPeripheral(
                    omronPeripheral, ArrayList(Arrays.asList(selectedUsers[0]))
                ) { peripheral, resultInfo ->
                    connectionUpdateWithPeripheral(
                        peripheral,
                        resultInfo,
                        false
                    )
                }
        }
    }

    private fun connectionUpdateWithPeripheral(
        peripheral: OmronPeripheral?,
        resultInfo: OmronErrorInfo,
        wait: Boolean
    ) {
        runOnUiThread {
            if (resultInfo.resultCode == 0 && peripheral != null) {
                mSelectedPeripheral = peripheral
                if (null != peripheral.localName) {
                    mTvDeviceLocalName!!.text = peripheral.localName
                    mTvDeviceUuid!!.text = peripheral.uuid
                    val deviceInformation = peripheral.deviceInformation
                    Log.d(TAG, "Device Information : $deviceInformation")
                    val deviceSettings = mSelectedPeripheral!!.getDeviceSettings()
                    if (deviceSettings != null) {
                        Log.d(TAG, "Device Settings:$deviceSettings")
                    }
                    val peripheralConfig =
                        OmronPeripheralManager.sharedManager(App.getInstance().applicationContext).configuration
                    Log.d(
                        TAG,
                        "Device Config :  " + peripheralConfig.getDeviceConfigGroupIdAndGroupIncludedId(
                            peripheral.deviceGroupIDKey,
                            peripheral.deviceGroupIncludedGroupIDKey
                        )
                    )
                    if (wait) {
                        mHandler = Handler()
                        mHandler!!.postDelayed({ resumeConnection(peripheral) }, 5000)
                    } else {
                        if (peripheral.vitalData != null) {
                            Log.d(TAG, "Vital data - " + peripheral.vitalData.toString())
                        }
                        showMessage(
                            getString(R.string.device_connected),
                            getString(R.string.device_paired)
                        )
                    }
                }
            } else {
                setStatus("-")
                mTvErrorCode!!.text = resultInfo.detailInfo
                mTvErrorDesc!!.text = resultInfo.messageInfo
            }
            enableDisableButton(true)
        }
    }

    /**
     * Method to end the connection with device
     */
    private fun endConnection() {
        OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
            .endConnectPeripheral { peripheral, resultInfo ->
                runOnUiThread {
                    if (resultInfo.resultCode == 0 && peripheral != null) {
                        mSelectedPeripheral = peripheral
                        if (null != peripheral.localName) {
                            mTvDeviceLocalName!!.text = peripheral.localName
                            mTvDeviceUuid!!.text = peripheral.uuid
                            showMessage(
                                getString(R.string.device_connected),
                                getString(R.string.device_paired)
                            )
                        }
                    } else {
                        setStatus("-")
                        mTvErrorCode!!.text = resultInfo.detailInfo
                        mTvErrorDesc!!.text = resultInfo.messageInfo
                    }
                    enableDisableButton(true)
                }
            }
    }

    private fun disconnectDevice() {

        // Disconnect device using OmronPeripheralManager
        OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
            .disconnectPeripheral(
                mSelectedPeripheral
            ) { peripheral, resultInfo ->
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Device disconnected", Toast.LENGTH_SHORT)
                        .show()
                    enableDisableButton(true)
                }
            }
    }

    /*
        OmronPeripheralManager Transfer Function
     */

    /*
        OmronPeripheralManager Transfer Function
     */
    private fun transferData() {
        resetErrorMessage()
        enableDisableButton(false)
        resetVitalDataResult()
        if (mSelectedPeripheral == null) {
            mTvErrorDesc!!.text = "Device Not Paired"
            return
        }

        // Disclaimer: Read definition before usage
        if (device!![OmronConstants.OMRONBLEConfigDevice.Category]!!.toInt() == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY || device!![OmronConstants.OMRONBLEConfigDevice.Category]!!
                .toInt() == OmronConstants.OMRONBLEDeviceCategory.PULSEOXIMETER
        ) {
            startOmronPeripheralManager(false, false)
            performDataTransfer()
        } else {
            val alertDialogBuilder = AlertDialog.Builder(mContext)
            alertDialogBuilder.setTitle("Transfer")
            alertDialogBuilder.setMessage("Do you want to transfer all historic readings from device?")
            alertDialogBuilder.setPositiveButton(
                "No"
            ) { arg0, arg1 ->
                startOmronPeripheralManager(false, false)
                performDataTransfer()
            }
            alertDialogBuilder.setNegativeButton(
                "Yes"
            ) { arg0, arg1 ->
                startOmronPeripheralManager(true, false)
                performDataTransfer()
            }
            alertDialogBuilder.setCancelable(true)
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

    private fun performDataTransfer() {

        // Set State Change Listener
        setStateChanges()

        //Create peripheral object with localname and UUID
        val peripheralLocal =
            OmronPeripheral(mSelectedPeripheral!!.localName, mSelectedPeripheral!!.uuid)
        if (selectedUsers.size > 1) {
            transferUsersDataWithPeripheral(peripheralLocal)
        } else {
            transferUserDataWithPeripheral(peripheralLocal)
        }
    }

    // Single User data transfer
    private fun transferUserDataWithPeripheral(peripheral: OmronPeripheral) {

        // Data Transfer from Device using OmronPeripheralManager
        OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
            .startDataTransferFromPeripheral(
                peripheral,
                selectedUsers[0]!!,
                true,
                OmronConstants.OMRONVitalDataTransferCategory.BloodPressure
            ) { peripheral, resultInfo ->
                if (resultInfo.resultCode == 0 && peripheral != null) {
                    val deviceInformation = peripheral.deviceInformation
                    Log.d(TAG, "Device Information : $deviceInformation")
                    val allSettings =
                        peripheral.deviceSettings as ArrayList<HashMap<*, *>>
                    Log.i(TAG, "Device settings : $allSettings")
                    mSelectedPeripheral = peripheral // Saving for Transfer Function

                    // Save Device to List
                    // To change based on data available
                    preferencesManager?.addDataStoredDeviceList(
                        peripheral.localName,
                        device!![OmronConstants.OMRONBLEConfigDevice.Category]!!
                            .toInt(),
                        peripheral.modelName
                    )

                    // Get vital data for previously selected user using OmronPeripheral
                    val output = peripheral.vitalData
                    if (output is OmronErrorInfo) {
                        val errorInfo = output
                        runOnUiThread {
                            mTvErrorCode!!.text =
                                errorInfo.resultCode.toString() + " / " + errorInfo.detailInfo
                            mTvErrorDesc!!.text = errorInfo.messageInfo
                            enableDisableButton(true)
                        }
                        disconnectDevice()
                    } else {
                        val vitalData =
                            output as HashMap<String, Any>
                        if (vitalData != null) {
                            uploadData(vitalData, peripheral, true)
                        }
                    }
                } else {
                    runOnUiThread {
                        setStatus("-")
                        mTvErrorCode!!.text =
                            resultInfo.resultCode.toString() + " / " + resultInfo.detailInfo
                        mTvErrorDesc!!.text = resultInfo.messageInfo
                        if (mHandler != null) mHandler!!.removeCallbacks(mRunnable!!)
                        enableDisableButton(true)
                    }
                }
            }
    }

    // Data transfer with multiple users
    private fun transferUsersDataWithPeripheral(peripheral: OmronPeripheral) {
        OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
            .startDataTransferFromPeripheral(
                peripheral, selectedUsers, true
            ) { peripheral, resultInfo ->
                if (resultInfo.resultCode == 0 && peripheral != null) {
                    val deviceInformation = peripheral.deviceInformation
                    Log.d(TAG, "Device Information : $deviceInformation")
                    val allSettings =
                        peripheral.deviceSettings as ArrayList<HashMap<*, *>>
                    Log.i(TAG, "Device settings : $allSettings")
                    mSelectedPeripheral = peripheral // Saving for Transfer Function

                    // Save Device to List
                    // To change based on data available
                    preferencesManager?.addDataStoredDeviceList(
                        peripheral.localName,
                        device!![OmronConstants.OMRONBLEConfigDevice.Category]!!
                            .toInt(),
                        peripheral.modelName
                    )

                    // Get vital data for previously selected user using OmronPeripheral
                    val output = peripheral.vitalData
                    if (output is OmronErrorInfo) {
                        val errorInfo = output
                        runOnUiThread {
                            mTvErrorCode!!.text =
                                errorInfo.resultCode.toString() + " / " + errorInfo.detailInfo
                            mTvErrorDesc!!.text = errorInfo.messageInfo
                            enableDisableButton(true)
                        }
                        disconnectDevice()
                    } else {
                        val vitalData =
                            output as HashMap<String, Any>
                        if (vitalData != null) {
                            uploadData(vitalData, peripheral, true)
                        }
                    }
                } else {
                    runOnUiThread {
                        setStatus("-")
                        mTvErrorCode!!.text =
                            resultInfo.resultCode.toString() + " / " + resultInfo.detailInfo
                        mTvErrorDesc!!.text = resultInfo.messageInfo
                        if (mHandler != null) mHandler!!.removeCallbacks(mRunnable!!)
                        enableDisableButton(true)
                    }
                }
            }
    }

    // Vital Data Save

    // Vital Data Save
    private fun uploadData(
        vitalData: HashMap<String, Any>,
        peripheral: OmronPeripheral,
        isWait: Boolean
    ) {
        val deviceInfo = peripheral.deviceInformation

        // Blood Pressure Data
        val bloodPressureItemList =
            vitalData[OmronConstants.OMRONVitalDataBloodPressureKey] as ArrayList<HashMap<String, Any>>?
        if (bloodPressureItemList != null) {
            for (bpItem in bloodPressureItemList) {
                Log.d("Blood Pressure - ", bpItem.toString())
            }
            insertVitalDataToDB(bloodPressureItemList, deviceInfo)
        }

        // Activity Data
        val activityList =
            vitalData[OmronConstants.OMRONVitalDataActivityKey] as ArrayList<HashMap<String, Any>>?
        if (activityList != null) {
            for (activityItem in activityList) {
                val list: List<String> = ArrayList(activityItem.keys)
                for (key in list) {
                    Log.d("Activity key - ", key)
                    Log.d("Activity Data - ", activityItem[key].toString())
                    if (key.equals(
                            OmronConstants.OMRONActivityData.AerobicStepsPerDay,
                            ignoreCase = true
                        ) || key.equals(
                            OmronConstants.OMRONActivityData.StepsPerDay,
                            ignoreCase = true
                        ) || key.equals(
                            OmronConstants.OMRONActivityData.DistancePerDay,
                            ignoreCase = true
                        ) || key.equals(
                            OmronConstants.OMRONActivityData.WalkingCaloriesPerDay,
                            ignoreCase = true
                        )
                    ) {
                        insertActivityToDB(
                            activityItem[key] as HashMap<String, Any>?,
                            deviceInfo,
                            key
                        )
                    }
                }
            }
        }

        // Sleep Data
        val sleepingData =
            vitalData[OmronConstants.OMRONVitalDataSleepKey] as ArrayList<HashMap<String, Any>>?
        if (sleepingData != null) {
            for (sleepitem in sleepingData) {
                Log.d("Sleep - ", sleepitem.toString())
            }
            insertSleepToDB(sleepingData, deviceInfo)
        }

        // Records Data
        val recordData =
            vitalData[OmronConstants.OMRONVitalDataRecordKey] as ArrayList<HashMap<String, Any>>?
        if (recordData != null) {
            for (recordItem in recordData) {
                Log.d("Record - ", recordItem.toString())
            }
            insertRecordToDB(recordData, deviceInfo)
        }

        // Weight Data
        val weightData =
            vitalData[OmronConstants.OMRONVitalDataWeightKey] as ArrayList<HashMap<String, Any>>?
        if (weightData != null) {
            for (weightItem in weightData) {
                Log.d("Weight - ", weightItem.toString())
            }
            insertRecordToDB(recordData, deviceInfo)
        }

        // Pulse oxximeter Data
        val pulseOximeterData =
            vitalData[OmronConstants.OMRONVitalDataPulseOximeterKey] as ArrayList<HashMap<String, Any>>?
        if (pulseOximeterData != null) {
            for (pulseOximeterItem in pulseOximeterData) {
                Log.d("Pulse Oximeter - ", pulseOximeterItem.toString())
            }
        }
        if (isWait) {
            mHandler = Handler()
            mRunnable = Runnable { continueDataTransfer() }
            mHandler!!.postDelayed(mRunnable!!, TIME_INTERVAL.toLong())
        } else {
            if (mHandler != null) mHandler!!.removeCallbacks(mRunnable!!)
            continueDataTransfer()
        }
    }

    private fun continueDataTransfer() {
        OmronPeripheralManager.sharedManager(App.getInstance().applicationContext)
            .endDataTransferFromPeripheral { peripheral, errorInfo ->
                runOnUiThread {
                    enableDisableButton(true)
                    if (errorInfo.resultCode == 0 && peripheral != null) {
                        val deviceInformation = peripheral.deviceInformation
                        Log.d(TAG, "Device Information : $deviceInformation")
                        val allSettings =
                            peripheral.deviceSettings as ArrayList<HashMap<*, *>>
                        Log.i(TAG, "Device settings : $allSettings")


                        // Get vital data for previously selected user using OmronPeripheral
                        val output = peripheral.vitalData
                        if (output is OmronErrorInfo) {
                            val errorInfo = output
                            mTvErrorCode!!.text =
                                errorInfo.resultCode.toString() + " / " + errorInfo.detailInfo
                            mTvErrorDesc!!.text = errorInfo.messageInfo
                        } else {
                            val vitalData =
                                output as HashMap<String, Any>
                            if (vitalData != null) {

                                // Blood Pressure Data
                                val bloodPressureItemList =
                                    vitalData[OmronConstants.OMRONVitalDataBloodPressureKey] as ArrayList<HashMap<String, Any>>?
                                bloodPressureItemList?.let { showVitalDataResult(it) }
                            }
                        }
                    } else {
                        setStatus("-")
                        mTvErrorCode!!.text =
                            errorInfo.resultCode.toString() + " / " + errorInfo.detailInfo
                        mTvErrorDesc!!.text = errorInfo.messageInfo
                    }
                }
            }
    }

    private fun insertVitalDataToDB(
        dataList: ArrayList<HashMap<String, Any>>,
        deviceInfo: HashMap<String, String>
    ) {
        for (bloodPressureItem in dataList) {
            val cv = ContentValues()
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataArtifactDetectionKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.ArtifactDetectionKey].toString()
            )
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataCuffFlagKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.CuffFlagKey].toString()
            )
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataDiastolicKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.DiastolicKey].toString()
            )
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataIHBDetectionKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.IHBDetectionKey].toString()
            )
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataIrregularFlagKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.IrregularFlagKey].toString()
            )
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataMeasurementDateKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.DateKey].toString()
            )
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataMovementFlagKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.MovementFlagKey].toString()
            )
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataPulseKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.PulseKey].toString()
            )
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataSystolicKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.SystolicKey].toString()
            )
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataMeasurementDateUTCKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.StartDateKey].toString()
            )
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataAtrialFibrillationDetectionFlagKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.AtrialFibrillationDetectionFlagKey].toString()
            )
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataDisplayedErrorCodeNightModeKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.DisplayedErrorCodeNightModeKey].toString()
            )
            cv.put(
                OmronDBConstans.VITAL_DATA_OMRONVitalDataMeasurementModeKey,
                bloodPressureItem[OmronConstants.OMRONVitalData.MeasurementModeKey].toString()
            )
            cv.put(
                OmronDBConstans.DEVICE_SELECTED_USER,
                bloodPressureItem[OmronConstants.OMRONVitalData.UserIdKey].toString()
            )
            cv.put(
                OmronDBConstans.DEVICE_LOCAL_NAME,
                deviceInfo[OmronConstants.OMRONDeviceInformation.LocalNameKey]!!
                    .lowercase(Locale.getDefault())
            )
            cv.put(
                OmronDBConstans.DEVICE_DISPLAY_NAME,
                deviceInfo[OmronConstants.OMRONDeviceInformation.DisplayNameKey]
            )
            cv.put(
                OmronDBConstans.DEVICE_IDENTITY_NAME,
                deviceInfo[OmronConstants.OMRONDeviceInformation.IdentityNameKey]
            )
            cv.put(
                OmronDBConstans.DEVICE_CATEGORY,
                device!![OmronConstants.OMRONBLEConfigDevice.Category]
            )
            val uri = contentResolver.insert(OmronDBConstans.VITAL_DATA_CONTENT_URI, cv)
            if (uri != null) {
                //TODO successful insert
            }
        }
    }

    /*******************************************************************************************/
    /************************ Section for Activity Device / HeartVue **************************/
    /*******************************************************************************************/

    /** */
    /************************ Section for Activity Device / HeartVue  */
    /** */
    /**
     * Insert Activity data
     */
    private fun insertActivityToDB(
        stepData: HashMap<String, Any>?,
        deviceInfo: HashMap<String, String>,
        type: String
    ) {
        val cv = ContentValues()
        cv.put(
            OmronDBConstans.ACTIVITY_DATA_StartDateUTCKey,
            stepData!![OmronConstants.OMRONActivityData.StartDateKey].toString()
        )
        cv.put(
            OmronDBConstans.ACTIVITY_DATA_EndDateUTCKey,
            stepData[OmronConstants.OMRONActivityData.EndDateKey].toString()
        )
        cv.put(
            OmronDBConstans.ACTIVITY_DATA_MeasurementValueKey,
            stepData[OmronConstants.OMRONActivityData.MeasurementKey].toString()
        )
        cv.put(
            OmronDBConstans.ACTIVITY_DATA_SeqNumKey,
            stepData[OmronConstants.OMRONActivityData.SequenceKey].toString()
        )
        cv.put(OmronDBConstans.ACTIVITY_DATA_Type, type)
        cv.put(
            OmronDBConstans.DEVICE_LOCAL_NAME,
            deviceInfo[OmronConstants.OMRONDeviceInformation.LocalNameKey]!!
                .lowercase(Locale.getDefault())
        )
        cv.put(
            OmronDBConstans.DEVICE_DISPLAY_NAME,
            deviceInfo[OmronConstants.OMRONDeviceInformation.DisplayNameKey]
        )
        cv.put(
            OmronDBConstans.DEVICE_IDENTITY_NAME,
            deviceInfo[OmronConstants.OMRONDeviceInformation.IdentityNameKey]
        )
        cv.put(
            OmronDBConstans.DEVICE_CATEGORY,
            device!![OmronConstants.OMRONBLEConfigDevice.Category]
        )
        val uri = contentResolver.insert(OmronDBConstans.ACTIVITY_DATA_CONTENT_URI, cv)
        if (uri != null) {
            val individualData =
                stepData[OmronConstants.OMRONActivityData.DividedDataKey] as ArrayList<HashMap<String, Any>>?
            if (individualData != null) {
                for (activityIndividual in individualData) {
                    val dividedCV = ContentValues()
                    dividedCV.put(
                        OmronDBConstans.ACTIVITY_DIVIDED_DATA_MainStartDateUTCKey,
                        stepData[OmronConstants.OMRONActivityData.StartDateKey].toString()
                    )
                    dividedCV.put(
                        OmronDBConstans.ACTIVITY_DIVIDED_DATA_StartDateUTCKey,
                        activityIndividual[OmronConstants.OMRONActivityData.DividedDataStartDateKey].toString()
                    )
                    dividedCV.put(
                        OmronDBConstans.ACTIVITY_DIVIDED_DATA_StartDateUTCKey,
                        activityIndividual[OmronConstants.OMRONActivityData.DividedDataStartDateKey].toString()
                    )
                    dividedCV.put(
                        OmronDBConstans.ACTIVITY_DIVIDED_DATA_MeasurementValueKey,
                        activityIndividual[OmronConstants.OMRONActivityData.DividedDataMeasurementKey].toString()
                    )
                    dividedCV.put(
                        OmronDBConstans.ACTIVITY_DIVIDED_DATA_SeqNumKey,
                        stepData[OmronConstants.OMRONActivityData.SequenceKey].toString()
                    )
                    dividedCV.put(OmronDBConstans.ACTIVITY_DIVIDED_DATA_Type, type)
                    dividedCV.put(
                        OmronDBConstans.DEVICE_LOCAL_NAME,
                        deviceInfo[OmronConstants.OMRONDeviceInformation.LocalNameKey]!!
                            .lowercase(Locale.getDefault())
                    )
                    val uriDivided = contentResolver.insert(
                        OmronDBConstans.ACTIVITY_DIVIDED_DATA_CONTENT_URI,
                        dividedCV
                    )
                    if (uriDivided != null) {
                    }
                }
            }
        }
    }

    /**
     * Insert sleep data
     */
    private fun insertSleepToDB(
        dataList: ArrayList<HashMap<String, Any>>,
        deviceInfo: HashMap<String, String>
    ) {
        for (sleepingDataItem in dataList) {
            val cv = ContentValues()
            cv.put(
                OmronDBConstans.SLEEP_DATA_SleepStartTimeKey,
                sleepingDataItem[OmronConstants.OMRONSleepData.TimeInBedKey].toString()
            )
            cv.put(
                OmronDBConstans.SLEEP_DATA_SleepOnSetTimeKey,
                sleepingDataItem[OmronConstants.OMRONSleepData.SleepOnsetTimeKey].toString()
            )
            cv.put(
                OmronDBConstans.SLEEP_DATA_WakeUpTimeKey,
                sleepingDataItem[OmronConstants.OMRONSleepData.WakeTimeKey].toString()
            )
            cv.put(
                OmronDBConstans.SLEEP_DATA_SleepingTimeKey,
                sleepingDataItem[OmronConstants.OMRONSleepData.TotalSleepTimeKey].toString()
            )
            cv.put(
                OmronDBConstans.SLEEP_DATA_SleepEfficiencyKey,
                sleepingDataItem[OmronConstants.OMRONSleepData.SleepEfficiencyKey].toString()
            )
            cv.put(
                OmronDBConstans.SLEEP_DATA_SleepArousalTimeKey,
                sleepingDataItem[OmronConstants.OMRONSleepData.ArousalDuringSleepTimeKey].toString()
            )
            cv.put(
                OmronDBConstans.SLEEP_DATA_SleepBodyMovementKey,
                sleepingDataItem[OmronConstants.OMRONSleepData.BodyMotionLevelKey].toString()
            )
            cv.put(
                OmronDBConstans.SLEEP_DATA_StartDateUTCKey,
                sleepingDataItem[OmronConstants.OMRONSleepData.StartDateKey].toString()
            )
            cv.put(
                OmronDBConstans.SLEEP_DATA_StartEndDateUTCKey,
                sleepingDataItem[OmronConstants.OMRONSleepData.EndDateKey].toString()
            )
            cv.put(
                OmronDBConstans.DEVICE_LOCAL_NAME,
                deviceInfo[OmronConstants.OMRONDeviceInformation.LocalNameKey]!!
                    .lowercase(Locale.getDefault())
            )
            cv.put(
                OmronDBConstans.DEVICE_DISPLAY_NAME,
                deviceInfo[OmronConstants.OMRONDeviceInformation.DisplayNameKey]
            )
            cv.put(
                OmronDBConstans.DEVICE_IDENTITY_NAME,
                deviceInfo[OmronConstants.OMRONDeviceInformation.IdentityNameKey]
            )
            cv.put(
                OmronDBConstans.DEVICE_CATEGORY,
                device!![OmronConstants.OMRONBLEConfigDevice.Category]
            )
            val uri = contentResolver.insert(OmronDBConstans.SLEEP_DATA_CONTENT_URI, cv)
            if (uri != null) {
                //TODO successful insert
            }
        }
    }

    /**
     * Insert record details
     */
    private fun insertRecordToDB(
        dataList: ArrayList<HashMap<String, Any>>?,
        deviceInfo: HashMap<String, String>
    ) {
        for (recordDataItem in dataList!!) {
            val cv = ContentValues()
            cv.put(
                OmronDBConstans.RECORD_DATA_StartDateUTCKey,
                recordDataItem[OmronConstants.OMRONRecordData.DateKey].toString()
            )
            cv.put(
                OmronDBConstans.DEVICE_LOCAL_NAME,
                deviceInfo[OmronConstants.OMRONDeviceInformation.LocalNameKey]!!
                    .lowercase(Locale.getDefault())
            )
            cv.put(
                OmronDBConstans.DEVICE_DISPLAY_NAME,
                deviceInfo[OmronConstants.OMRONDeviceInformation.DisplayNameKey]
            )
            cv.put(
                OmronDBConstans.DEVICE_IDENTITY_NAME,
                deviceInfo[OmronConstants.OMRONDeviceInformation.IdentityNameKey]
            )
            cv.put(
                OmronDBConstans.DEVICE_CATEGORY,
                device!![OmronConstants.OMRONBLEConfigDevice.Category]
            )
            val uri = contentResolver.insert(OmronDBConstans.RECORD_DATA_CONTENT_URI, cv)
            if (uri != null) {
                //TODO successful insert
            }
        }
    }

    // Settings update for Connectivity library
    private fun updateSettings() {
        if (mSelectedPeripheral == null) {
            mTvErrorDesc!!.text = "Device Not Paired"
            return
        }
        resetErrorMessage()
        enableDisableButton(false)
        resetVitalDataResult()
        setStatus("Connecting...")
        val peripheralConfig =
            OmronPeripheralManager.sharedManager(App.getInstance().applicationContext).configuration

        // Filter device to scan and connect (optional)
        if (device != null && device!![OmronConstants.OMRONBLEConfigDevice.GroupID] != null && device!![OmronConstants.OMRONBLEConfigDevice.GroupIncludedGroupID] != null) {

            // Add item
            val filterDevices: MutableList<HashMap<String?, String?>?>? = ArrayList()
            filterDevices?.add(device)
            OmronPeripheralManagerConfig.deviceFilters = filterDevices
        }
        val settingsModel = HashMap<String, String>()
        settingsModel[OmronConstants.OMRONDevicePersonalSettings.UserHeightKey] = "17200"
        settingsModel[OmronConstants.OMRONDevicePersonalSettings.UserWeightKey] = "6500"
        settingsModel[OmronConstants.OMRONDevicePersonalSettings.UserStrideKey] = "1500"
        settingsModel[OmronConstants.OMRONDevicePersonalSettings.TargetSleepKey] = "60"
        settingsModel[OmronConstants.OMRONDevicePersonalSettings.TargetStepsKey] = "2000"
        val userSettings = HashMap<String, HashMap<*, *>>()
        userSettings[OmronConstants.OMRONDevicePersonalSettingsKey] = settingsModel

        // Test Functions
        // Date Format
        val dateFormatSettings = HashMap<String, Any>()
        dateFormatSettings[OmronConstants.OMRONDeviceDateSettings.FormatKey] =
            OmronConstants.OMRONDeviceDateFormat.DayMonth
        val dateSettings = HashMap<String, HashMap<*, *>>()
        dateSettings[OmronConstants.OMRONDeviceDateSettingsKey] = dateFormatSettings

        // Distance Unit Format
        val dateUnitSettings = HashMap<String, Any>()
        dateUnitSettings[OmronConstants.OMRONDeviceDistanceSettings.UnitKey] =
            OmronConstants.OMRONDeviceDistanceUnit.Kilometer
        val distanceSettings = HashMap<String, HashMap<*, *>>()
        distanceSettings[OmronConstants.OMRONDeviceDistanceSettingsKey] = dateUnitSettings

        // Time Format
        val timeFormatSettings = HashMap<String, Any>()
        timeFormatSettings[OmronConstants.OMRONDeviceTimeSettings.FormatKey] =
            OmronConstants.OMRONDeviceTimeFormat.Time24Hour
        val timeSettings = HashMap<String, HashMap<*, *>>()
        timeSettings[OmronConstants.OMRONDeviceTimeSettingsKey] = timeFormatSettings

        // Sleep Settings
        val sleepTimeSettings = HashMap<String, Any>()
        sleepTimeSettings[OmronConstants.OMRONDeviceSleepSettings.AutomaticKey] =
            OmronConstants.OMRONDeviceSleepAutomatic.On
        sleepTimeSettings[OmronConstants.OMRONDeviceSleepSettings.StartTimeKey] = "19"
        sleepTimeSettings[OmronConstants.OMRONDeviceSleepSettings.StopTimeKey] = "20"
        val sleepSettings = HashMap<String, HashMap<*, *>>()
        sleepSettings[OmronConstants.OMRONDeviceSleepSettingsKey] = sleepTimeSettings

        // Alarm Settings
        // Alarm 1 Time
        val alarmTime1 = HashMap<String, Any>()
        alarmTime1[OmronConstants.OMRONDeviceAlarmSettings.HourKey] = "15"
        alarmTime1[OmronConstants.OMRONDeviceAlarmSettings.MinuteKey] = "33"
        // Alarm 1 Day (SUN-SAT)
        val alarmDays1 = HashMap<String, Any>()
        alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.SundayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.MondayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.FridayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        val alarm1 = HashMap<String, Any>()
        alarm1[OmronConstants.OMRONDeviceAlarmSettings.DaysKey] = alarmDays1
        alarm1[OmronConstants.OMRONDeviceAlarmSettings.TimeKey] = alarmTime1
        alarm1[OmronConstants.OMRONDeviceAlarmSettings.TypeKey] =
            OmronConstants.OMRONDeviceAlarmType.Measure


        // Alarm 2 Time
        val alarmTime2 = HashMap<String, Any>()
        alarmTime2[OmronConstants.OMRONDeviceAlarmSettings.HourKey] = "15"
        alarmTime2[OmronConstants.OMRONDeviceAlarmSettings.MinuteKey] = "34"
        // Alarm 2 Day (SUN-SAT)
        val alarmDays2 = HashMap<String, Any>()
        alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.SundayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.MondayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.FridayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey] =
            OmronConstants.OMRONDeviceAlarmStatus.Off
        val alarm2 = HashMap<String, Any>()
        alarm2[OmronConstants.OMRONDeviceAlarmSettings.DaysKey] = alarmDays2
        alarm2[OmronConstants.OMRONDeviceAlarmSettings.TimeKey] = alarmTime2
        alarm2[OmronConstants.OMRONDeviceAlarmSettings.TypeKey] =
            OmronConstants.OMRONDeviceAlarmType.Medication

        // Add Alarm1, Alarm2, Alarm3 to List
        val alarms = ArrayList<HashMap<*, *>>()
        alarms.add(alarm1)
        alarms.add(alarm2)
        val alarmSettings = HashMap<String, Any>()
        alarmSettings[OmronConstants.OMRONDeviceAlarmSettingsKey] = alarms


        // Notification settings
        val notificationsAvailable = ArrayList<String>()
        notificationsAvailable.add("android.intent.action.PHONE_STATE")
        notificationsAvailable.add("com.google.android.gm")
        notificationsAvailable.add("android.provider.Telephony.SMS_RECEIVED")
        val notificationSettings = HashMap<String, Any>()
        notificationSettings[OmronConstants.OMRONDeviceNotificationSettingsKey] =
            notificationsAvailable
        val deviceSettings = ArrayList<HashMap<*, *>>()
        deviceSettings.add(userSettings)
        deviceSettings.add(dateSettings)
        deviceSettings.add(distanceSettings)
        deviceSettings.add(timeSettings)
        deviceSettings.add(sleepSettings)
        deviceSettings.add(alarmSettings)
        deviceSettings.add(notificationSettings)
        OmronPeripheralManagerConfig.deviceSettings = deviceSettings


        // Set Scan timeout interval (optional)
        OmronPeripheralManagerConfig.timeoutInterval = Constants.CONNECTION_TIMEOUT

        // Set User Hash Id (mandatory)
        OmronPeripheralManagerConfig.userHashId =
            "<email_address_of_user>" // Set logged in user email
        OmronPeripheralManagerConfig.enableAllDataRead = true

        // Set configuration for OmronPeripheralManager
        OmronPeripheralManager.sharedManager(App.getInstance().applicationContext).configuration =
            peripheralConfig

        //Create peripheral object with localname and UUID
        val peripheral =
            OmronPeripheral(mSelectedPeripheral!!.getLocalName(), mSelectedPeripheral!!.getUuid())

        //Call to update the settings
        OmronPeripheralManager.sharedManager(App.getInstance().applicationContext).updatePeripheral(
            peripheral
        ) { peripheral, resultInfo ->
            runOnUiThread {
                if (resultInfo.resultCode == 0 && peripheral != null) {
                    mSelectedPeripheral = peripheral
                    if (null != peripheral.localName) {
                        mTvDeviceLocalName!!.text = peripheral.localName
                        mTvDeviceUuid!!.text = peripheral.uuid
                        showMessage(
                            getString(R.string.device_connected),
                            getString(R.string.update_success)
                        )
                        val deviceInformation = peripheral.deviceInformation
                        Log.d(TAG, "Device Information : $deviceInformation")
                        val peripheralConfig =
                            OmronPeripheralManager.sharedManager(App.getInstance().applicationContext).configuration
                        Log.d(
                            TAG,
                            "Device Config :  " + peripheralConfig.getDeviceConfigGroupIdAndGroupIncludedId(
                                peripheral.deviceGroupIDKey,
                                peripheral.deviceGroupIncludedGroupIDKey
                            )
                        )
                    }
                } else {
                    setStatus("-")
                    mTvErrorCode!!.text = resultInfo.detailInfo
                    mTvErrorDesc!!.text = resultInfo.messageInfo
                }
                enableDisableButton(true)
            }
        }
    }


    private fun getBloodPressureSettings(
        deviceSettings: ArrayList<HashMap<*, *>?>?,
        isPairing: Boolean
    ): ArrayList<HashMap<*, *>?>? {

        // Blood Pressure
        if (device!![OmronConstants.OMRONBLEConfigDevice.Category]!!.toInt() == OmronConstants.OMRONBLEDeviceCategory.BLOODPRESSURE) {
            val bloodPressurePersonalSettings = HashMap<String, Any>()
            bloodPressurePersonalSettings[OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadEnableKey] =
                OmronConstants.OMRONDevicePersonalSettingsBloodPressureTruReadStatus.On
            bloodPressurePersonalSettings[OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadIntervalKey] =
                OmronConstants.OMRONDevicePersonalSettingsBloodPressureTruReadInterval.Interval30
            val settings = HashMap<String, Any>()
            settings[OmronConstants.OMRONDevicePersonalSettings.BloodPressureKey] =
                bloodPressurePersonalSettings
            val personalSettings = HashMap<String, HashMap<*, *>>()
            personalSettings[OmronConstants.OMRONDevicePersonalSettingsKey] = settings
            val transferModeSettings = HashMap<String, Any>()
            val transferSettings = HashMap<String, HashMap<*, *>>()
            if (isPairing) {
                transferModeSettings[OmronConstants.OMRONDeviceScanSettings.ModeKey] =
                    OmronConstants.OMRONDeviceScanSettingsMode.Pairing
            } else {
                transferModeSettings[OmronConstants.OMRONDeviceScanSettings.ModeKey] =
                    OmronConstants.OMRONDeviceScanSettingsMode.MismatchSequence
            }
            transferSettings[OmronConstants.OMRONDeviceScanSettingsKey] = transferModeSettings

            // Personal settings for device
            deviceSettings?.add(personalSettings)
            deviceSettings?.add(transferSettings)
        }
        return deviceSettings
    }

    private fun getActivitySettings(deviceSettings: ArrayList<HashMap<*, *>>): ArrayList<HashMap<*, *>>? {

        // Activity Tracker
        if (device!![OmronConstants.OMRONBLEConfigDevice.Category]!!.toInt() == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY) {

            // Set Personal Settings in Configuration (mandatory for Activity devices)
            if (personalSettings != null) {
                val settingsModel = HashMap<String, String?>()
                settingsModel[OmronConstants.OMRONDevicePersonalSettings.UserHeightKey] =
                    personalSettings!!["personalHeight"]
                settingsModel[OmronConstants.OMRONDevicePersonalSettings.UserWeightKey] =
                    personalSettings!!["personalWeight"]
                settingsModel[OmronConstants.OMRONDevicePersonalSettings.UserStrideKey] =
                    personalSettings!!["personalStride"]
                settingsModel[OmronConstants.OMRONDevicePersonalSettings.TargetSleepKey] = "120"
                settingsModel[OmronConstants.OMRONDevicePersonalSettings.TargetStepsKey] = "2000"
                val userSettings = HashMap<String, HashMap<*, *>>()
                userSettings[OmronConstants.OMRONDevicePersonalSettingsKey] = settingsModel

                // Notification settings
                val notificationsAvailable = ArrayList<String>()
                notificationsAvailable.add("android.intent.action.PHONE_STATE")
                notificationsAvailable.add("com.google.android.gm")
                notificationsAvailable.add("android.provider.Telephony.SMS_RECEIVED")
                notificationsAvailable.add("com.omronhealthcare.OmronConnectivitySample")
                val notificationSettings = HashMap<String, Any>()
                notificationSettings[OmronConstants.OMRONDeviceNotificationSettingsKey] =
                    notificationsAvailable

                // Time Format
                val timeFormatSettings = HashMap<String, Any>()
                timeFormatSettings[OmronConstants.OMRONDeviceTimeSettings.FormatKey] =
                    OmronConstants.OMRONDeviceTimeFormat.Time12Hour
                val timeSettings = HashMap<String, HashMap<*, *>>()
                timeSettings[OmronConstants.OMRONDeviceTimeSettingsKey] = timeFormatSettings


                // Sleep Settings
                val sleepTimeSettings = HashMap<String, Any>()
                sleepTimeSettings[OmronConstants.OMRONDeviceSleepSettings.AutomaticKey] =
                    OmronConstants.OMRONDeviceSleepAutomatic.Off
                sleepTimeSettings[OmronConstants.OMRONDeviceSleepSettings.StartTimeKey] = "19"
                sleepTimeSettings[OmronConstants.OMRONDeviceSleepSettings.StopTimeKey] = "20"
                val sleepSettings = HashMap<String, HashMap<*, *>>()
                sleepSettings[OmronConstants.OMRONDeviceSleepSettingsKey] = sleepTimeSettings


                // Alarm Settings
                // Alarm 1 Time
                val alarmTime1 = HashMap<String, Any>()
                alarmTime1[OmronConstants.OMRONDeviceAlarmSettings.HourKey] = "15"
                alarmTime1[OmronConstants.OMRONDeviceAlarmSettings.MinuteKey] = "33"
                // Alarm 1 Day (SUN-SAT)
                val alarmDays1 = HashMap<String, Any>()
                alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.SundayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.Off
                alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.MondayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.Off
                alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.Off
                alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.Off
                alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.On
                alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.FridayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.Off
                alarmDays1[OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.Off
                val alarm1 = HashMap<String, Any>()
                alarm1[OmronConstants.OMRONDeviceAlarmSettings.DaysKey] = alarmDays1
                alarm1[OmronConstants.OMRONDeviceAlarmSettings.TimeKey] = alarmTime1
                alarm1[OmronConstants.OMRONDeviceAlarmSettings.TypeKey] =
                    OmronConstants.OMRONDeviceAlarmType.Measure


                // Alarm 2 Time
                val alarmTime2 = HashMap<String, Any>()
                alarmTime2[OmronConstants.OMRONDeviceAlarmSettings.HourKey] = "15"
                alarmTime2[OmronConstants.OMRONDeviceAlarmSettings.MinuteKey] = "34"
                // Alarm 2 Day (SUN-SAT)
                val alarmDays2 = HashMap<String, Any>()
                alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.SundayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.Off
                alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.MondayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.Off
                alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.TuesdayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.Off
                alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.WednesdayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.Off
                alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.ThursdayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.On
                alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.FridayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.Off
                alarmDays2[OmronConstants.OMRONDeviceAlarmSettings.SaturdayKey] =
                    OmronConstants.OMRONDeviceAlarmStatus.Off
                val alarm2 = HashMap<String, Any>()
                alarm2[OmronConstants.OMRONDeviceAlarmSettings.DaysKey] = alarmDays2
                alarm2[OmronConstants.OMRONDeviceAlarmSettings.TimeKey] = alarmTime2
                alarm2[OmronConstants.OMRONDeviceAlarmSettings.TypeKey] =
                    OmronConstants.OMRONDeviceAlarmType.Medication

                // Add Alarm1, Alarm2, Alarm3 to List
                val alarms = ArrayList<HashMap<*, *>>()
                alarms.add(alarm1)
                alarms.add(alarm2)
                val alarmSettings = HashMap<String, Any>()
                alarmSettings[OmronConstants.OMRONDeviceAlarmSettingsKey] = alarms


                // Notification enable settings
                val notificationEnableSettings = HashMap<String, Any>()
                notificationEnableSettings[OmronConstants.OMRONDeviceNotificationStatusKey] =
                    OmronConstants.OMRONDeviceNotificationStatus.On
                val notificationStatusSettings =
                    HashMap<String, HashMap<*, *>>()
                notificationStatusSettings[OmronConstants.OMRONDeviceNotificationEnableSettingsKey] =
                    notificationEnableSettings
                deviceSettings.add(userSettings)
                deviceSettings.add(notificationSettings)
                deviceSettings.add(alarmSettings)
                deviceSettings.add(timeSettings)
                deviceSettings.add(sleepSettings)
                deviceSettings.add(notificationStatusSettings)
            }
        }
        return deviceSettings
    }

    private fun getBCMSettings(deviceSettings: ArrayList<HashMap<*, *>>): ArrayList<HashMap<*, *>>? {

        // body composition
        if (device!![OmronConstants.OMRONBLEConfigDevice.Category]!!.toInt() == OmronConstants.OMRONBLEDeviceCategory.BODYCOMPOSITION) {

            //Weight settings
            val weightPersonalSettings = HashMap<String, Any>()
            weightPersonalSettings[OmronConstants.OMRONDevicePersonalSettings.WeightDCIKey] = 100
            val settings = HashMap<String, Any>()
            settings[OmronConstants.OMRONDevicePersonalSettings.UserHeightKey] = "17000"
            settings[OmronConstants.OMRONDevicePersonalSettings.UserGenderKey] =
                OmronConstants.OMRONDevicePersonalSettingsUserGenderType.Male
            settings[OmronConstants.OMRONDevicePersonalSettings.UserDateOfBirthKey] = "19001010"
            settings[OmronConstants.OMRONDevicePersonalSettings.WeightKey] = weightPersonalSettings
            val personalSettings = HashMap<String, HashMap<*, *>>()
            personalSettings[OmronConstants.OMRONDevicePersonalSettingsKey] = settings

            // Weight Settings
            // Add other weight common settings if any
            val weightCommonSettings = HashMap<String, Any>()
            weightCommonSettings[OmronConstants.OMRONDeviceWeightSettings.UnitKey] =
                OmronConstants.OMRONDeviceWeightUnit.Lbs
            val weightSettings = HashMap<String, Any>()
            weightSettings[OmronConstants.OMRONDeviceWeightSettingsKey] = weightCommonSettings
            deviceSettings.add(personalSettings)
            deviceSettings.add(weightSettings)
        }
        return deviceSettings
    }

    // UI Functions

    // UI Functions
    private fun initClickListeners() {

        // To perform scan process.
        binding.btnScan.setOnClickListener(View.OnClickListener { startScanning() })
        binding.btnDisconnect.setOnClickListener(View.OnClickListener { disconnectDevice() })

        //Data transfer
        binding.btnTransfer.setOnClickListener(View.OnClickListener { transferData() })

        //Add device for the connected device list.
        binding.ivAddDevice.setOnClickListener(View.OnClickListener { })

        //Open Vital data activity
        binding.ivVitalData.setOnClickListener(View.OnClickListener {
            if (mSelectedPeripheral != null) if (device!![OmronConstants.OMRONBLEConfigDevice.Category]!!
                    .toInt() == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY
            ) {
                /*val toVitalData = Intent(this@MainActivity, DataListingActivity::class.java)
                toVitalData.putExtra(
                    Constants.extraKeys.KEY_DEVICE_LOCAL_NAME,
                    mSelectedPeripheral!!.getLocalName()
                )
                startActivity(toVitalData)*/
            } else {
                /*val toVitalData = Intent(this@MainActivity, VitalDataListingActivity::class.java)
                toVitalData.putExtra(
                    Constants.extraKeys.KEY_DEVICE_LOCAL_NAME,
                    mSelectedPeripheral!!.getLocalName()
                )
                startActivity(toVitalData)*/
            }
        })

        //Open Settings update activity
        binding.ivDeviceSetting.setOnClickListener(View.OnClickListener {
            //                if (mSelectedPeripheral != null) {
//                    PeripheralDevice peripheralDevice = new PeripheralDevice(mSelectedPeripheral.getLocalName(), mSelectedPeripheral.getUuid(), mSelectedPeripheral.getSelectedUser(), Integer.parseInt(device.get(OmronConstants.OMRONBLEConfigDevice.Category)));
//                    peripheralDevice.setModelName(mSelectedPeripheral.getModelName());
//                    peripheralDevice.setModelSeries(mSelectedPeripheral.getModelSeries());
//                    Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
//                    intent.putExtra(ReminderActivity.ARG_DEVICE, peripheralDevice);
//                    if (personalSettings != null)
//                        intent.putExtra(Constants.extraKeys.KEY_PERSONAL_SETTINGS, personalSettings);
//
//                    if (device != null)
//                        intent.putExtra(Constants.extraKeys.KEY_SELECTED_DEVICE, device);
//
//                    startActivity(intent);
//                }
        })
    }


    private fun initViews() {
        mLvScannedList = findViewById(R.id.lv_scannedlist) as ListView
        mTvTImeStamp = findViewById(R.id.tv_timestamp_value) as TextView
        mTvSystolic = findViewById(R.id.tv_sys_value) as TextView
        mTvDiastolic = findViewById(R.id.tv_dia_value) as TextView
        mTvPulseRate = findViewById(R.id.tv_pulse_value) as TextView
        mTvUserSelected = findViewById(R.id.tv_userselected) as TextView
        mTvDeviceInfo = findViewById(R.id.device_info) as TextView
        mTvDeviceLocalName = findViewById(R.id.tv_device_name) as TextView
        mTvDeviceUuid = findViewById(R.id.tv_device_uuid) as TextView
        mTvStatusLabel = findViewById(R.id.tv_status_value) as TextView
        mTvErrorCode = findViewById(R.id.tv_error_value) as TextView
        mTvErrorDesc = findViewById(R.id.tv_error_desc) as TextView
        mRlDeviceListView = findViewById(R.id.rl_device_list) as RelativeLayout
        mRlTransferView = findViewById(R.id.rl_transfer_view) as RelativeLayout
        mProgressBar = findViewById(R.id.pb_scan) as ProgressBar
        mTvUserSelected!!.text = TextUtils.join(",", selectedUsers)
        scanBtn = findViewById(R.id.btn_scan) as Button
        transferBtn = findViewById(R.id.btn_transfer) as Button
        if (device!![OmronConstants.OMRONBLEConfigDevice.Category]!!.toInt() == OmronConstants.OMRONBLEDeviceCategory.ACTIVITY) {
            // Activity Tracker or HeartVue
            binding.ivDeviceSetting.setVisibility(View.VISIBLE)
        } else {
            // Blood Pressure
            binding.ivDeviceSetting.setVisibility(View.INVISIBLE)
        }

        // Hide Add device
        binding.ivAddDevice.setVisibility(View.GONE)
    }

    private fun enableDisableButton(enable: Boolean) {
        binding.ivVitalData.setEnabled(enable)
        scanBtn!!.isEnabled = enable
        transferBtn!!.isEnabled = enable
    }

    private fun initLists() {
        mPeripheralList = ArrayList()
        mScannedDevicesAdapter = ScannedDevicesAdapter(mContext, mPeripheralList)
        mLvScannedList!!.adapter = mScannedDevicesAdapter
        mLvScannedList!!.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val noOfUsers = device!![OmronConstants.OMRONBLEConfigDevice.Users]!!.toInt()
                if (noOfUsers == 1) {
                    connectPeripheral(mScannedDevicesAdapter!!.getItem(position))
                } else {
                    connectPeripheralWithWait(mScannedDevicesAdapter!!.getItem(position))
                }
            }
    }

    private fun showDeviceListView() {
        mRlTransferView!!.visibility = View.GONE
        mRlDeviceListView!!.visibility = View.VISIBLE
    }

    private fun showTransferView() {
        mRlDeviceListView!!.visibility = View.GONE
        mRlTransferView!!.visibility = View.VISIBLE
        setDeviceInformation()
    }

    private fun setStatus(statusMessage: String) {
        mTvStatusLabel!!.text = statusMessage
    }

    private fun resetErrorMessage() {
        mTvErrorCode!!.text = "-"
        mTvErrorDesc!!.text = "-"
        mTvStatusLabel!!.text = "-"
    }

    private fun setDeviceInformation() {
        if (null != mSelectedPeripheral) {
            if (null != mSelectedPeripheral!!.getModelName()) {
                mTvDeviceInfo!!.text =
                    mSelectedPeripheral!!.getModelName() + " - " + getString(R.string.device_information)
            } else {
                mTvDeviceInfo!!.text = getString(R.string.device_information)
            }
        }
    }

    private fun showVitalDataResult(bloodPressureItemList: ArrayList<HashMap<String, Any>>) {
        runOnUiThread {
            if (bloodPressureItemList.size == 0) {
                mTvErrorDesc!!.text = "No New readings transferred"
                mTvTImeStamp!!.text = "-"
                mTvSystolic!!.text = "-"
                mTvDiastolic!!.text = "-"
                mTvPulseRate!!.text = "-"
            } else {
                val bloodPressureItem = bloodPressureItemList[bloodPressureItemList.size - 1]
                mTvErrorDesc!!.text = "-"
                mTvTImeStamp!!.text =
                    bloodPressureItem[OmronConstants.OMRONVitalData.DateKey].toString()
                mTvSystolic!!.text =
                    bloodPressureItem[OmronConstants.OMRONVitalData.SystolicKey]
                        .toString() + "\t mmHg"
                mTvDiastolic!!.text =
                    bloodPressureItem[OmronConstants.OMRONVitalData.DiastolicKey]
                        .toString() + "\t mmHg"
                mTvPulseRate!!.text =
                    bloodPressureItem[OmronConstants.OMRONVitalData.PulseKey].toString() + "\t bpm"
            }
        }
    }

    private fun resetVitalDataResult() {
        mTvTImeStamp!!.text = "-"
        mTvSystolic!!.text = "-"
        mTvDiastolic!!.text = "-"
        mTvPulseRate!!.text = "-"
    }

    private fun resetDeviceList() {
        if (mScannedDevicesAdapter != null) {
            mProgressBar!!.visibility = View.GONE
            mPeripheralList = ArrayList()
            mScannedDevicesAdapter?.setPeripheralList(mPeripheralList)
            mScannedDevicesAdapter?.notifyDataSetChanged()
        }
    }

    fun showMessage(title: String?, message: String?) {
        val alertDialogBuilder = AlertDialog.Builder(mContext)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton(
            "Ok"
        ) { arg0, arg1 -> }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}