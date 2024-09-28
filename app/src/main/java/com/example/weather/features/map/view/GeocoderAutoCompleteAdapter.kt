package com.example.weather.features.map.view

import android.content.Context
import android.location.Geocoder
import android.widget.ArrayAdapter
import android.widget.Filter

class GeocoderAutoCompleteAdapter(
    context: Context,
    private val geocoder: Geocoder
) : ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line) {

    private val suggestions = mutableListOf<String>()

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()

                if (constraint != null) {
                    try {
                        val addressList = geocoder.getFromLocationName(constraint.toString(), 5)
                        suggestions.clear()
                        if (addressList != null) {
                            for (address in addressList) {
                                suggestions.add(address.getAddressLine(0))
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    results.values = suggestions
                    results.count = suggestions.size
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    clear()
                    addAll(results.values as List<String>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}
