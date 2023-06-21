package com.example.prm1.fragments

import android.content.Intent
import android.os.Bundle
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
import com.example.prm1.adapters.TasksAdapter
import com.example.prm1.data.TaskDatabase
import com.example.prm1.databinding.FragmentListBinding
import com.example.prm1.model.Task
import kotlin.concurrent.thread

class ListFragment : Fragment() {

    private lateinit var binding: FragmentListBinding
    private var adapter : TasksAdapter? = null
    private lateinit var db : TaskDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = TaskDatabase.open(requireContext())
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
                (activity as? Navigable)?.navigate(Navigable.Destination.Edit, it)
            }
            onLongClick = {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.delPopUpTitle)
                builder.setMessage(R.string.delPopUpTextMessage)

                builder.setPositiveButton(R.string.yes) { _, _ ->
                    adapter?.removeItem(it)?.let {
                        thread {
                            db.tasks.remove(it.id)
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
                            db.tasks.remove(it.id)
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

    fun loadData() = thread {
        val tasks = db.tasks.getAll().map {
                entity -> Task(
            entity.id,
            entity.name,
            entity.subTasks.split("\n"),
            resources.getIdentifier(entity.icon, "drawable", requireContext().packageName)
            )
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
        db.close()
        super.onDestroy()
    }

}