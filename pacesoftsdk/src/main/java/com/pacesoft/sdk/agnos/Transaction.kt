package com.pacesoft.sdk.agnos

import android.app.Activity
import android.util.Log
import ca.amadis.agnos.sdk.Agnos
import ca.amadis.agnos.sdk.ola.types.OlaError
import ca.amadis.agnos.sdk.ola.types.OlaOutcomeParameter
import ca.amadis.agnos.sdk.ola.types.OlaTag
import ca.amadis.agnos.sdk.ola.types.PublicKeyData
import ca.amadis.agnos.sdk.ola.utils.Holders
import com.pacesoft.sdk.util.utility.Utils
import x.code.util.XConst
import kotlin.experimental.or

class Transaction(private val activity: Activity?, val agnos: Agnos) : Thread() {
    private var amount: Long = 0
    private lateinit var listner: CardListner
    private var isRunning = false
    private val AID_CARD = 0x4F
    private val APPLICATION_LABEL = 0x50
    private val IIN = 0x42
    private val AID_TERMINAL = 0x9F06
    private val DF_NAME = 0x84
    private val APPLICATION_PRIORITY_INDICATOR = 0x87
    private val clcEMV_CARD = 0x04
    private val batch = intArrayOf(
        APPLICATION_LABEL,
        AID_TERMINAL,
        AID_CARD,
        OlaTag.KERNEL_2_CONFIGURATION,
        OlaTag.AID_CARD,
        OlaTag.DF_NAME,
        OlaTag.APPLICATION_LABEL,
        OlaTag.PAN_SEQUENCE_NUMBER,
        OlaTag.APPLICATION_VERSION_NUMBER_TERMINAL,
        OlaTag.APPLICATION_INTERCHANGE_PROFILE,
        OlaTag.TVR,
        OlaTag.CVM_RESULTS,
        OlaTag.TRANSACTION_STATUS_INFORMATION,
        OlaTag.APPLICATION_TRANSACTION_COUNTER,
        OlaTag.TRANSACTION_SEQUENCE_COUNTER,
        OlaTag.UNPREDICTABLE_NUMBER,
        OlaTag.ICC_DYNAMIC_NUMBER,
        OlaTag.TRANSACTION_TYPE,
        OlaTag.TERMINAL_TYPE,
        OlaTag.TERMINAL_CAPABILITIES,
        OlaTag.ADDITIONAL_TERMINAL_CAPABILITIES,
        OlaTag.TERMINAL_COUNTRY_CODE,
        OlaTag.TRANSACTION_CURRENCY_CODE,
        OlaTag.ISSUER_APPLICATION_DATA,
        OlaTag.APPLICATION_CRYPTOGRAM,
        OlaTag.CRYPTOGRAM_INFORMATION_DATA,
        OlaTag.TTQ,
        OlaTag.FORM_FACTOR_INDICATOR,
        OlaTag.CUSTOMER_EXCLUSIVE_DATA,
        OlaTag.INTERFACE_DEVICE_SERIAL_NUMBER,
        OlaTag.ACCOUNT_TYPE,
        OlaTag.POS_ENTRY_MODE,
        0x9F53,
        0x9F7C
    )


    init {
    }

    /**
     * Configures OLA layer (key files paths, trace, etc...)
     */


    /**
     * Configures terminal permanent data
     */
    private fun terminalConfig() {

        Log.v("Transaction", "terminalConfig")
        terminalOlaConfig()
        terminalContactlessConfig()
        terminalPublicKeyConfig()

        val versions = agnos.olaAgnosVersions
        Log.v("Transaction", "terminalConfig - versions: $versions")
    }

    private fun terminalOlaConfig() {

        Log.v("Transaction", "terminalOlaConfig")
        agnos.olaInitializeAtStartUp(Config.getOlaConfig()!!)
    }

