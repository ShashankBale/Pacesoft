package com.pacesoft.sdk.agnos

import android.util.Log
import ca.amadis.agnos.sdk.ola.types.OlaUiRequest
import ca.amadis.agnos.sdk.Agnos
import ca.amadis.agnos.sdk.ola.types.OlaError
import ca.amadis.agnos.sdk.ola.types.OlaOutcomeParameter
import ca.amadis.agnos.sdk.ola.utils.Holders

object Outcome {

    fun showOutcome(agnos: Agnos, previousResult: OlaError) {

        Log.v("OlaOutcome", "showOutcome - previous result before showOutcome(): $previousResult")
        val outcomeHolder = Holders.SingleObjectHolder<OlaOutcomeParameter>()
        val ret = agnos.olaContactlessGetOutcome(outcomeHolder)
        Log.v("OlaOutcome", "showOutcome - getOutcome() ret: $ret")

        val outcome = outcomeHolder.get()
        Log.v("OlaOutcome", "showOutcome - outcome: " + outcome?.outcome)
        Log.v("OlaOutcome", "showOutcome - startingPoint: " + outcome?.startingPoint)
        Log.v("OlaOutcome", "showOutcome - onlineResponseData: " + outcome?.onlineResponseData)
        Log.v("OlaOutcome", "showOutcome - cvm: " + outcome?.cvm)

        Log.v("OlaOutcome", "showOutcome - UIReqOnOutcomePresent: " + outcome?.UIReqOnOutcomePresent)
        if (outcome?.UIReqOnOutcomePresent == true) {
            showUIRequestOutcome(agnos)
        }

        Log.v("OlaOutcome", "showOutcome - UIReqOnRestartPresent: " + outcome?.UIReqOnRestartPresent)
        if (outcome?.UIReqOnRestartPresent == true) {
            showUIRequestRestart(agnos)
        }

        Log.v("OlaOutcome", "showOutcome - dataRecordPresent: " + outcome?.dataRecordPresent)
        Log.v("OlaOutcome", "showOutcome - discretionaryDataPresent: " + outcome?.discretionaryDataPresent)
        Log.v("OlaOutcome", "showOutcome - alternateInterfacePreference: " + outcome?.alternateInterfacePreference)
        Log.v("OlaOutcome", "showOutcome - fieldOffReq: " + outcome?.fieldOffReq)
        Log.v("OlaOutcome", "showOutcome - removalTimeout: " + outcome?.removalTimeout)
    }

    private fun showUIRequestOutcome(agnos: Agnos) {

        Log.v("OlaOutcome", "showUIRequestOutcome")
        val holder = Holders.SingleObjectHolder<OlaUiRequest>()
        val ret = agnos.olaContactlessGetUIRequestUponOutcome(holder)
        Log.v("OlaOutcome", "showUIRequestOutcome - getUIRequestUponOutcome() ret: $ret")
        if (ret == OlaError.OLA_OK) {
            val req = holder.get()
            if (req != null) {
                Log.v("OlaOutcome", "showUIRequestOutcome - messageIdentifier: " + req.messageIdentifier)
                Log.v("OlaOutcome", "showUIRequestOutcome - status: " + req.status)
                Log.v("OlaOutcome", "showUIRequestOutcome - holdTime: " + req.holdTime)
            }
        }
    }

    private fun showUIRequestRestart(agnos: Agnos) {

        Log.v("OlaOutcome", "showUIRequestRestart")
        val holder = Holders.SingleObjectHolder<OlaUiRequest>()
        val ret = agnos.olaContactlessGetUIRequestRestart(holder)
        if (ret == OlaError.OLA_OK) {
            val req = holder.get()
            if (req != null) {
                Log.v("OlaOutcome", "showUIRequestRestart - getUIRequestRestart() ret: $ret")
                Log.v("OlaOutcome", "showUIRequestRestart - messageIdentifier: " + req.messageIdentifier    )
                Log.v("OlaOutcome", "showUIRequestRestart - status: " + req.status)
                Log.v("OlaOutcome", "showUIRequestRestart - holdTime: " + req.holdTime)
            }
        }
    }
}
