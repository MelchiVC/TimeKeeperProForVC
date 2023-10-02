package com.example.opsc_poe_login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Intent
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        var user_name = findViewById(R.id.User_Name) as EditText
        var password = findViewById(R.id.txt_password) as EditText
        val regtextView = findViewById<TextView>(R.id.register)
        val btn_submit = findViewById<Button>(R.id.btn_submit)

        //OnClick to check password and username and log the user in
        btn_submit.setOnClickListener {
            val username = user_name.text.toString()
            val password = password.text.toString()

            auth.signInWithEmailAndPassword(username, password)
                .addOnSuccessListener {
                    // Login successful, navigate to the main activity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { exception ->
                    // Invalid credentials or other error occurred, show error message
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
        }

        regtextView.setOnClickListener{
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }



    }

    //Function that has the logic area to check user password and username is valid
    private fun isValidUser(username: String, password: String): Boolean {

        return username == "admin" && password == "password"
    }
}