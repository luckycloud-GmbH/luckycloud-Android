package com.seafile.seadroid2.pdfviewer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import com.seafile.seadroid2.R

class RadioOptionAdapter(
    context: Context,
    private val options: Array<String>,
    private val selectedPosition: Int
) : ArrayAdapter<String>(context, R.layout.item_radio_custom, options) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_radio_custom, parent, false)

        val radioButton = view.findViewById<RadioButton>(R.id.radioButton)
        val textView = view.findViewById<TextView>(R.id.tvOptionText)

        textView.text = options[position]
        radioButton.isChecked = (position == selectedPosition)

        return view
    }
}