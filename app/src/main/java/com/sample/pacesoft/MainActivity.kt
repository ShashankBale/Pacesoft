package com.sample.pacesoft

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pacesoft.sdk.session.XpssInsta
import com.sample.pacesoft.R
import com.sample.pacesoft.user.DashboardActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if(XpssInsta.isNotAuthorized())
            startActivity(Intent(this, AuthActivity::class.java))
        else
            startActivity(Intent(this, DashboardActivity::class.java))
    }
}