    private fun terminalContactlessConfig() {

        Log.v("Transaction", "terminalContactlessConfig" + agnos.olaAgnosVersions)
        agnos.olaContactlessFlushAIDSupported()
        for (config in Config.contactlessCfg) {
            agnos.olaContactlessAddAIDSupported(
                config.aid,
                config.partial,
                config.kernelId.toByte(),
                config.additionalData
            )
        }
        agnos.olaContactlessCommitSupportedAIDs()
    }

    private fun terminalPublicKeyConfig() {

        Log.v("Transaction", "terminalPublicKeyConfig")
        agnos.olaPublicKeyFlush()

        for (config in Config.publicKeyCfg) {
            val maxReached = Holders.SingleObjectHolder(false)
            val key: PublicKeyData = PublicKeyData(
                config.rid,
                config.idx,
                config.modulus,
                config.exponentValue,
                config.expirDate
            )
            agnos.olaPublicKeyAdd(key, null, maxReached)
        }

        agnos.olaPublicKeyCommit()
    }

    fun setAmount(amount: Long) {

        this.amount = amount
    }

    fun setCardListner(listner: CardListner) {
        this.listner = listner
    }

    override fun run() {
        isRunning = true
        terminalConfig()

        Log.v("Transaction", "run")
        //activity.switchLed(1, 0, 3)
        //activity.switchLed(2, 0, 3)
        //activity.switchLed(3, 0, 3)
        //activity.switchLed(4, 0, 3)

        txnSetTransactionRelatedData()
        val ret = txnPreProcess()
        if (ret != OlaError.OLA_OK) {
            isRunning = false
            listner.txnCancelled("Transaction Pre Process Cancelled")
            return
        }

        //activity.setText("Please present card")
        //activity.switchLed(1, 1, 4)

        val card = txnGetCard()
        if (card != clcEMV_CARD) {
            //waitAndClear()
            if (isRunning)
                listner.txnCancelled("Time out")

            return
        }

        //activity.setText("Processing...")
        //activity.switchLed(2, 1, 4)
        doTransaction()

        //activity.switchLed(3, 1, 4)
        val outcome = Holders.SingleObjectHolder<OlaOutcomeParameter>()
        agnos.olaContactlessGetOutcome(outcome)
        //activity.setText(outcome.get()?.outcome?.name ?: "  ")
        if (outcome.get()?.outcome == OlaOutcomeParameter.Outcome.Approved ||
            outcome.get()?.outcome == OlaOutcomeParameter.Outcome.OnlineRequest
        ) {
            //activity.switchLed(4, 1, 4)
        } else {
            //activity.switchLed(1, 1, 3)
            //activity.switchLed(2, 1, 3)
            //activity.switchLed(3, 1, 3)
            //activity.switchLed(4, 1, 3)
        }

        waitAndClear()
    }

    private fun waitAndClear() {
        /* Clear everything after 5 sec */
        sleep(5000)
        //activity.clearLeds()
        //activity.setText("")
        //activity.enableDisableButtons(true)
    }

    private fun decimalToBcd(num: Long): ByteArray {
        var num = num
        require(num >= 0) {
            "The method decimalToBcd doesn't support negative numbers." +
                    " Invalid argument: " + num
        }

        var digits = 0
        var temp = num
        while (temp != 0L) {
            digits++
            temp /= 10
        }

        val byteLen = 6 //if (digits % 2 == 0) digits / 2 else (digits + 1) / 2
        val bcd = ByteArray(byteLen)
        for (i in 0 until digits) {
            val tmp = (num % 10).toByte()
            if (i % 2 == 0) {
                bcd[i / 2] = tmp
            } else {
                bcd[i / 2] = bcd[i / 2] or (tmp.toInt() shl 4).toByte()
            }
            num /= 10
        }

        for (i in 0 until byteLen / 2) {
            val tmp = bcd[i]
            bcd[i] = bcd[byteLen - i - 1]
            bcd[byteLen - i - 1] = tmp
        }

        return bcd
    }

