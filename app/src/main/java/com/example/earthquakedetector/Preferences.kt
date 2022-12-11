package com.example.earthquakedetector

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


var interval:Int? = null

class Preferences : AppCompatActivity(), AdapterView.OnItemSelectedListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)
        val magnitudeField = findViewById<EditText>(R.id.minMagnitude)
        val minmagnitude = magnitudeField.text
        val rangeField = findViewById<EditText>(R.id.maxRange)
        val maxrange = rangeField.text
        val savebutton = findViewById<Button>(R.id.saveButton)
        //Binding for save button element when the user presses, it saves the Preferences
        savebutton.setOnClickListener{
            intent = Intent(this,MainActivity::class.java)
            intent.putExtra("minMagnitude",minmagnitude)
            intent.putExtra("maxRange",maxrange)
//            intent.putExtra("minInterval", interval);
            startActivity(intent)
        }
        val intervalspinner: Spinner = findViewById(R.id.intervalSpinner)  // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.intervalVal,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            intervalspinner.adapter = adapter
        }
        interval = intervalspinner.selectedItem as Int?
    }

    fun getrange(): Double? {
        return magnitude
    }

    fun getInterval(): Int? {
        return interval
    }
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        TODO("Not yet implemented")
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}