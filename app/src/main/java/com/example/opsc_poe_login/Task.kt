package com.example.opsc_poe_login

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.grpc.Context.Storage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class Task : AppCompatActivity() {
    private lateinit var activityName: TextView
    private lateinit var activityDescription: TextView
    private lateinit var selectedDate: Calendar
    private lateinit var selectedTime: Calendar
    private lateinit var selectedDateTextView: TextView
    private lateinit var selectedStartTime: TextView
    private lateinit var selectedEndTime: TextView
    private lateinit var brtextView: TextView
    private lateinit var selectedCategory: TextView
    private lateinit var logOutbtn: ImageButton
    private lateinit var dailyGoal: Button
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var imageView: ImageView
    private lateinit var buttonI: Button
    private val pickImage = 100
    private var imageUri: Uri? = null
    private val userDataArray: UserData = UserData()
    private val hoursArray: Array<String> = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    private var selectedMinHours: String = ""
    private var selectedMaxHours: String = ""
    private lateinit var database: DatabaseReference
    private lateinit var storage : StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var CatSpinner: Spinner
    var selectedCat: String=""

    companion object {
        val IMAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        loadUserCategories()
        // Binding variables to their respective components in the view
        selectedDateTextView = findViewById(R.id.txt_selectedDate)
        selectedStartTime = findViewById(R.id.txt_startTime)
        selectedEndTime = findViewById(R.id.txt_endTime)
        activityName = findViewById(R.id.activityNameEditText)
        activityDescription = findViewById(R.id.descriptionEditText)
        brtextView = findViewById(R.id.txt_Details)
        imageView = findViewById(R.id.photoImageView)
        buttonI = findViewById(R.id.addPhotoButton)
        logOutbtn = findViewById(R.id.imageButton)
        CatSpinner = findViewById(R.id.CategoriesSpinner)
        val dateButton: Button = findViewById(R.id.selectDateButton)
        val saveButton: Button = findViewById(R.id.saveEntryButton)
        val sTimeButton: Button = findViewById(R.id.selectStartTimeButton)
        val eTimeButton: Button = findViewById(R.id.selectEndTimeButton)
        val minSpinner: Spinner = findViewById(R.id.Minspinner)
        val maxSpinner: Spinner = findViewById(R.id.Maxspinner)
        val viewTask: Button = findViewById(R.id.viewTasks)
        val hoursAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, hoursArray).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Spinner functionality
        minSpinner.adapter = hoursAdapter
        maxSpinner.adapter = hoursAdapter
        minSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMinHours = hoursArray[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedMinHours = ""
            }
        }
        maxSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMaxHours = hoursArray[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedMaxHours = ""
            }
        }
        CatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCat = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where no item is selected
            }
        }



        // Onclick events for buttons
        dateButton.setOnClickListener {
            showDatePickerDialog()
        }
        sTimeButton.setOnClickListener {
            showTimePickerDialog()
        }
        eTimeButton.setOnClickListener {
            showTimePickerDialog2()
        }
        saveButton.setOnClickListener {
            saveUserData(
                activityName.text.toString(),
                activityDescription.text.toString(),
                selectedStartTime.text.toString(),
                selectedEndTime.text.toString(),
                selectedDateTextView.text.toString(),
                selectedCat,
                selectedMinHours,
                selectedMaxHours,
                hoursWorked =""
            )
//            displayUserData()
        }

        // OnClick that directs the user to a list of tasks
        viewTask.setOnClickListener {
            val intent = Intent(this, DailyGoal::class.java)
            startActivity(intent)
        }

        // Image button to call image function to get the user's image from the gallery
        buttonI.setOnClickListener {
            selectImage()
        }

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
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Function to select image from local storage
    private fun selectImage() {
        val selectIntent = Intent()
        selectIntent.type="image/*"
        selectIntent.action= Intent.ACTION_GET_CONTENT
        startActivityForResult(selectIntent,pickImage)
    }
    // Function used after successfully pick image and set image uri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage && data != null) {
            imageUri= data?.data!!
            imageView.setImageURI(imageUri)
        }
    }

    // Function for date picker
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                // Save the selected date
                selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                // Format the selected date as per your requirements
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)

                // Display the selected date
                selectedDateTextView.text = formattedDate
            }, year, month, day)
        datePickerDialog.show()
    }

    // Function for start Time picker
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                // Save the selected time
                selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedTime.set(Calendar.MINUTE, selectedMinute)

                // Format the selected time as per your requirements
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormat.format(selectedTime.time)

                // Display the selected time
                selectedStartTime.text = formattedTime
            }, hour, minute, false)

        timePickerDialog.show()
    }

    // Function for end Time picker
    private fun showTimePickerDialog2() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                // Save the selected time
                selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedTime.set(Calendar.MINUTE, selectedMinute)

                // Format the selected time as per your requirements
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = timeFormat.format(selectedTime.time)

                // Display the selected time
                selectedEndTime.text = formattedTime
            }, hour, minute, false)

        timePickerDialog.show()
    }

    // Function to save user data to the data array in user data
    private fun saveUserData(
        name: String,
        description: String,
        startTime: String,
        endTime: String,
        date: String,
        category: String,
        minHours: String,
        maxHours: String,
        hoursWorked: String = "0"
    ) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val taskData = TaskData(name, description, startTime, endTime, date, category, minHours, maxHours, hoursWorked)
            val categoryRef = database.child("users/$userId/categories/$category")
            val taskRef = categoryRef.child("tasks").child(name)
            taskRef.child("name").setValue(name)
            taskRef.child("description").setValue(description)
            taskRef.child("startTime").setValue(startTime)
            taskRef.child("endTime").setValue(endTime)
            taskRef.child("date").setValue(date)
            taskRef.child("category").setValue(category)
            taskRef.child("minHours").setValue(minHours)
            taskRef.child("maxHours").setValue(maxHours)
            taskRef.child("hoursWorked").setValue(hoursWorked)
            storage= FirebaseStorage.getInstance().getReference("users/$userId")
            storage.putFile(imageUri!!).addOnSuccessListener {
                storage.downloadUrl.addOnSuccessListener { Uri->
                    val map= HashMap<String, Any>()
                    map["pic"]= Uri.toString()
                    taskRef.child("imageUri").setValue(map)
                }
                }
                .addOnSuccessListener {
                    Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun populateCategorySpinner(categories: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        CatSpinner.adapter = adapter
    }
    private fun loadUserCategories() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val categoriesRef = database.child("users/$userId/categories")
            categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categories = ArrayList<String>()
                    for (categorySnapshot in snapshot.children) {
                        val categoryName = categorySnapshot.key
                        if (categoryName != null) {
                            categories.add(categoryName)
                        }
                    }
                    populateCategorySpinner(categories)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        }
    }

    // Function to display the user data for the task that was created by the user
    @SuppressLint("SuspiciousIndentation")
    private fun displayUserData() {
        val stringBuilder = StringBuilder()

        stringBuilder.append("Name: ${activityName.text.toString()}\n\n")
        stringBuilder.append("Description:\n ${activityDescription.text.toString()}\n\n")
        stringBuilder.append("Date: ${selectedDateTextView.text.toString()}\n\n")
        stringBuilder.append("Start Time: ${selectedStartTime.text.toString()}\n\n")
        stringBuilder.append("End Time: ${selectedEndTime.text.toString()}\n\n")
        stringBuilder.append("Category: ${selectedCategory.text.toString()}\n\n")
        stringBuilder.append("Min Hours Goal: $selectedMinHours\n\n")
        stringBuilder.append("Max Hours Goal: $selectedMaxHours\n\n")
        stringBuilder.append("<img src='" + imageUri + "' visible='true' runat='server' style='width: 75px; height: 41px' />");
        brtextView.text = stringBuilder.toString()
    }
}