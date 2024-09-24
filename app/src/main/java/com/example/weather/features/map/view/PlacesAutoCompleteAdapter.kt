package com.example.weather.features.map.view

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class PlacesAutoCompleteAdapter(context: Context, private val placesClient: PlacesClient) :
    ArrayAdapter<AutocompletePrediction>(context, android.R.layout.simple_list_item_1, ArrayList()) {
    private val predictions: MutableList<AutocompletePrediction> = mutableListOf()

    override fun getCount(): Int {
        return predictions.size
    }

    override fun getItem(position: Int): AutocompletePrediction? {
        return predictions[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        val prediction = getItem(position)
        textView.text = prediction?.getPrimaryText(null)?.toString()
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val result = FilterResults()
                if (constraint != null) {
                    val predictionsList = getAutocomplete(constraint.toString())
                    result.values = predictionsList
                    result.count = predictionsList.size
                }
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    predictions.clear()
                    predictions.addAll(results.values as List<AutocompletePrediction>)
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    private fun getAutocomplete(query: String): List<AutocompletePrediction> {
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(token)
            .build()

        val task = placesClient.findAutocompletePredictions(request)
        val resultList = mutableListOf<AutocompletePrediction>()

        try {
            val response = Tasks.await(task)
            resultList.addAll(response.autocompletePredictions)
        } catch (e: Exception) {
            Log.e("PlacesAutoComplete", "Error fetching autocomplete predictions", e)
        }
        return resultList
    }
}
