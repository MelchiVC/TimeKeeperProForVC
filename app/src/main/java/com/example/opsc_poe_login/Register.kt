package com.example.opsc_poe_login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var Email: EditText
    lateinit var ConfPass: EditText
    private lateinit var Pass: EditText
    private lateinit var btnsubmit: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        Email = findViewById(R.id.User_Name)
        ConfPass = findViewById(R.id.reenterPassword)
        Pass = findViewById(R.id.txt_password)
        btnsubmit = findViewById(R.id.btn_submit)



        btnsubmit.setOnClickListener {
            signUpUser()
        }
    }


    private fun signUpUser() {
        val email = Email.text.toString()
        val pass = Pass.text.toString()
        val confirmPassword = ConfPass.text.toString()

        // check pass
        if (email.isBlank() || pass.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, "Email and Password can't be blank", Toast.LENGTH_SHORT).show()

            if (pass != confirmPassword) {
                Toast.makeText(
                    this,
                    "Password and Confirm Password do not match",
                    Toast.LENGTH_SHORT
                ).show()

                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Successfully Singed Up", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Singed Up Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }
}