package com.base.memoryleaksdetectors

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.telephony.CellInfo
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.security.AccessController.getContext


class MainActivity : AppCompatActivity() {
    private val TAG = "MyApp"
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    var connectivityManager: ConnectivityManager? = null
    var telephonyManager: TelephonyManager? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connectivityManager = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        startListen()
        //  val netMonSignalStrength = NetMonSignalStrength(this)

        val text = findViewById<TextView>(R.id.textView)
        val text2 = findViewById<TextView>(R.id.textView2)
        text.setOnClickListener {
             startActivity(Intent(this,MemoryLeakActivity::class.java))
//            Log.d(TAG, "onCreate: ${getNetworkStrength()}")
//            text.text = "vodafone ${getNetworkStrength().second}"
//            text2.text = "we ${getNetworkStrength().first}"
        }
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        // val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        //   val phoneStateListenerList = mutableListOf<PhoneStateListener>()


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        }


//        val phoneCount = telephonyManager.phoneCount
//
//        if (phoneCount > 0) {
//            print("Device has $phoneCount SIM card(s)")
//
//            for (i in 0 until phoneCount) {
//                // Get info for each SIM card
//                val networkType = telephonyManager.getAllowedNetworkTypesForReason(i)
//                val networkStrength = telephonyManager.getSignalStrength().get
//
//                Log.e(TAG, "SIM $i \nNetwork Type: $networkType  Signal Strength: $networkStrength" )
//
//            }
//        } else {
//            print("Device has no SIM cards")
//        }
//        subscriptionManager.activeSubscriptionInfoList?.forEach { subscriptionInfo ->
//            val phoneStateListener = object : PhoneStateListener() {
//                override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
//                    super.onSignalStrengthsChanged(signalStrength)
//                    if (signalStrength != null) {
//                        val dbm = signalStrength.cdmaDbm
//                        Log.d(TAG, "Signal strength for SIM card ${subscriptionInfo.subscriptionId}: ${dbm} dBm")
//                    }
//                }
//            }
//            phoneStateListenerList.add(phoneStateListener)
//            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
//        }
    }

    private fun requestPermissions() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.READ_PHONE_STATE] == true) {
                    return@registerForActivityResult
                }
            }
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
            )
        )
    }

    var signalStrengthValue = 0


    private var handlerThreadCellularSignal: HandlerThread? = null

    fun startListen() {
        handlerThreadCellularSignal = HandlerThread("CELLULAR_INFO_THREAD")
        handlerThreadCellularSignal!!.start()
        val looper = handlerThreadCellularSignal!!.looper
        val handler = Handler(looper)
        handler.post(Runnable {
            val phoneStatelistener = PhoneStateListenerEx()
            val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.listen(phoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        })
    }

    private fun stopListen() {
        handlerThreadCellularSignal!!.quit()
    }


    class PhoneStateListenerEx : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            super.onSignalStrengthsChanged(signalStrength)
            val signalStrengthdBm = 2 * signalStrength.gsmSignalStrength - 113 // -> dBm
            Log.d("TAG", "Cellular Signal Strength | $signalStrengthdBm")
        }
    }

    fun getRegisteredCellInfo(cellInfos: MutableList<CellInfo>): ArrayList<CellInfo> {
        val registeredCellInfos = ArrayList<CellInfo>()
        if (cellInfos.isNotEmpty()) {
            for (i in cellInfos.indices) {
                if (cellInfos[i].isRegistered) {
                    registeredCellInfos.add(cellInfos[i])
                }
            }
        }
        return registeredCellInfos
    }

    fun getNetworkStrength(): Pair<Int, Int> {

        var strength1 = -1
        var strength2 = -1


        val manager =
            getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return Pair(0, 0)
        }
        if (telephonyManager?.allCellInfo != null) {
            val allCellinfo = telephonyManager?.allCellInfo
            val activeSubscriptionInfoList = manager.activeSubscriptionInfoList

            val regCellInfo = allCellinfo?.let { getRegisteredCellInfo(it) }

            activeSubscriptionInfoList.forEachIndexed { Subindex, subs ->

                if (activeSubscriptionInfoList.size >= 2) {
                    regCellInfo?.size?.let {
                        if (it >= 2) {

                            if (subs.simSlotIndex == 0) {

                                if (subs.carrierName != "No service") {


                                    strength1 = when (val info1 = regCellInfo?.get(0)) {
                                        is CellInfoLte -> info1.cellSignalStrength.asuLevel
                                        is CellInfoGsm -> info1.cellSignalStrength.asuLevel
                                        is CellInfoCdma -> info1.cellSignalStrength.asuLevel
                                        is CellInfoWcdma -> info1.cellSignalStrength.asuLevel
                                        else -> 0
                                    }

                                    Log.i(TAG, "subs $subs")

                                    Log.i(
                                        TAG,
                                        "sim1   ${subs.carrierName}  ${subs.mnc}  $strength1"
                                    )
                                } else {

                                    strength1 = -1
                                }

                            } else if (subs.simSlotIndex == 1) {

                                if (subs.carrierName != "No service") {

                                    strength2 = when (val info2 = regCellInfo?.get(1)) {
                                        is CellInfoLte -> info2.cellSignalStrength.dbm
                                        is CellInfoGsm -> info2.cellSignalStrength.dbm
                                        is CellInfoCdma -> info2.cellSignalStrength.dbm
                                        is CellInfoWcdma -> info2.cellSignalStrength.dbm
                                        else -> 0
                                    }
                                    Log.i(TAG, "subs $subs")

                                    Log.i(
                                        TAG,
                                        "sim2   ${subs.carrierName}  ${subs.mnc}  $strength2"
                                    )
                                } else {

                                    strength2 = -1
                                }

                            }

                        }
                    }

                } else if (activeSubscriptionInfoList.size == 1) {

                    regCellInfo?.size?.let {
                        if (it >= 1) {

                            if (subs.simSlotIndex == 0) {

                                if (subs.carrierName != "No service") {
                                    strength1 = when (val info1 = regCellInfo[0]) {
                                        is CellInfoLte -> info1.cellSignalStrength.level
                                        is CellInfoGsm -> info1.cellSignalStrength.level
                                        is CellInfoCdma -> info1.cellSignalStrength.level
                                        is CellInfoWcdma -> info1.cellSignalStrength.level
                                        else -> 0
                                    }
                                } else {

                                    strength1 = -1
                                }

                            }
                        }
                    }
                    strength2 = -2

                }
            }

        }

        Log.i(TAG, "final strenght   sim1 $strength1  sim2 $strength2")

        return Pair(strength1, strength2)
    }
}