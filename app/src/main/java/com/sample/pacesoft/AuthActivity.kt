package com.sample.pacesoft

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.pacesoft.sdk.network.repo.pojo.auth.login.AuthLoginReq
import com.pacesoft.sdk.network.repo.pojo.auth.login.AuthLoginRsp
import com.pacesoft.sdk.network.repo.pojo.auth.otpVerification.AuthLoginOtpVerificationRsp
import com.pacesoft.sdk.vm.AuthVm
import com.sample.pacesoft.user.DashboardActivity
import x.code.util.log.toast
import x.code.util.repo.Resource
import x.code.util.repo.Status

class AuthActivity : AppCompatActivity() {

    private lateinit var psViewModel: AuthVm
    private lateinit var etCountryCode : EditText
    private lateinit var etPhoneNumber : EditText
    private lateinit var etVerificationCode : EditText
    private lateinit var pbGenerateVerificationCode : ProgressBar
    private lateinit var pbSubmitVerificationCode : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        registerUi()
        initObjects()
        addListeners()
        attachObservers()
    }

    private fun initObjects() {
        psViewModel = ViewModelProvider(this)[AuthVm::class.java]
    }

    private fun registerUi() {
        etCountryCode = findViewById(R.id.etCountryCode)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etVerificationCode = findViewById(R.id.etVerificationCode)
        pbGenerateVerificationCode = findViewById(R.id.pbGenerateVerificationCode)
        pbSubmitVerificationCode = findViewById(R.id.pbSubmitVerificationCode)
    }

    private fun addListeners() {
        findViewById<View>(R.id.btnGenerateVerificationCode).setOnClickListener {
            onClickGenerateVerificationCode()
        }

        findViewById<View>(R.id.btnSubmitVerificationCode).setOnClickListener {
            onClickSubmitVerificationCode()
        }
    }

    private fun onClickGenerateVerificationCode() {
        val countryCode = etCountryCode.text.toString()
        val phoneNumber = etPhoneNumber.text.toString()

        /*
            phoneNumber = "CountryCode" + "Number"
            eg. +15123412345, +919876543210
            */

        val authLogin = AuthLoginReq(phoneNumber, "123")
        psViewModel.apiInit_4_Login(authLogin)
    }

    private fun onClickSubmitVerificationCode() {

    }


    private val psvmObsAuthLogin = Observer<Resource<AuthLoginRsp?>> {

        it?.let { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showProgressForGenerateVerificationCode(true)
                }
                Status.SUCCESS -> {
                    showProgressForGenerateVerificationCode(false)
                    popApiRspForGenerateVerificationCode(resource.data)
                }
                Status.ERROR -> {
                    showProgressForGenerateVerificationCode(false)
                    popApiRspForGenerateVerificationCode(null)
                }
            }
        }
    }


    private fun showProgressForGenerateVerificationCode(flag: Boolean) {
        pbGenerateVerificationCode.isVisible = flag
        etPhoneNumber.isEnabled = !flag
    }


    private val psvmObsAuthOtpVerification =
        Observer<Resource<AuthLoginOtpVerificationRsp?>> {

            it?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        showProgressForSubmitVerificationCode(true)
                    }
                    Status.SUCCESS -> {
                        showProgressForSubmitVerificationCode(false)
                        val data = resource.data
                        popApiRspForSubmitVerificationCode(data)
                    }
                    Status.ERROR -> {
                        showProgressForSubmitVerificationCode(false)
                        toast(resource.msg ?: "Login OTP failed, please try after sometime.")
                    }
                }
            }
        }


    private fun showProgressForSubmitVerificationCode(flag: Boolean) {
        pbSubmitVerificationCode.isVisible = flag
        etVerificationCode.isEnabled = !flag
    }


    fun attachObservers() {
        psViewModel.ldAuthLogin.observe(this, psvmObsAuthLogin)
        psViewModel.ldLoginOtpVerification.observe(this, psvmObsAuthOtpVerification)
    }

    fun detachObservers() {
        psViewModel.ldAuthLogin.removeObserver(psvmObsAuthLogin)
        psViewModel.ldLoginOtpVerification.removeObserver(psvmObsAuthOtpVerification)
    }

    private fun popApiRspForGenerateVerificationCode(data: AuthLoginRsp?) {
        if (data?.status != true) {
            toast("Incorrect phone number")
        } else {
            toast("Verification code generated")
        }
    }

    private fun popApiRspForSubmitVerificationCode(data: AuthLoginOtpVerificationRsp?) {
        if (data?.status != true) {
            toast("Incorrect phone number")
        } else {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detachObservers()
    }
}