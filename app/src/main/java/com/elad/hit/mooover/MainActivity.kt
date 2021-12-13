package com.elad.hit.mooover

import android.os.Bundle
import androidx.navigation.findNavController
import com.elad.hit.mooover.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : BoundActivity<ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding?.let {
            setMenuClickListener(it.topAppBar)
        }
    }

    private fun setMenuClickListener(toolbar: MaterialToolbar) = with(toolbar) {
        setOnMenuItemClickListener { menuItem ->
            return@setOnMenuItemClickListener when (menuItem.itemId) {
                R.id.cow_svg -> {
                    binding?.navHostFragment?.findNavController()?.navigate(R.id.fragmentMain)
                    true
                }
                else -> {
                    false
                }
            }
        }
    }


    override fun inflate(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
}