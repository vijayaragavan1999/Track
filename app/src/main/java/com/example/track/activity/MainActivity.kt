package com.example.track.activity//package com.example.track
//
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.widget.ArrayAdapter
//import android.widget.Toast
//import androidx.activity.viewModels
//import androidx.lifecycle.ViewModelProvider
//import com.example.employeedatabase.databinding.ActivityMainBinding
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding : ActivityMainBinding
//    private val viewModel : EmployeeViewModel by viewModels {
//        TaskItemModelFactory((application as MyApplication).repository)
//    }
//    private var employeeAdapter: ArrayAdapter<Employee>?=null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding=ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        employeeAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, mutableListOf())
//        binding.employeeListView.adapter=employeeAdapter
//
////        val viewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(EmployeeViewModel::class.java)
////        val viewModel = ViewModelProvider(this).get(EmployeeViewModel::class.java)
//
//        viewModel.getAllEmployees().observe(this,{ emp ->
//            emp?.let {
//                employeeAdapter?.clear()
//                employeeAdapter?.addAll(it)
//                employeeAdapter?.notifyDataSetChanged()
//            }
//        })
//        setupClickListeners(viewModel)
//
//        binding.employeeListView.setOnItemClickListener{_,_,position,_ ->
//            val selectedEmployee = employeeAdapter?.getItem(position)
//            selectedEmployee?.let{ its ->
//                binding.editTextName.setText(its.name)
//                binding.editTextDesignation.setText(its.designation)
//                binding.editTextSalary.setText(its.salary.toString())
//
//                binding.buttonUpdate.setOnClickListener{
//                    val updatedName =binding.editTextName.text.toString()
//                    val updatedDesignation = binding.editTextDesignation.text.toString()
//                    val updatedSalary= binding.editTextSalary.text.toString().toDouble()
//
//                    its.name=updatedName
//                    its.designation =updatedDesignation
//                    its.salary = updatedSalary
//                    viewModel.updateEmployee(its)
//                    Toast.makeText(this,"Updated successfully",Toast.LENGTH_SHORT).show()
//                    clearInputFields()
//
//                }
//
//                binding.buttonDelete.setOnClickListener {
//                    viewModel.deleteEmployee(its)
//                    Toast.makeText(this,"Deleted successfully",Toast.LENGTH_SHORT).show()
//                    clearInputFields()
//                }
//
//
//            }
//        }
//
//    }
//
//    private fun clearInputFields(){
//        binding.editTextName.text.clear()
//        binding.editTextDesignation.text.clear()
//        binding.editTextSalary.text.clear()
//    }
//
//    private fun observeEmployeeData(viewModel: EmployeeViewModel) {
//        viewModel.getAllEmployees().observe(this,{ emp ->
//            emp?.let {
//                employeeAdapter?.clear()
//            employeeAdapter?.addAll(it)
//                employeeAdapter?.notifyDataSetChanged()
//        }
//        })
//    }
//
//    private fun setupClickListeners(viewModel: EmployeeViewModel) {
//        binding.buttonAdd.setOnClickListener {
//            val name = binding.editTextName.text.toString()
//            val designation =binding.editTextDesignation.text.toString()
//            val salary = binding.editTextSalary.text.toString().toDouble()
//            viewModel.insertEmployee(Employee(name = name,designation = designation, salary = salary))
//        }
//
//    }
//}