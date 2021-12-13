package com.elad.hit.mooover

import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import com.elad.hit.mooover.Extensions.openErrorSnackbar
import com.elad.hit.mooover.Extensions.openSnackbar
import com.elad.hit.mooover.Extensions.openTopSnackbar
import com.elad.hit.mooover.databinding.FragmentFinishBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class Finish : Fragment() {
    private val dateFormat = DateTimeFormatter.ISO_DATE
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    private lateinit var _binding: FragmentFinishBinding
    private var ignoredFirstDateSelection = false

    private lateinit var _items: Array<CartItem>
    private var items: Array<CartItem>
        get() = _items
        set(items) {
            _items = items
        }

    private lateinit var _dateTimeSelection: DateTimeSelection
    private var dateTimeSelection: DateTimeSelection
        get() = _dateTimeSelection
        set(selection) {
            _dateTimeSelection = selection
            _binding.dateTimeTxt.text = getString(R.string.date_s_time_s, selection.date.format(dateFormat), selection.time.format(timeFormat))
        }

    private val total get() = Collections.synchronizedList(items.asList()).map { cartItem -> cartItem.price }.sum()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFinishBinding.inflate(inflater, container, false)

        getItemsFromArgs()
        configureCalendar()
        subscribeToSendClick()

        return _binding.root
    }

    private fun subscribeToSendClick() {
        _binding.sendBtn.setOnClickListener {
            shareCartAsCSV()
        }
    }

    private fun configureCalendar() {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DATE, 1)
        val tomorrowInMillis = calendar.time.time
        val today = Calendar.getInstance()
        _binding.calendar.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)) {
                view, year, month, dayOfMonth ->
                    if (!ignoredFirstDateSelection) {
                        ignoredFirstDateSelection = true
                        return@init
                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        val onclick = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                            val date = LocalDate.of(year, month, dayOfMonth)
                            val time = LocalTime.of(hour, minute)
                            openTopSnackbar(
                                getString(
                                    R.string.set_date_s_time_s,
                                    date.format(dateFormat),
                                    time.format(timeFormat)
                                )
                            )
                            dateTimeSelection = DateTimeSelection(date, time)
                        }

                        val timepickerDialog = TimePickerDialog(
                            requireContext(),
                            onclick,
                            12,
                            0,
                            true
                        )
                        requireActivity().runOnUiThread {
                            timepickerDialog.show()
                        }
                    }
        }
        _binding.calendar.minDate = tomorrowInMillis
    }

    private fun getItemsFromArgs() {
        arguments?.let {
            val args = FinishArgs.fromBundle(bundle=it)
            items = args.cartItems
            Log.d("Finish", "Transported over items: $items")
        } ?: run {
            Log.e("Finish", "Finish fragment did not receive cart items")
        }
    }

    private fun shareCartAsCSV() {
        if (!this::_dateTimeSelection.isInitialized) {
            openErrorSnackbar(R.string.date_s_time_s_required)
            return
        }


        val config = Configuration(requireContext().resources.configuration)
        config.setLocale(Locale.US)
        val usContext = requireContext().createConfigurationContext(config)
        usContext.resources.let { resources ->
            val mappedValues =
                items.groupBy {
                    it.hashCode()
                }.mapValues { item ->
                    "${resources.getStringArray(R.array.add_item_dropdown_options)[item.value[0].type.itemIdx]}, ${item.value[0].width}, ${item.value[0].height}, ${item.value.size}, " + "%.2f\$".format(item.value[0].price * item.value.size)
                }.values.joinToString("\n")

            val shippingDateTime = resources.getString(R.string.exported_date_s_time_s, dateTimeSelection.date.format(dateFormat), dateTimeSelection.time.format(timeFormat))
            val csv = "Name,Width,Height,Amount,Price\n$mappedValues\n,,,,${resources.getString(R.string.total, total)}\n,,,,$shippingDateTime"

            val outputDir = requireContext().cacheDir // context being the Activity pointer
            val outputFile = File.createTempFile("furniture_list_", ".csv", outputDir)
            outputDir.mkdirs()
            requireContext().openFileOutput(outputFile.name, Context.MODE_PRIVATE).use {
                outputFile.writeText(csv)
            }
            val fileUri = FileProvider.getUriForFile(requireContext(), "com.elad.hit.mooover.fileProvider", outputFile)
            requireContext().grantUriPermission("com.elad.hit.mooover.fileProvider", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    //
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                data = fileUri
                type = requireContext().contentResolver.getType(fileUri)
                putExtra(Intent.EXTRA_STREAM, fileUri)
            }
            startActivity(Intent.createChooser(shareIntent, "Share as CSV"))

        }
    }
}