package com.example.prm1.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.prm1.Navigable
import com.example.prm1.R
import com.example.prm1.adapters.TaskDbObj
import com.example.prm1.adapters.TaskImagesAdapter
import com.example.prm1.databinding.FragmentDisplayBinding
import com.example.prm1.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.concurrent.thread

/**
 * A simple [Fragment] subclass.
 * Use the [DisplayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DisplayFragment : Fragment() {

    private lateinit var binding: FragmentDisplayBinding
    private lateinit var adapter: TaskImagesAdapter
    private lateinit var userId: String
    private lateinit var database: DatabaseReference
    private lateinit var currentTask: Task
    private val ONE_MEGABYTE: Long = 1024 * 1024
    private lateinit var photo: Bitmap
    private var storage = Firebase.storage("gs://prm-project-fae69.appspot.com")
    var tasks: HashMap<Long, Task> = HashMap<Long, Task>()
    var tasksNumber : Long = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        userId = user!!.uid
        database =
            FirebaseDatabase.getInstance("https://prm-project-fae69-default-rtdb.europe-west1.firebasedatabase.app/").reference.child(
                "tasks"
            )
                .child(userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentDisplayBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TaskImagesAdapter()

        finishSetup();

            binding.btnReturn.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

    fun finishSetup() = thread {

        database.get().addOnSuccessListener {
            tasks = HashMap<Long, Task>()
            tasksNumber = 0
            for (tasksSnapshot in it.children) {
                var tmpTask = tasksSnapshot.getValue(TaskDbObj::class.java);
                if (tmpTask != null) {
                    tasks[tasksNumber] = Task(
                        id = tmpTask.id,
                        name = tmpTask.name,
                        subTasks = tmpTask.subTasks,
                        resId = resources.getIdentifier(
                            tmpTask.resName.toString(),
                            "drawable",
                            requireContext().packageName
                        ),
                    )
                    tasksNumber++;
                }
            }

            val id = requireArguments().getLong(ARG_EDIT_ID, -1)

            //w przypadku edycji
            if (id != -1L) {
                currentTask = tasks[id - 1]!!

                thread {

                    var storageRef = storage.reference.child(userId+"/"+ currentTask!!.id+"/")

                    storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                        photo = BitmapFactory.decodeByteArray(it, 0, it.size)
                        binding.photo.setImageBitmap(photo)
                    }

                    requireActivity().runOnUiThread {
                        binding.taskName.setText(currentTask?.name ?: "")
                        binding.taskName.isEnabled = false;
                        binding.subTasks.setText(currentTask?.subTasks?.joinToString(",\n") ?: "")
                        binding.subTasks.isEnabled = false;
                        binding.imageTask.setImageResource(currentTask.resId!!)
                    }
                }

                binding.btnShare.setOnClickListener {
                    var intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"

                    val sb = StringBuilder()
                    sb.append(getText(R.string.task))
                        .append(" ")
                        .append(currentTask?.name)
                        .append("\n")
                        .append(getText(R.string.subTasks))
                        .append(" ")
                        .append(currentTask?.subTasks)
                    val body = sb.toString()
                    intent.putExtra(Intent.EXTRA_TEXT, body)
                    startActivity(Intent.createChooser(intent, R.string.shareUsing.toString()))
                }

            }

            binding.btnEdit.setOnClickListener {
                (activity as? Navigable)?.navigate(Navigable.Destination.Edit, id)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}