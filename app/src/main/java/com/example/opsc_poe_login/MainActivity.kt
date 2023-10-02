package com.example.opsc_poe_login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity() {

    // Initialize Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var logOutbtn: ImageButton
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logOutbtn = findViewById(R.id.imageButton)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        val userRef = database.reference.child("users")
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userDataRef = userRef.child(userId.toString())
        val categoryRef = userDataRef.child("categories")

        val btn_submit = findViewById<Button>(R.id.addCategoryButton)
        val user_Category = findViewById<EditText>(R.id.categoryEditText)

        // Functionality to direct the user to a page to create a task based on the category they select
        val intent = Intent(this, Task::class.java)
        val listView = findViewById<ListView>(R.id.listView)
        val adapter = ArrayAdapter<String>(this, R.layout.list_item)
        listView.adapter = adapter
        listView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                startActivity(intent)
                finish()
                category = adapter.getItem(position).toString()
            }
        }

        // OnClick to save the user's new category to the Firebase Realtime Database
        btn_submit.setOnClickListener {
            val userCat = user_Category.text.toString()
            val currentUser = auth.currentUser
            if (userId != null) {
                categoryRef.child(userCat).setValue(userCat)
            }
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
        }

        // Read user's categories from Firebase and update the adapter
        categoryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCategories = mutableListOf<String>()
                for (categorySnapshot in snapshot.children) {
                    val userCategory = categorySnapshot.key as String
                    userCategories.add(userCategory)
                }
                adapter.clear()
                adapter.addAll(userCategories)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}


