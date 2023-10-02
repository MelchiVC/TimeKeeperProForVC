package com.example.opsc_poe_login
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Random

class DailyGoal : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var logOutbtn: ImageButton
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_goal)

        listView = findViewById(R.id.listView)
        logOutbtn = findViewById(R.id.imageButton)
        auth = FirebaseAuth.getInstance()
        taskAdapter = TaskAdapter(this, mutableListOf())
        listView.adapter = taskAdapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val database = FirebaseDatabase.getInstance()
        val tasksRef = database.getReference("users").child(userId.toString()).child("categories")

        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tasksList = mutableListOf<Task>()

                for (categorySnapshot in dataSnapshot.children) {
                    val categoryName = categorySnapshot.key as String

                    for (taskSnapshot in categorySnapshot.child("tasks").children) {
                        val taskId = taskSnapshot.key as String
                        val activityName = taskSnapshot.child("name").getValue(String::class.java)
                        val date = taskSnapshot.child("date").getValue(String::class.java)

                        val task = Task(taskId, activityName, date, categoryName)
                        tasksList.add(task)
                    }
                }

                taskAdapter.clear()
                taskAdapter.addAll(tasksList)
                taskAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur while retrieving data from Firebase
            }
        })
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
                R.id.menu_statistics -> {
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
            finish()
        }
    }

    data class Task(
        val id: String,
        val name: String?,
        val date: String?,
        val category: String?,
        val taskBarValue: Int = Random().nextInt(101)
    )

    class TaskAdapter(context: Context, tasksList: MutableList<Task>) :
        ArrayAdapter<Task>(context, R.layout.list_item_tasks, tasksList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            val viewHolder: ViewHolder

            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(R.layout.list_item_tasks, parent, false)
                viewHolder = ViewHolder(itemView)
                itemView.tag = viewHolder
            } else {
                viewHolder = itemView.tag as ViewHolder
            }

            val task = getItem(position)
            viewHolder.bind(task)

            return itemView!!
        }

        private class ViewHolder(view: View) {
            private val nameTextView: TextView = view.findViewById(R.id.textViewActivityName)
            private val dateTextView: TextView = view.findViewById(R.id.textViewDate)
            private val categoryTextView: TextView = view.findViewById(R.id.textViewCategory)
            private val taskBarView: View = view.findViewById(R.id.taskBarLayout)

            fun bind(task: Task?) {
                nameTextView.text = task?.name
                dateTextView.text = task?.date
                categoryTextView.text = task?.category

                // Set task bar color based on task bar value
                val taskBarColor = calculateTaskBarColor(task?.taskBarValue)
                taskBarView.setBackgroundColor(taskBarColor)
            }

            private fun calculateTaskBarColor(value: Int?): Int {
                // Calculate color based on value
                val red = (255 * (100 - value!!)) / 100
                val green = (255 * value) / 100
                val blue = 0
                return (255 shl 24) + (red shl 16) + (green shl 8) + blue
            }
        }
    }
}

