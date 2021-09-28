package com.aracoix.registeradapter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun one2More(view: android.view.View) {
        startActivity(Intent(this,One2MoreActivity::class.java))
    }
    fun one2One(view: android.view.View) {
        startActivity(Intent(this,One2OneActivity::class.java))
    }
    fun mvvm(view: android.view.View) {
        startActivity(Intent(this,MVVMListActivity::class.java))
    }
    fun holder2view(view: android.view.View) {
        startActivity(Intent(this,Holder2ViewActivity::class.java))
    }
}