    private fun txnSetTransactionRelatedData() {

        Log.v("Transaction", "txnSetTransactionRelatedData")
        agnos.olaEmvSetTag(OlaTag.TRANSACTION_DATE, byteArrayOf(0x20, 0x10, 0x28))                   // 9A
        agnos.olaEmvSetTag(OlaTag.TRANSACTION_TIME, byteArrayOf(0x12, 0x00, 0x00))                   // 9F21
        agnos.olaEmvSetTag(OlaTag.AMOUNT_AUTHORISED, decimalToBcd(this.amount))                      // 9F02
        agnos.olaEmvSetTag(OlaTag.AMOUNT_OTHER_NUM, byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x50)) // 9F03
        agnos.olaEmvSetTag(OlaTag.TRANSACTION_CURRENCY_CODE, byteArrayOf(0x08, 0x40))                // 5F2A
        agnos.olaEmvSetTag(OlaTag.TRANSACTION_CURRENCY_EXPONENT, byteArrayOf(0x02))                  // 5F36
        agnos.olaEmvSetTag(OlaTag.TRANSACTION_TYPE, byteArrayOf(0x00))                               // 9C

        /*
         * Other possible tags to set here:
         * Account Type (5F57)
         * Transaction Category Code (9F53)
         *
         * All other tags must be set through the combinations!
         */

