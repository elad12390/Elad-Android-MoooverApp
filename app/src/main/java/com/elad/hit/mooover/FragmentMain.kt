package com.elad.hit.mooover

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.elad.hit.mooover.databinding.FragmentMainBinding


class FragmentMain : Fragment() {
    private lateinit var _binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = _binding.root

        _binding.startPackingBtn.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentMain_to_packingFragment)
        }
        return view
    }
}