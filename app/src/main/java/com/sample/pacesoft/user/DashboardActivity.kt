package com.sample.pacesoft.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sample.pacesoft.R

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        registerUi()
    }

    private fun registerUi() {

    }
}