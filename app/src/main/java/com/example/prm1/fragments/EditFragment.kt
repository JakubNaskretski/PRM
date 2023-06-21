package com.example.prm1.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prm1.Navigable
import com.example.prm1.R
import com.example.prm1.adapters.TaskImagesAdapter
import com.example.prm1.data.TaskDatabase
import com.example.prm1.data.model.TaskEntity
import com.example.prm1.databinding.FragmentEditBinding
import kotlin.concurrent.thread

const val ARG_EDIT_ID = "edit_id"

class EditFragment : Fragment() {

    private lateinit var binding: FragmentEditBinding
    private lateinit var adapter: TaskImagesAdapter
    private lateinit var db: TaskDatabase
    private var task : TaskEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = TaskDatabase.open(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentEditBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TaskImagesAdapter()
        val id = requireArguments().getLong(ARG_EDIT_ID, -1)
        if (id != -1L) {
            thread {
                task = db.tasks.getTask(id)
                requireActivity().runOnUiThread {

                    binding.taskName.setText(task?.name ?: "")
                    binding.subTasks.setText(task?.subTasks ?: "")

                    adapter.setSelection(task?.icon?.let {
                        resources.getIdentifier(
                            it,
                            "drawable",
                            requireContext().packageName
                        )
                    })
                }
            }
        }

        binding.images.apply {
            adapter = this@EditFragment.adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.btnShare.setOnClickListener {
            var intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"

            val sb = StringBuilder()
            sb.append(getText(R.string.task))
                .append(" ")
                .append(task?.name)
                .append("\n")
                .append(getText(R.string.subTasks))
                .append(" ")
                .append(task?.subTasks)
            val body = sb.toString()
            intent.putExtra(Intent.EXTRA_TEXT, body)
            startActivity(Intent.createChooser(intent, R.string.shareUsing.toString()))
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            val task = task?.copy(
                name = binding.taskName.text.toString(),
                subTasks = binding.subTasks.text.toString(),
                icon = resources.getResourceEntryName(adapter.selectedIdRes)
            ) ?: TaskEntity(
                name = binding.taskName.text.toString(),
                subTasks = binding.subTasks.text.toString(),
                icon = resources.getResourceEntryName(adapter.selectedIdRes)
            )
            this.task = task

            thread {
                TaskDatabase.open(requireContext()).tasks.addTask(task)
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }

}