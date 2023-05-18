package com.pacesoft.sdk.ui.zDefend

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pacesoft.sdk.R
import com.pacesoft.sdk.app.PaceSoftSdk
//import com.zimperium.zdetection.api.v1.Threat
//import com.zimperium.zdetection.api.v1.enums.ThreatSeverity
import java.text.SimpleDateFormat
import java.util.*

class TemperedActivity : AppCompatActivity() {

    companion object {
        fun newIntention(
            context: Context?
        ): Intent {
            return Intent(context, TemperedActivity::class.java)
        }

        var isTemperedPageCreated = false
    }

    private var tvReferenceNo: TextView? = null
    private var vgZDThreat: View? = null
    private var tvTime: TextView? = null
    private var tvTitle: TextView? = null
    private var tvDescription: TextView? = null
    private var tvAllThreat: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tempered)

        isTemperedPageCreated = true

        initObject()

        PaceSoftSdk.callback.onForceUserLogout()


        // Listen to new threat detection
        /*PaceSoftSdk.mXzDefend.detectedThreatLiveData().observe(this) { zDThreat: Threat? ->
            zDThreat ?: return@observe
            vgZDThreat?.show()
            tvReferenceNo?.show()
            tvTime?.text = SimpleDateFormat.getDateTimeInstance().format(Date(zDThreat.attackTime))
            tvTitle?.text = zDThreat.humanThreatName
            tvDescription?.text = zDThreat.humanThreatSummary
            tvReferenceNo?.text = "Error code : ${getReferenceId(zDThreat)}"

            if (zDThreat.threatSeverity == ThreatSeverity.CRITICAL)
                vgZDThreat?.setBackgroundResource(R.drawable.rect_c6_red_500_stroke)
            else
                vgZDThreat?.setBackgroundResource(R.drawable.rect_c6_divider_color_stroke)
        }*/


        // Listen to new threat detection
        /*PaceSoftSdk.mXzDefend.activeThreatsLiveData().observe(this) { alThreat: List<Threat>? ->
            alThreat ?: return@observe

            val sb = StringBuffer()

            var latestReferenceNo = ""
            alThreat.forEach {
                if (it.threatSeverity == ThreatSeverity.CRITICAL)
                    sb.append("CRITICAL : ")

                val referenceId = getReferenceId(it)

                sb.append("$referenceId - ${it.humanThreatName}")
                sb.append("\n")
                if (it.packageName.isNotEmpty())
                    sb.append("(${it.packageName})")
                if (it.humanThreatSummary.isNotEmpty())
                    sb.append("-- ${it.humanThreatSummary}")
                sb.append("\n")
                sb.append("\n")
                latestReferenceNo = referenceId
            }

            tvAllThreat?.text = sb.toString()
            tvReferenceNo?.text = "Error code : $latestReferenceNo"
        }*/
    }

    //private fun getReferenceId(it: Threat) = "E${it.threatInternalID}"

    private fun initObject() {
        initUi()
    }

    private fun initUi() {
        vgZDThreat = findViewById(R.id.vgZDThreat)
        tvReferenceNo = findViewById(R.id.tvReferenceNo)
        tvTime = findViewById(R.id.threat_time)
        tvTitle = findViewById(R.id.threat_title)
        tvDescription = findViewById(R.id.threat_description)
        tvAllThreat = findViewById(R.id.tvAllThreat)
    }

    override fun onDestroy() {
        super.onDestroy()
        isTemperedPageCreated = false
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    fun View.show(isShow: Boolean = true) {
        this.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    fun View.hide() {
        this.visibility = View.GONE
    }
}
