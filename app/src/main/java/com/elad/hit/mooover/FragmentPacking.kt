package com.elad.hit.mooover

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.elad.hit.mooover.Extensions.getInt
import com.elad.hit.mooover.Extensions.openErrorSnackbar
import com.elad.hit.mooover.Extensions.openSnackbar
import com.elad.hit.mooover.Extensions.toFloat
import com.elad.hit.mooover.Extensions.toFloatOrNull
import com.elad.hit.mooover.Extensions.toInt
import com.elad.hit.mooover.Extensions.toIntOrNull
import com.elad.hit.mooover.databinding.FragmentPackingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList


class FragmentPacking : Fragment() {
    private lateinit var dropdownAdapter: ArrayAdapter<String>
    private lateinit var _binding: FragmentPackingBinding
    private lateinit var items: ArrayList<CartItem>
    private var _isLoading = false
    private var isLoading
    get() = _isLoading
    set(isLoading) {
        if (isLoading) {
            _binding.loader.visibility = View.VISIBLE
        } else {
            _binding.loader.visibility = View.GONE
        }
        _isLoading = isLoading
    }
    private val total get() = Collections.synchronizedList(items).map { cartItem -> cartItem.price }.sum()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentPackingBinding.inflate(inflater, container, false)

        createItemSelectionOptions()
        subscribeToAutocomplete()
        subscribeToAddSubtractButtons()
        subscribeToInputsForValidation()
        subscribeToSubmitActions()
        subscribeToTruckButton()
        clearAllFields()

        items = ArrayList(getInt(R.string.add_item_max_items_allowed))

