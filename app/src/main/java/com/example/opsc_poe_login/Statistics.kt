package com.example.opsc_poe_login
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle

import android.widget.Button

import android.widget.ImageButton

import android.widget.TextView

import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.github.mikephil.charting.charts.BarChart

import com.github.mikephil.charting.components.XAxis

import com.github.mikephil.charting.components.YAxis

import com.github.mikephil.charting.data.BarData

import com.github.mikephil.charting.data.BarDataSet

import com.github.mikephil.charting.data.BarEntry

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import com.github.mikephil.charting.utils.ColorTemplate

import com.google.android.material.bottomnavigation.BottomNavigationView

import java.text.SimpleDateFormat

import java.util.*





class Statistics : AppCompatActivity() {

    //variables

    private lateinit var txtSelectedDate: TextView

    private lateinit var btnSelectDate: Calendar

    private lateinit var btnGenerateGraph: Button

    private lateinit var btnReset: Button

    private lateinit var barChart: BarChart

    private lateinit var logOutbtn: ImageButton

    private lateinit var bottomNavigationView: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_statistics)



        barChart = findViewById(R.id.bar_chart)

        btnGenerateGraph = findViewById(R.id.btnGenerateGraph)

        btnReset = findViewById(R.id.btnReset)

        logOutbtn = findViewById(R.id.imageButton)

        barChart = findViewById(R.id.bar_chart)

        val dateButton2: Button = findViewById(R.id.btnSelectDate)






        dateButton2.setOnClickListener {
            showDatePickerDialog()
        }



        btnGenerateGraph.setOnClickListener {

            // Handle the Generate Graph button click here
            setupBarChart()
            generateGraph()
            Toast.makeText(this, "Generate Graph button clicked", Toast.LENGTH_SHORT).show()

        }



        btnReset.setOnClickListener {

            // Handle the Reset button click here

            Toast.makeText(this, "Reset button clicked", Toast.LENGTH_SHORT).show()

        }

    }

    private fun setupBarChart() {

        barChart.setDrawBarShadow(false)

        barChart.setDrawValueAboveBar(true)

        barChart.description.isEnabled = false


        val xAxis = barChart.xAxis

        xAxis.position = XAxis.XAxisPosition.BOTTOM

        xAxis.setDrawGridLines(false)

        xAxis.valueFormatter = IndexAxisValueFormatter(getXAxisValues())


        val leftAxis = barChart.axisLeft

        leftAxis.axisMinimum = 0f


        val rightAxis: YAxis = barChart.axisRight

        rightAxis.isEnabled = false

    }


    //logic to generate bar graph colours

    private fun generateGraph() {

        val entries = getBarEntries()

        val dataSet = BarDataSet(entries, "Total Hours Worked")

        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val data = BarData(dataSet)

        barChart.data = data

        barChart.invalidate()

    }


    private fun getXAxisValues(): List<String> {

        // Headings/X Axis values for the graph.. dummy data

        return listOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5")

    }


    private fun getBarEntries(): List<BarEntry> {

        // Dummy data for bar graph

        val entries = mutableListOf<BarEntry>()

        entries.add(BarEntry(0f, 5f))

        entries.add(BarEntry(1f, 8f))

        entries.add(BarEntry(2f, 3f))

        entries.add(BarEntry(3f, 6f))

        entries.add(BarEntry(4f, 4f))

        return entries

    }


    // Function for date picker

    private fun showDatePickerDialog() {

        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)

        val month = calendar.get(Calendar.MONTH)

        val day = calendar.get(Calendar.DAY_OF_MONTH)


        val datePickerDialog = DatePickerDialog(
            this,

            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->

                // Save the selected date

                btnSelectDate = Calendar.getInstance()

                btnSelectDate.set(selectedYear, selectedMonth, selectedDay)


                // Format the selected date as per your requirements

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val formattedDate = dateFormat.format(btnSelectDate.time)


                // Display the selected date

                txtSelectedDate.text = formattedDate

            }, year, month, day

        )

        datePickerDialog.show()


        // Logic for UI navigation as well as logout button functionality

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {

                R.id.menu_home -> {

                    val intent = Intent(this, MainActivity::class.java)

                    startActivity(intent)

                    true

                }

                R.id.menu_task -> {

                    val intent = Intent(this, Task::class.java)

                    startActivity(intent)

                    true

                }

                R.id.menu_list -> {

                    val intent = Intent(this, DailyGoal::class.java)

                    startActivity(intent)

                    true

                }

                else -> false

            }

        }

        logOutbtn.setOnClickListener {

            val intent = Intent(this, Login::class.java)

            startActivity(intent)

            finish()

        }

    }
}


