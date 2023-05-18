package com.pacesoft.sdk.agnos

import android.app.Activity
import android.content.Context
import android.util.Log
import ca.amadis.agnos.sdk.Agnos
import ca.amadis.agnos.sdk.DevListener
import ca.amadis.agnos.sdk.Key
import ca.amadis.agnos.sdk.Rpc
import com.pacesoft.sdk.session.XpssInsta
import com.pacesoft.sdk.vm.user.merchant.terminal.MTerminalVm
import kotlinx.coroutines.delay
import x.code.util.XCoroutines
import x.code.util.view.text.XStr
import java.util.*

class AgnosPaySDK : CardListner {
    private var agnos: Agnos? = null
    private var transactionCancelled = true
    private lateinit var purchase: Transaction
    private lateinit var callbacks: AgnosPayCallbacks
    private lateinit var mActivity: Activity
    private var encryptedData: String = ""
    private var tagParams: LinkedHashMap<String, String?>? = null
    private var txnAmount: Double = 0.0


    fun initializeAgnos(context: Context, activity: Activity, agnosCB: AgnosPayCallbacks) {
        if (agnos == null) {
            callbacks = agnosCB
            mActivity = activity
            Log.v("AgnosPaySDK", "initializeDev")
            agnos = Agnos(devListener, context, activity)

            XCoroutines.main {
                delay(400)
                XCoroutines.io {
                    agnos?.initialize()
                }
            }
        }
    }


    fun terminateAgnos() {
        if (agnos != null) {
            agnos?.terminate()
            agnos = null
        }
    }

    val devListener = object : DevListener {
        override fun cardPresent() {
            Log.v("ola-app", "received card present from dev")
            callbacks.onCardPresent()
        }

        override fun cardRemoved() {
            Log.v("ola-app", "received card removed from dev")
            callbacks.onCardRemoved()
        }

        override fun handleDevEvent(bytes: ByteArray) {
            val cmd = bytes[0].toInt()
//        Log.v("UserMerchantActivity", "--- handleDevEvent ---")

            /* Handles NFC requests locally. Pass the rest onto the higher level app */
            if (cmd == Rpc.Cmd.RPC_CMDID_EVT_LOG.id) {
                if (bytes.size >= 2) {
                    val msg = Arrays.copyOfRange(bytes, 2, bytes.size - 1)
                    Log.v("ola-app", "dev log: " + String(msg))
                }
            }
        }

        override fun handleDevRequest(data: ByteArray): ByteArray? {
            Log.v("UserMerchantActivity", "--- handleDevRequest ---" + data[0].toInt())

            /*return if(transactionCancelled) {
             val cmd = Rpc.Cmd.RPC_CMDID_REQ_PINPAD_GETKEY.id//data[0].toInt()
             *//* Handle CANCEL event only *//*
            return if (cmd == Rpc.Cmd.RPC_CMDID_REQ_PINPAD_GETKEY.id) {
                if (transactionCancelled) {
                    byteArrayOf(Key.PINPAD_CANCEL.value.toByte())
                }else
                    byteArrayOf(Key.PINPAD_NO_KEY.value.toByte())
            } else {
                ByteArray(0)
            }
        }else
            ByteArray(0)*/
            val cmd = data[0].toInt()

            return if (cmd == Rpc.Cmd.RPC_CMDID_REQ_PINPAD_GETKEY.id) {
                if (transactionCancelled) byteArrayOf(Key.PINPAD_CANCEL.value.toByte())
                else byteArrayOf(Key.PINPAD_NO_KEY.value.toByte())
            } else {
                ByteArray(0)
            }
        }

        override fun initializeCompleted() {
            if (mActivity != null) {
                purchase = Transaction(mActivity, agnos!!)
                callbacks.onInitializeCompleted(purchase = purchase)
            }
        }

        override fun initializeFailed(message: String) {
            callbacks.onInitializeFailed(message = message)
        }

    }

    fun cancelTxn() {
        transactionCancelled = true
        XCoroutines.main {
            delay(400)
            XpssInsta.agnosPaySDK.nfcReleaseAdapter(enable = true)
        }

    }

    fun transactionThreadRunning(): Boolean {
        return !transactionCancelled
    }

    fun resetTxn() {
        transactionCancelled = false
        encryptedData = ""
        if (tagParams != null)
            tagParams = null
    }

    fun nfcReleaseAdapter(enable: Boolean) {
        agnos?.setReleaseAdapter(enable)
    }


    override fun cardIsReady(encryptedData: String, params: LinkedHashMap<String, String?>) {
        Log.d("MTerminalTxnTapCardFragment", encryptedData + "")
        if (XStr.isEmpty(encryptedData)) {
//            mUserActivity.snack("Transaction Failed")
//            funOnBackPressed()
            callbacks.txnCancelled("Transaction Failed")
        } else {
//            if(!txnAmount.equals(0))
//                callbacks.txnCancelled("Transaction Amount is Zero.")
            this@AgnosPaySDK.encryptedData = encryptedData
            tagParams = params
            callbacks.transactionIsReady()
//            apiInit_4V2_Sale(encryptedData = encryptedData, params = params)
        }
    }

    override fun cardDeclined(params: HashMap<String, String?>, msg: String) {
        callbacks.transactionDeclined(msg)
    }


    override fun txnCancelled(error: String) {
        Log.d("MTerminalTxnTap", "txnCancelled")
        callbacks.txnCancelled(error)
    }

    fun startTransaction(amount: Double) {
        txnAmount = amount
        purchase!!.setCardListner(this)
    }

    fun processSaleRequest(mTerminalVm: MTerminalVm) {
        tagParams?.let {
            mTerminalVm.processSaleRequest(
                txnAmount, 6, null, encryptedData = encryptedData,
                it
            )
        }
    }

}