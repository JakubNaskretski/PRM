package com.example.prm1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.prm1.fragments.ARG_EDIT_ID
import com.example.prm1.fragments.DisplayFragment
import com.example.prm1.fragments.EditFragment
import com.example.prm1.fragments.IconSelectionFragment
import com.example.prm1.fragments.ListFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), Navigable {

    private lateinit var listFragment: ListFragment
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var btnLogOut: Button
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        if (user == null) {
            var intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent);
            finish();
        } else {
            listFragment = ListFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.container, listFragment, listFragment.javaClass.name)
                .commit()
        }

        findViewById<Button?>(R.id.btnLogOut).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            var intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent);
            finish();
            Toast.makeText(
                baseContext,
                "You have been logged out successfully",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    override fun navigate(to: Navigable.Destination, id: Long?) {
        supportFragmentManager.beginTransaction().apply {
            when (to) {
                Navigable.Destination.List -> {
                    replace(
                        R.id.container,
                        ListFragment::class.java,
                        Bundle().apply { putLong(ARG_EDIT_ID, id ?: -1L) },
                        ListFragment::class.java.name
                    )

                    addToBackStack(EditFragment::class.java.name)
                }

                Navigable.Destination.Add -> {
                    replace(
                        R.id.container,
                        EditFragment::class.java,
                        Bundle().apply { putLong(ARG_EDIT_ID, id ?: -1L) },
                        EditFragment::class.java.name
                    )
                    addToBackStack(EditFragment::class.java.name)
                }

                Navigable.Destination.Edit -> {
                    replace(
                        R.id.container,
                        EditFragment::class.java,
                        Bundle().apply { putLong(ARG_EDIT_ID, id ?: -1L) },
                        EditFragment::class.java.name
                    )
                    addToBackStack(EditFragment::class.java.name)
                }

                Navigable.Destination.Display -> {
                    replace(
                        R.id.container,
                        DisplayFragment::class.java,
                        Bundle().apply { putLong(ARG_EDIT_ID, id ?: -1L) },
                        DisplayFragment::class.java.name
                    )
                    addToBackStack(DisplayFragment::class.java.name)
                }

                Navigable.Destination.IconSelection -> {
                    replace(
                        R.id.container,
                        IconSelectionFragment::class.java,
                        Bundle().apply { putLong(ARG_EDIT_ID, id ?: -1L) },
                        IconSelectionFragment::class.java.name
                    )
                    addToBackStack(IconSelectionFragment::class.java.name)
                }

            }
        }.commit()
    }

}