        agnos.olaEmvSetTag(0x9F53, byteArrayOf(0x11));
        agnos.olaEmvSetTag(0x9F7C, byteArrayOf(0x55, 0x44, 0x33, 0x22, 0x11));
    }

    private fun txnPreProcess(): OlaError {

        Log.v("Transaction", "txnPreProcess")
        val ret = agnos.olaContactlessPreprocess()
        if (ret != OlaError.OLA_OK) {
            Log.e("Transaction", "txnPreProcess - contactless pre-process failed")
        }
        return ret
    }

    private fun txnGetCard(): Int {

        Log.v("Transaction", "txnGetCard - **** PRESENT CARD ****")
        val foundTechnos = agnos.technoPolling(XConst.terminalCardReaderTimerInSec.toInt())
        Log.v("Transaction", "txnGetCard - result of foundTechnos: $foundTechnos")
        return if (foundTechnos == 4) {
            clcEMV_CARD //TODO remove hard-coding
        } else {
            0
        }
    }

    private fun doTransaction(): OlaError {

        Log.v("Transaction", "doTransaction - buildCandidateList")
        val nbCandidatesHolder = Holders.SingleObjectHolder<Int>()
        var ret = agnos.olaContactlessBuildCandidateList(nbCandidatesHolder)
        if (ret != OlaError.OLA_OK) {
            Outcome.showOutcome(agnos, ret)
        }

        Log.v("Transaction", "doTransaction - nb of candidates: ${nbCandidatesHolder.get()}")
        Log.v("Transaction", "doTransaction - finalSelectCandidate")
        val kernel_id_holder = Holders.SingleObjectHolder<Byte>()
        ret = agnos.olaContactlessFinalSelectCandidate(1, kernel_id_holder)
        if (ret != OlaError.OLA_OK) {
            Outcome.showOutcome(agnos, ret)
        }

        Log.v("Transaction", "doTransaction - doTransaction")
        ret = agnos.olaContactlessDoTransaction()
        Outcome.showOutcome(agnos, ret)
        var map = LinkedHashMap<String, String?>()
        if (ret == OlaError.OLA_OK) {
            val bytesHolder = Holders.SingleObjectHolder<ByteArray>()
            for (olaTag in batch) {
                val result = agnos.olaEmvGetTag(olaTag, bytesHolder)
                if (result == OlaError.OLA_OK) {
                    val n: String = fetchTagName(olaTag)
                    if (!n.isEmpty()) {
                        map.put(n, bytesHolder.get()?.let { Utils.bytesToHex(it) })
                        Log.v("Transaction", "doTransaction - result for getTag($n): $result => " +
                                bytesHolder.get()?.let { Utils.bytesToHex(it) })
                    }
                } else {
                    Log.v(
                        "Transaction",
                        "doTransaction - result for getTag(): $result => tag missing"
                    )

                }
            }
        }

        val outcomeHolder = Holders.SingleObjectHolder<OlaOutcomeParameter>()
        ret = agnos.olaContactlessGetOutcome(outcomeHolder)
        val outcome = outcomeHolder.get()
        Log.v("Transaction", "doTransaction - cvm: " + outcome?.cvm)
        if (outcome?.cvm == OlaOutcomeParameter.CVMethodContactless.OnlinePin) {
            //activity.enterPin()
            listner?.cardDeclined(map, "ONLINE PIN REQUIRED")
        }


        //if (!BuildConfig.FLAVOR.lowercase().contains("nosred")) {
        //    Sred.exportHashedPAN(agnos)
        //    Sred.exportKCV(agnos)
        try {
            val exportedData = Sred.decryptDataBlock(agnos)
            if (exportedData.equals("NA")) {
                listner?.cardDeclined(map, "Card Reading Error")
                ret = OlaError.OLA_MISSING_DATA
            } else {
                listner?.cardIsReady(exportedData, map)
                ret = OlaError.OLA_OK
                Log.d("Transaction", "doTransaction - **** REMOVE CARD **** ")
            }

        } catch (e: Exception) {
            Log.e("Transaction", "exception:" + e.message)
            ret = OlaError.OLA_MISSING_DATA
        } finally {
            agnos.olaContactlessClean();
        }


        return ret
    }

    fun cancel() {
        isRunning = false
    }

    private fun fetchTagName(olaTag: Int): String {
        when (olaTag) {
            APPLICATION_LABEL -> return "50"
            AID_TERMINAL -> return "9F06"
            AID_CARD -> return "4F"
            OlaTag.KERNEL_2_CONFIGURATION -> return "DF79"
            OlaTag.AID_CARD -> return "4F"
            OlaTag.DF_NAME -> return "84"
            OlaTag.APPLICATION_LABEL -> return "50"
            OlaTag.PAN_SEQUENCE_NUMBER -> return "5F34"
            OlaTag.APPLICATION_VERSION_NUMBER_TERMINAL -> return "9F09"
            OlaTag.APPLICATION_INTERCHANGE_PROFILE -> return "82"
            OlaTag.TVR -> return "95"
            OlaTag.CVM_RESULTS -> return "9F34"
            OlaTag.TRANSACTION_STATUS_INFORMATION -> return "9B"
            OlaTag.APPLICATION_TRANSACTION_COUNTER -> return "9F36"
            OlaTag.TRANSACTION_SEQUENCE_COUNTER -> return "9F41"
            OlaTag.UNPREDICTABLE_NUMBER -> return "9F37"
            OlaTag.ICC_DYNAMIC_NUMBER -> return "9F4C"
            OlaTag.TRANSACTION_TYPE -> return "9C"
            OlaTag.TERMINAL_TYPE -> return "9F35"
            OlaTag.TERMINAL_CAPABILITIES -> return "9F33"
            OlaTag.ADDITIONAL_TERMINAL_CAPABILITIES -> return "9F40"
            OlaTag.TERMINAL_COUNTRY_CODE -> return "9F1A"
            OlaTag.TRANSACTION_CURRENCY_CODE -> return "9F42"
            OlaTag.ISSUER_APPLICATION_DATA -> return "9F10"
            OlaTag.APPLICATION_CRYPTOGRAM -> return "9F26"
            OlaTag.CRYPTOGRAM_INFORMATION_DATA -> return "9F27"
            OlaTag.TTQ -> return "9F66"
            OlaTag.FORM_FACTOR_INDICATOR -> return "9F6E"
            OlaTag.CUSTOMER_EXCLUSIVE_DATA -> return "9F7C"
            OlaTag.INTERFACE_DEVICE_SERIAL_NUMBER -> return "DF78"
            OlaTag.ACCOUNT_TYPE -> return "5F57"
            OlaTag.POS_ENTRY_MODE -> return "9F39"
            0x9F53 -> return "9F53"
            0x9F7C -> return "9F7C"

        }
        return ""
    }
}