        return _binding.root
    }

    private fun subscribeToAutocomplete() {
        _binding.itemTypeAutocomplete.setOnItemClickListener { _, _, position, _ ->
            openSnackbar(getString(R.string.add_item_dropdown_selected_string, dropdownAdapter.getItem(position)))
        }
    }

    private fun subscribeToTruckButton() {
        _binding.truckButton.setOnClickListener {
            if (items.size > 0) {
                findNavController().navigate(FragmentPackingDirections.actionPackingFragmentToFinish(items.toTypedArray()))
            } else {
                openErrorSnackbar(R.string.add_item_atleast_one_cart_item_required)
            }
        }
    }

    private fun subscribeToSubmitActions() {
        _binding.addItemSubmitClearBtn.setOnClickListener {
            clearAllFields()
            clearFocus()
        }

        _binding.addItemSubmitAddBtn.setOnClickListener {
            clearFocus()

            val itemTypeString = _binding.itemTypeAutocomplete.text.toString()
            if (itemTypeString == "") {
                openErrorSnackbar(getString(R.string.add_item_item_type_required_error))
                return@setOnClickListener
            }

            addItemToCart(
                _binding.resultLayout,
                CartItem(
                    ItemType.fromIdx(dropdownAdapter.getPosition(_binding.itemTypeAutocomplete.text.toString())),
                    _binding.widthEditText.toFloat(),
                    _binding.heightEditText.toFloat()
                ),
                _binding.itemCountTextEdit.toInt()
            )
        }
    }

    private fun addItemToCart(parentLayout: LinearLayout, item: CartItem, howMany: Int) {
        isLoading = true
        GlobalScope.launch {
            for (i in 1..howMany) {
                Collections.synchronizedList(items).add(item)
                val newParent = createResultItem(item.id, resources.getStringArray(R.array.add_item_dropdown_options)[item.type.itemIdx], "${item.width}X${item.height}","%.2f\$".format(item.price))

                withContext(Dispatchers.Main) {
                    _binding.resultLayout.addView(newParent)
                }
            }


            withContext(Dispatchers.Main) {
                _binding.resultLayout.visibility = View.VISIBLE
                isLoading = false
                _binding.sumTotalTxt.text = getString(R.string.total, total)
                parentLayout.requestLayout()
            }
        }

    }

    private fun createResultItem(id: Int, titleTxt: String, descriptionTxt: String, priceTxt: String): LinearLayout {
        val parent = LinearLayout(requireContext())
        val title = TextView(requireContext())
        val description = TextView(requireContext())
        val price = TextView(requireContext())
        val button = FloatingActionButton(requireContext())

        val parentLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        parentLayoutParams.setMargins(0, 0,0,20)
        parent.layoutParams = parentLayoutParams
        parent.orientation = LinearLayout.HORIZONTAL
        parent.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
//        parent.background = AppCompatResources.getDrawable(requireContext(), R.drawable.border_top_bottom)
        parent.id = 1000 + id

        title.text = titleTxt
        title.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        title.setTextAppearance(requireContext(), R.style.TextAppearance_AppCompat_Body1)
        title.setPadding(20, 40, 20, 40)

        description.text = descriptionTxt
        description.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        description.setTextAppearance(requireContext(), R.style.TextAppearance_AppCompat_Body2)
        description.setPadding(20, 40, 20, 40)

        price.text = priceTxt
        price.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        price.setTextAppearance(requireContext(), R.style.TextAppearance_AppCompat_Body2)
        price.setPadding(20, 40, 20, 40)

        button.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.delete_icon))
        button.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        button.setPadding(20, 40, 20, 40)
        button.setOnClickListener {
            _binding.resultLayout.removeView(parent)
            removeById(id)
            _binding.sumTotalTxt.text = getString(R.string.total, total)
            if (items.size == 0) {
                _binding.resultLayout.visibility = View.GONE
            }
        }

        parent.addView(title)
        parent.addView(description)
        parent.addView(price)
        parent.addView(button)
        return parent
    }

    private fun removeById(id: Int) {
        val it = items.iterator()
        while (it.hasNext()) {
            if (it.next().id == id) {
                it.remove()
                break;
            }
        }
    }

    private fun clearAllFields() {
        _binding.widthEditText.setText(getString(R.string.add_item_width_default_value))
        _binding.heightEditText.setText(getString(R.string.add_item_height_default_value))
        _binding.itemCountTextEdit.setText(getString(R.string.add_item_item_count_default_value))

        getInt(R.string.add_item_dropdown_default_string).let { defaultDropdownVal ->
            dropdownAdapter.getItem(defaultDropdownVal).let { defaultDropdownPos ->
                _binding.itemTypeAutocomplete.setText(dropdownAdapter.getItem(defaultDropdownVal), false)
                _binding.itemTypeAutocomplete.setSelection(dropdownAdapter.getPosition(defaultDropdownPos))
                dropdownAdapter.filter.filter(null);
            }
        }
    }

    private fun clearFocus() {
        val inputManager: InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        requireActivity().currentFocus?.let {
            inputManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
        _binding.widthEditText.clearFocus()
        _binding.heightEditText.clearFocus()
        _binding.itemTypeAutocomplete.clearFocus()
        _binding.itemCountTextEdit.clearFocus()
    }

    private fun subscribeToInputsForValidation() {
        setRequiredValidation(_binding.widthTextInputLayout, _binding.widthEditText, AppCompatResources.getDrawable(requireContext(), R.drawable.width_icon))
        setRequiredValidation(_binding.heightTextInputLayout, _binding.heightEditText, AppCompatResources.getDrawable(requireContext(), R.drawable.height_icon))
    }

    private fun setRequiredValidation(layout: TextInputLayout, editText: TextInputEditText, icon: Drawable?) {
        editText.addTextChangedListener { text ->
            // let does something if num is not null and run does something if num is null
            text.toFloatOrNull()?.let {
                editText.error = null
                layout.endIconDrawable = icon
            } ?: run {
                editText.error = getString(R.string.add_item_required_error_string)
                layout.endIconDrawable = null
            }
        }
    }

    private fun subscribeToAddSubtractButtons() {
        addValueToItemCount(_binding.addItemCountButton, 1)
        addValueToItemCount(_binding.subtractItemCountButton, -1)

        _binding.itemCountTextEdit.addTextChangedListener {
            it.toIntOrNull()?.let { num ->
                if (num < 0)
                    _binding.itemCountTextEdit.setText(num.coerceAtLeast(1).toString())
            }
        }
    }

    private fun addValueToItemCount(actionButton: FloatingActionButton, valueAdded: Int) {
        actionButton.setOnClickListener {
            _binding.itemCountTextEdit.toIntOrNull()?.let { num ->
                _binding.itemCountTextEdit.setText(
                        (num + valueAdded)
                        .coerceAtLeast(1)
                        .toString()
                )
            } ?: run {
                _binding.itemCountTextEdit.setText(getString(R.string.add_item_item_count_default_value))
            }
        }
    }

    private fun createItemSelectionOptions() {
        val options = resources.getStringArray(R.array.add_item_dropdown_options)
        dropdownAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, options)
        _binding.itemTypeAutocomplete.setAdapter(dropdownAdapter)
        dropdownAdapter.getPosition(getString(R.string.add_item_dropdown_default_string)).coerceAtLeast(0).let {
            _binding.itemTypeAutocomplete.setSelection(it)
            _binding.itemTypeAutocomplete.requestLayout()
        }
    }
}