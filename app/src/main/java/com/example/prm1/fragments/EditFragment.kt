package com.example.prm1.fragments

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prm1.MainActivity
import com.example.prm1.Navigable
import com.example.prm1.R
import com.example.prm1.adapters.TaskDbObj
import com.example.prm1.adapters.TaskImagesAdapter
import com.example.prm1.databinding.FragmentEditBinding
import com.example.prm1.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread


const val ARG_EDIT_ID = "edit_id"
const val TASKS_NUMBER = "tasks_number"

class EditFragment : Fragment() {

    private lateinit var binding: FragmentEditBinding
    private lateinit var adapter: TaskImagesAdapter
    private var taskObj: TaskDbObj? = null
    private lateinit var userId: String
    private lateinit var database: DatabaseReference
    private lateinit var currentTask: Task
    private lateinit var storageRef: Task
    private lateinit var photo: Bitmap
    var tasks: HashMap<Long, Task> = HashMap<Long, Task>()
    var taskIdToHash: HashMap<String, String> = HashMap<String, String>()
    var tasksNumber : Long = 0;
    private val ONE_MEGABYTE: Long = 1024 * 1024
    private var storage = Firebase.storage("gs://prm-project-fae69.appspot.com")

    private val CHANNEL_ID = "0"



    //CAMERA
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private var imageUri: Uri? = null
    private val onTakePhoto: (Boolean) -> Unit = { photography: Boolean ->
        if (!photography) {
            imageUri?.let {
                requireContext().contentResolver.delete(it, null, null)
            }
        } else {
            loadPhoto()
        }
    }

    private fun loadPhoto() {
        val imageUri = imageUri ?: return
        requireContext().contentResolver.openInputStream(imageUri)
            ?.use {
            BitmapFactory.decodeStream(it)
        }?.let {
                binding.photo.setImageBitmap(it)
                photo = it
        }
    }

    //DO BUTTONA
    private fun createPicture() {
        val imagesUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val ct = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "tasksPhotos.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "images/jpeg")
        }
        imageUri = requireContext().contentResolver.insert(imagesUri, ct)
        //button
        cameraLauncher.launch(imageUri)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture(),
            onTakePhoto
        )
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
        return FragmentEditBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    @SuppressLint("RemoteViewLayout", "MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TaskImagesAdapter()

        database.get().addOnSuccessListener {
            tasks = HashMap<Long, Task>()
            tasksNumber = 0
            for (tasksSnapshot in it.children) {
                var tmpTask = tasksSnapshot.getValue(TaskDbObj::class.java)
                if (tmpTask != null) {
                    tasks[tasksNumber] = Task(
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
                    tasksNumber++;
                }
            }
            tasksNumber = tasks.size.toLong()

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
                        binding.subTasks.setText(currentTask?.subTasks?.joinToString(",\n") ?: "")
                        adapter.setSelection(currentTask.resId)
                    }
                }
            }

            binding.images.apply {
                adapter = this@EditFragment.adapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }

            binding.btnCancel.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            binding.btnSave.setOnClickListener {

                var newId = if (id == -1L) tasksNumber + 1 else currentTask.id

                this.taskObj = TaskDbObj(
                    id = newId,
                    name = binding.taskName.text.toString(),
                    subTasks = binding.subTasks.text.split("\n"),
                    resName = resources.getResourceEntryName(adapter.selectedIdRes)
                )

                thread {
                    val database =
                        FirebaseDatabase.getInstance("https://prm-project-fae69-default-rtdb.europe-west1.firebasedatabase.app/").reference.child(
                            "tasks"
                        )
                            .child(userId)

                    var storageRef = storage.reference.child(userId+"/"+ taskObj!!.id+"/")
                    // create new
                    if (id == -1L) {
                        //TODO" check how to move it above
                        if (this::photo.isInitialized) {

                        val baos = ByteArrayOutputStream()
                        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()
                            var uploadTask = storageRef.putBytes(data)
                            uploadTask.addOnFailureListener {
                                Log.d("myTag", it.message.toString());
                            }.addOnSuccessListener { taskSnapshot ->
                                Log.d("myTag", taskSnapshot.toString());
                            }

                        }
                        database.push().setValue(taskObj)
                        dispatchNotification("New Task!", "You got new task: "+ taskObj!!.name)
                    } else {
//                        Update

                        // Delete the file
                        storageRef.delete().addOnSuccessListener {

                            if (this::photo.isInitialized) {
                                val baos = ByteArrayOutputStream()
                                photo.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                val data = baos.toByteArray()
                                var uploadTask = storageRef.putBytes(data)
                                uploadTask.addOnFailureListener {
                                    Log.d("myTag", it.message.toString());
                                }.addOnSuccessListener { taskSnapshot ->
                                    Log.d("myTag", taskSnapshot.toString());
                                }
                            }

                        }.addOnFailureListener {
                            Log.d("myTag", it.message.toString());
                        }

                        taskObj
                        val childUpdates = hashMapOf<String, Any>()
                        childUpdates.put(currentTask.dbHash!!, taskObj!!)
                        database.updateChildren(childUpdates)
                        dispatchNotification("Task Updated!", "Your task: "+ taskObj!!.name+" has been updated!")

                    }
                    (activity as? Navigable)?.navigate(Navigable.Destination.List)
                }
            }

            binding.btnPhoto.setOnClickListener {
                createPicture()
            }

        }
    }
    @SuppressLint("MissingPermission")
    private fun dispatchNotification(title: String, description: String) {
        createNotificationChannel()
        val date = Date();
        val notificationId = SimpleDateFormat("ddHHmmss", Locale.GERMANY).format(date).toInt();

        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val mainPendingIntent = PendingIntent.getActivity(
            requireContext(),
            1,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )


        val notificationBuilder = NotificationCompat.Builder(requireContext(), "$CHANNEL_ID")
        notificationBuilder.setSmallIcon(R.drawable.logosmall)
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setContentText(description)
        notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setContentIntent(mainPendingIntent)


        val notificationManagerCompact = NotificationManagerCompat.from(requireContext())
        notificationManagerCompact.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        val name: CharSequence = "New task!"
        val description = "You new task has been created"

        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
        notificationChannel.description = description
        val notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

}