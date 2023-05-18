package x.code.app

import android.app.Application
import android.content.Context

object XCodeApp {
    lateinit var app: Application
    val ctx: Context by lazy { app.applicationContext }
}