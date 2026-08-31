package com.lunar.matcher

import android.app.Application
import org.opencv.android.OpenCVLoader

class LunarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        OpenCVLoader.initDebug()
    }
}
