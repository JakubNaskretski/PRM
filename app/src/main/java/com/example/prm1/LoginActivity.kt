package com.example.prm1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextEmail : TextInputEditText
    private lateinit var editTextPassword : TextInputEditText
    private lateinit var btnLogin : Button
    private lateinit var auth : FirebaseAuth
    private lateinit var registerNavigate : TextView

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            var intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent);
            finish();
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.inputEmail);
        editTextPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        registerNavigate = findViewById(R.id.textNotRegistered);

        registerNavigate.setOnClickListener {
            var intent = Intent(applicationContext, RegistrationActivity::class.java)
            startActivity(intent);
            finish();
        }

        btnLogin.setOnClickListener {
            var email: String = editTextEmail.text.toString();
            var password: String = editTextPassword.text.toString();

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Logged successfully", Toast.LENGTH_SHORT).show();
                        var intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(
                            this,
                            "Authentication failed: "+ task.exception!!.message,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }

        }
    }
}