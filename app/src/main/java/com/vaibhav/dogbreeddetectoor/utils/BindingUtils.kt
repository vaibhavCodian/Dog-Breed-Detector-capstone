package com.vaibhav.dogbreeddetectoor.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("getDataWithoutNull")
fun TextView.setDataWithoutNull(data: String) {
    text = if (data == "null") ""
    else data
}