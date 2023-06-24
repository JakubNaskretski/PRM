package com.example.prm1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class RegistrationActivity : AppCompatActivity() {

    private lateinit var editTextEmail : TextInputEditText
    private lateinit var editTextPassword : TextInputEditText
    private lateinit var btnReg : Button
    private lateinit var auth : FirebaseAuth
    private lateinit var loginNavigate : TextView

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            var intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent);
            finish();
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration);
        auth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.inputEmail);
        editTextPassword = findViewById(R.id.inputPassword);
        btnReg = findViewById(R.id.btnRegister);
        loginNavigate = findViewById(R.id.textAlreadyRegistered);

        loginNavigate.setOnClickListener {
            var intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent);
            finish();
        }

        btnReg.setOnClickListener {
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

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            baseContext,
                            "Registration successfully",
                            Toast.LENGTH_SHORT,
                        ).show()

                        Handler(Looper.getMainLooper()).postDelayed({
                            var intent = Intent(applicationContext, LoginActivity::class.java)
                            startActivity(intent);
                            finish();
                        }, 1000)

                    } else {
                        Toast.makeText(
                            baseContext,
                            "Registration failed: "+ task.exception!!.message,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }
}