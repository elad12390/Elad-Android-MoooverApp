package com.elad.hit.mooover

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BoundActivity<T : ViewBinding?> : AppCompatActivity() {
    protected var binding: T? = null
    protected var view: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate()
        view = binding!!.root
        setContentView(view)
    }

    abstract fun inflate(): T
}