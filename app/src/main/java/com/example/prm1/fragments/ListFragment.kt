package com.example.prm1.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prm1.Navigable
import com.example.prm1.R
import com.example.prm1.adapters.SwipeToRemove
import com.example.prm1.adapters.TaskDbObj
import com.example.prm1.adapters.TasksAdapter
import com.example.prm1.data.TaskDatabase
import com.example.prm1.data.model.TaskEntity
import com.example.prm1.databinding.FragmentListBinding
import com.example.prm1.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.concurrent.thread

class ListFragment : Fragment() {

    var tasks: MutableList<Task> = mutableListOf<Task>()

    private lateinit var binding: FragmentListBinding
    private var adapter: TasksAdapter? = null
    private lateinit var dbReference: DatabaseReference
    private lateinit var userId: String
    private var storage = Firebase.storage("gs://prm-project-fae69.appspot.com")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        userId = user!!.uid

        dbReference =
            FirebaseDatabase.getInstance("https://prm-project-fae69-default-rtdb.europe-west1.firebasedatabase.app/").reference.child(
                "tasks"
            )
                .child(userId)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tasks = mutableListOf<Task>();
//                tasksCounter = 0;
                for (tasksSnapshot in dataSnapshot.children) {
                    var tmpTask = tasksSnapshot.getValue(TaskDbObj::class.java)
                    if (tmpTask != null) {
                        tasks.add(
                            Task(
                                dbHash = tasksSnapshot.key,
                                id = tmpTask.id,
                                name = tmpTask.name,
                                subTasks = tmpTask.subTasks,
                                resId = resources.getIdentifier(
                                    tmpTask.resName.toString(),
                                    "drawable",
                                    requireContext().packageName
                                ),
                            )
                        )
                    }
                }
//                tasksCounter = tasks.size.toLong()
                requireActivity().runOnUiThread {
                    adapter?.replace(tasks)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        dbReference.addValueEventListener(postListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentListBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TasksAdapter().apply {
            onItemClick = {
                (activity as? Navigable)?.navigate(Navigable.Destination.Display, it)
            }
            onLongClick = {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.delPopUpTitle)
                builder.setMessage(R.string.delPopUpTextMessage)

                builder.setPositiveButton(R.string.yes) { _, _ ->
                    adapter?.removeItem(it)?.let {
                        thread {
                            deleteFromDB(it)
                        }
                    }
                }

                builder.setNegativeButton(R.string.no) { _, _ ->
                }
                builder.create().show()
            }
        }
        loadData()

        binding.recyclerView.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(requireContext())
            ItemTouchHelper(
                SwipeToRemove {
                    adapter?.removeItem(it)?.let {
                        thread {
                            deleteFromDB(it)
                        }
                    }
                }
            ).attachToRecyclerView(it)
        }

        binding.btAdd.setOnClickListener {
            (activity as? Navigable)?.navigate(Navigable.Destination.Add)
        }

        binding.btSort.setOnClickListener {
            adapter?.sort()
        }
    }

    private fun deleteFromDB(task: Task) {
        var storageRef = storage.reference.child(userId+"/"+ task!!.id+"/")
        storageRef.delete()
        dbReference.child(task.dbHash!!).removeValue()
    }

    fun loadData() = thread {
        dbReference.get().addOnSuccessListener {
            tasks = mutableListOf<Task>()
            for (tasksSnapshot in it.children) {
                var tmpTask = tasksSnapshot.getValue(TaskDbObj::class.java)
                if (tmpTask != null) {
                    tasks.add(
                        Task(
                            dbHash = tasksSnapshot.key,
                            id = tmpTask.id,
                            name = tmpTask.name,
                            subTasks = tmpTask.subTasks,
                            resId = resources.getIdentifier(
                                tmpTask.resName.toString(),
                                "drawable",
                                requireContext().packageName
                            ),
                        )
                    )
                }
            }
//        tasksCounter = tasks.size.toLong()
        }.addOnFailureListener {

        }
        requireActivity().runOnUiThread {
            adapter?.replace(tasks)
        }
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}