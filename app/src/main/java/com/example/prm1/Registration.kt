package com.example.prm1

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class Registration : ComponentActivity() {

    private lateinit var editTextEmail : TextInputEditText;
    private lateinit var editTextPassword : TextInputEditText;
    private lateinit var btnReg : Button;
    private lateinit var auth : FirebaseAuth;
    private lateinit var loginNavigate : TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration);
        auth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.inputEmail);
        editTextPassword = findViewById(R.id.inputPassword);
        btnReg = findViewById(R.id.btnRegister);
        loginNavigate = findViewById(R.id.textAlreadyRegistered);

        loginNavigate.setOnClickListener {
            // sghould be activity instead of this
            (this as? Navigable)?.navigate(Navigable.Destination.Login)
        }

        btnReg.setOnClickListener {
            var email: String = editTextEmail.text.toString();
            var password: String = editTextPassword.text.toString();

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            baseContext,
                            "Registration successfull",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }

        }
    }
}