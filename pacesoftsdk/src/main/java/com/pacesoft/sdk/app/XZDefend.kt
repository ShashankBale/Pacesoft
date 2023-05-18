package com.pacesoft.sdk.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pacesoft.sdk.ui.zDefend.TemperedActivity
import com.zimperium.zdetection.api.v1.*
import com.zimperium.zdetection.api.v1.enums.ThreatSeverity
import x.code.util.XCoroutines
import x.code.util.log.elog

class XZDefend(private val mCtx: Context) {
    private val zDlicense =
        "U2FsdGVkX1-oqKvEK-vzrFwzQT_d7_G_H02PuwdYrryO82jiNef6KIn2jIPf6DdBqVm6VnCz6r3PzWemREzsqNHOqgSWP4uEZUAfyvV0aU6F3QwHCDS2Se69TxkXmD9ijfQb_vpHaOjiXUZjoHppHQGChyKnxUtiZg5WtJQuYu0JKRKPptEgm4sOsfPaCHqx1xeFs5FRcHOKuoh3SZ31JH7nXoS0XHmocP4qGOsnpoQG-Z-1avL3WMdnC6FhX-9zuYhkeX29oXNim7lfk7hvJwekB37Y3iXo1TsvPiumOyQSLZ_A5ngYSzQF2qDQ6hv1yrEw2NEAVMvSMXPo_sH0IU6pCaGeoARCrlA-abAfGcqtNHFaGA7yAfMgWeQKMs5f4wGlqdPQBlk6_-LiQ32fEMqrjodnTbjRjEHbZNopoVZdrPkv6fa632VnjUgqVMLfh5EdPc9wzUBt5N40ZFyDYOrsCaFXi7e0-ilxsuRNIENaAv9oOlrLf8G1z4U922IL"

    private val mLdAlThreat = MutableLiveData<List<Threat>>()
    private var mIsRooted = false
    private var mIsCompromised = false

    //To check the zDefend initiation state
    private var mzDefendInitialized = false

    //To maintain the zDefend detection state
    private var mzDefendDetecting = false

    //MutableLiveData object for Detected Thread, one at a time
    private val mldDetectedThreat = MutableLiveData<Threat>()

    //MutableLiveData object for zDefend Detection State
    private val mldDetectionState = MutableLiveData<DetectionState>()

    //LiveData object for Active Threats
    fun activeThreatsLiveData(): LiveData<List<Threat>> = mLdAlThreat

    //LiveData object of the last detected Threat.
    fun detectedThreatLiveData(): LiveData<Threat> = mldDetectedThreat

    //LiveData object of the latest DetectionState
    fun detectionStateLiveData(): LiveData<DetectionState> = mldDetectionState

    //Centralize log function
    private fun info(text: String) = Log.i("Pacesoft<>zDefend", text)

    //Retrieve the Detection State and update the DetectionState live data
    private val mDetectionStateCallback = DetectionStateCallback { oldState, newState ->
        //info("\tonZDefendDetectionStateChanged#OldState: $oldState")
        info("\tonZDefendDetectionStateChanged#NewState: $newState")
        mldDetectionState.postValue(newState)
    }

    //Retrieve the Last Threat Object and log them, if it's critical then show a Tempered screen.
    private val mAllThreatCallback = ThreatCallback { uri: Uri?, threat: Threat ->
        info("CriticalThreats======")
        info("Detected Threat:" + threat.humanThreatName)
        info(" - Severity: " + threat.severity)
        info(" - Type: " + threat.humanThreatType)
        info(" - Description: " + threat.humanThreatSummary)
        info(" - Uri: " + uri.toString())
        mldDetectedThreat.postValue(threat)

        if (isThreatSeverityForTemperedPage(threat)) {
            //showCriticalThreat(threat)
            if (TemperedActivity.isTemperedPageCreated == false)
                startTemperedActivity(threat)
        }
    }

    private fun isThreatSeverityForTemperedPage(threat: Threat) =
        threat.threatSeverity == ThreatSeverity.CRITICAL || threat.threatSeverity == ThreatSeverity.IMPORTANT

    /**
     * Init zDefend Observer
     * If valid threat got detected than Start Tempered Activity
     **/
    /*fun initZDefendObservers() {
        //Listen to new threat detection
        mldDetectedThreat.observeForever { threat: Threat? ->
            threat ?: return@observeForever
            startTemperedActivity(threat)
        }
    }*/

    private fun startTemperedActivity(threat: Threat) {
        //If Thread is detected than navigate to Tempered Screen
        val threatInternalID = threat.threatInternalID
        elog("BA Threat detected $threatInternalID")
        val intent = TemperedActivity.newIntention(PaceSoftSdk.ctx)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        PaceSoftSdk.ctx.startActivity(intent)
    }


    //Launcher Dialog for Critical Thread
    /*private fun showCriticalThreat(threat: Threat) {
        //Show the alert.
        val intent = Intent(
            mCtx,
            com.pacesoft.sdk.ui.zDefend.ZDefendThreatActivity::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("type", threat.threatType.name())
        intent.putExtra("title", threat.humanThreatName)
        intent.putExtra(
            "description",
            if (threat.humanThreatSummary != null) threat.humanThreatSummary.toString() else ""
        )
        mCtx.startActivity(intent)
    }*/

    /**
     * Set license from resource.
     * add DetectionState callback.
     * Initialize the detection instance with our configuration -- it kicks off the
     * authentication process.
     */
    fun initializeZDefend() {
        info("initializeZDefend()")
        if (mzDefendInitialized) return

        mzDefendInitialized = true
        try {
            ZDetection.setLicenseKey(mCtx, zDlicense.toByteArray())
            ZDetection.addDetectionStateCallback(mDetectionStateCallback)
        } catch (e: Exception) {
            mzDefendInitialized = false
            info("\tException: $e")
        }
    }


    //Start Thread Detection process
    fun startDetection() {
        if (!mzDefendDetecting) {
            mzDefendDetecting = true
            //ZDetection.detectCriticalThreats(mCtx, mAllThreatCallback)
            ZDetection.detectAllThreats(mCtx, mAllThreatCallback)
        }
    }

    //Stop all the threat callbacks.
    fun stopZDefend() {
        info("stopZDefend()")
        if (mzDefendDetecting) {
            mzDefendDetecting = false
            ZDetection.stopDetecting()
        }
    }

    fun getActiveThreat() {
        XCoroutines.default {
            val disposition = ZThreatDisposition(PaceSoftSdk.ctx)
            mIsRooted = disposition.isRooted
            mIsCompromised = disposition.isCompromised
            val activeThreats = disposition.activeThreats
            mLdAlThreat.postValue(activeThreats)

            /* TODO : Sha2nk To Sha2nk, check this logic
            clear SKB keys
            finish()
            startActivity()
            * */

            if (TemperedActivity.isTemperedPageCreated == false &&
                activeThreats.isNotEmpty()
            ) {
                XCoroutines.main {
                    //In UI thread, set the new list.
                    //threatAdapter.setThreats(threats)
                    activeThreats.forEach {
                        if (isThreatSeverityForTemperedPage(it))
                            startTemperedActivity(it)
                        if(TemperedActivity.isTemperedPageCreated == false)
                            return@forEach
                    }
                }
            }
        }
    }
}