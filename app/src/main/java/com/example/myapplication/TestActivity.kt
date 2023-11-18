package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class TestActivity : AppCompatActivity() {
    val TAG = "Test Activity "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        Log.d(TAG, "onCreate")

    }
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")

    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")

    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")

    }
}