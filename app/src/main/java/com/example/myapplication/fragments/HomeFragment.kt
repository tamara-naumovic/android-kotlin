package com.example.myapplication.fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.utils.adapter.TaskAdapter
import com.example.myapplication.utils.model.ToDoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), TodoDialogFragment.OnDialogNextBtnClickListener, TaskAdapter.TaskAdapterClickInterface {
    private val TAG = "Home Fragment"
    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String
    private lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController
    private lateinit var database: DatabaseReference

    private var popUpFragment: TodoDialogFragment?=null

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var toDoItemList: MutableList<ToDoData>

    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME = "channelName"
    private lateinit var notifyMen:NotificationManager
    val NOTIFICATION_ID = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        showAddTaskDialog()
        signOut()
        getTasksFromDatabase()
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                .apply {
                    lightColor = Color.RED
                    enableLights(true)
                }
            notifyMen = (requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)!!
            notifyMen.createNotificationChannel(channel)
        }
    }

    private fun showAddTaskDialog(){
        binding.addBtnHome.setOnClickListener {
            if (popUpFragment!=null)
                childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
            popUpFragment = TodoDialogFragment()
            popUpFragment!!.setListener(this)
            popUpFragment!!.show(childFragmentManager, TodoDialogFragment.TAG)
        }
    }

    private fun signOut(){
        binding.logout.setOnClickListener {
            auth.signOut()
            navController.navigate(R.id.action_homeFragment_to_signInFragment)
        }
    }

    private fun init(view:View) {
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser!!.uid
        database = Firebase.database.reference.child("Tasks").child(authId) //kreiranje kolekcije taskova sortiranih po IDu korisnika koji ih je dodao

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        toDoItemList = mutableListOf()
        taskAdapter = TaskAdapter(toDoItemList)
        taskAdapter.setListener(this)
        binding.recyclerView.adapter = taskAdapter

        createNotificationChannel()
    }

    private fun getTasksFromDatabase(){
        database.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                toDoItemList.clear()
                for(taskSnapshot in snapshot.children){
                    val todoTask = taskSnapshot.key?.let { ToDoData(it, taskSnapshot.value.toString()) }
                    if (todoTask!=null){
                        toDoItemList.add(todoTask)
                    }
                }
                Log.d(TAG, "onDataChanged"+toDoItemList)
                taskAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,error.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onSaveTask(todoTask: String, todoEt: TextInputEditText) {
        database
            .push().setValue(todoTask)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    //Toast.makeText(context,"Dodat task", Toast.LENGTH_SHORT).show()
                    //custom notification
                    val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                        .setContentTitle("Task Notification")
                        .setContentText("Task uspesno dodat")
                        .setSmallIcon(R.drawable.baseline_android_24)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build()
                    val notifyMenCompat: NotificationManagerCompat = NotificationManagerCompat.from(requireContext())
                    notifyMenCompat.notify(NOTIFICATION_ID, notification)

                    todoEt.text = null
                }else{
                    Toast.makeText(context,it.exception.toString(), Toast.LENGTH_LONG).show()

                }
            }
        popUpFragment!!.dismiss()
    }

    override fun onUpdateTask(toDoData: ToDoData, todoEdit: TextInputEditText) {
        val map = HashMap<String,Any>() //mapiraju se taskovi(taskId, task) - kao String, Any (moglo je biti String, String)
        map[toDoData.taskId] = toDoData.task
        database.updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(context, "Uspesno azuriran task", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context,it.exception.toString(),Toast.LENGTH_SHORT).show()
            }
            popUpFragment!!.dismiss()
        }

    }

    override fun onDeleteItemClicked(toDoData: ToDoData, position: Int) {
        database.child(toDoData.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(context,"Task uspesno obrisan", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditItemClicked(toDoData: ToDoData, position: Int) {
        if (popUpFragment!=null){
            childFragmentManager.beginTransaction().remove(popUpFragment!!).commit()
        }

        popUpFragment = TodoDialogFragment.newInstance(toDoData.taskId,toDoData.task)
        popUpFragment!!.setListener(this)
        popUpFragment!!.show(childFragmentManager,TodoDialogFragment.TAG)
    }

}