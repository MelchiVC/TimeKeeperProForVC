package com.example.opsc_poe_login
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.opsc_poe_login.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class Report : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var categorySpinner: Spinner
    private lateinit var spinnerTask: Spinner
    private lateinit var editTextReport: EditText
    private lateinit var buttonSubmit: Button
    private var selectedMinHours: String = ""
    private lateinit var hoursSpinner: Spinner
    private lateinit var selectedCategory: String
    private lateinit var taskName: String
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var logOutbtn: ImageButton
    private val hoursArray: Array<String> = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        logOutbtn = findViewById(R.id.imageButton)
        // Initialize Firebase instances
        // Category Selection Spinner
        categorySpinner = findViewById(R.id.spinnerCategory)
        val categoryAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        getUserCategories { categories ->
            categoryAdapter.clear()
            categoryAdapter.addAll(categories)
            categoryAdapter.notifyDataSetChanged()
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = parent?.getItemAtPosition(position) as String
                getUserTasks(selectedCategory) { tasks ->
                    val taskAdapter: ArrayAdapter<String> = ArrayAdapter(this@Report, android.R.layout.simple_spinner_item, tasks)
                    taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerTask.adapter = taskAdapter
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case when no category is selected
            }
        }

        // Task Selection Spinner
        spinnerTask = findViewById(R.id.spinnerTask)

        // Time Selection Spinner
        val hoursAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, hoursArray)
        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hoursSpinner = findViewById(R.id.Minspinner)
        hoursSpinner.adapter = hoursAdapter

        hoursSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMinHours = hoursArray[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedMinHours = ""
            }
        }

        buttonSubmit = findViewById(R.id.buttonSubmit)
        buttonSubmit.setOnClickListener {
            val selectedTask: String = spinnerTask.selectedItem as String
            val selectedHours: String = hoursSpinner.selectedItem as String
            updateHoursWorkedForTask(selectedCategory, selectedTask, selectedHours)
        }
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
                    val intent = Intent(this, Statistics::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
        logOutbtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun getUserCategories(callback: (List<String>) -> Unit) {
        val currentUser: FirebaseUser? = auth.currentUser
        val userId: String? = currentUser?.uid
        if (userId != null) {
            val categoriesRef: DatabaseReference = database.getReference("users/$userId/categories")
            categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val categories: MutableList<String> = mutableListOf()
                    for (categorySnapshot in dataSnapshot.children) {
                        val category: String = categorySnapshot.key ?: ""
                        categories.add(category)
                    }
                    callback(categories)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error
                }
            })
        }
    }

    private fun getUserTasks(category: String, callback: (List<String>) -> Unit) {
        val currentUser: FirebaseUser? = auth.currentUser
        val userId: String? = currentUser?.uid
        if (userId != null) {
            val tasksRef: DatabaseReference = database.getReference("users/$userId/categories/$category/tasks")
            tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val tasks: MutableList<String> = mutableListOf()
                    for (taskSnapshot in dataSnapshot.children) {
                        val taskName: String = taskSnapshot.child("name").value as String
                        tasks.add(taskName)
                    }
                    callback(tasks)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error
                }
            })
        }
    }

    private fun updateHoursWorkedForTask(category: String, taskName: String, hoursWorked: String) {
        val currentUser: FirebaseUser? = auth.currentUser
        val userId: String? = currentUser?.uid
        if (userId != null) {
            val tasksRef: DatabaseReference = database.getReference("users/$userId/categories/$category/tasks")
            val taskQuery: Query = tasksRef.orderByChild("name").equalTo(taskName)
            taskQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (taskSnapshot in dataSnapshot.children) {
                        val taskKey: String = taskSnapshot.key ?: ""
                        val currentHoursWorked: String? = taskSnapshot.child("hoursWorked").getValue(String::class.java)
                        val updatedHoursWorked: String = if (currentHoursWorked != null) {
                            (currentHoursWorked.toInt() + hoursWorked.toInt()).toString()
                        } else {
                            hoursWorked
                        }
                        tasksRef.child(taskKey).child("hoursWorked").setValue(updatedHoursWorked)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error
                }
            })
        }
    }
}
