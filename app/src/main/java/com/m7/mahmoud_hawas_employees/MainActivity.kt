package com.m7.mahmoud_hawas_employees

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.m7.mahmoud_hawas_employees.model.EmployeeProject
import com.m7.mahmoud_hawas_employees.model.SharedProject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val GET_FILE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // choose file from locale storage
        findViewById<Button>(R.id.btn_read_file).setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "text/*"
                startActivityForResult(
                    Intent.createChooser(this, "select the text file"),
                    GET_FILE_REQUEST_CODE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GET_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.also {
                readFileAsString(it)
            }
        }
    }

    // convert file from string to list of objects
    private fun readFileAsString(fileUri: Uri) {
        val inputStream = contentResolver.openInputStream(fileUri)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        val employees = ArrayList<EmployeeProject>()
        val text = StringBuilder()
        try {
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                text.append(line).appendLine()

                // save each line as an employee data
                line?.filter { !it.isWhitespace() }?.also {
                    val splittedText = it.split(",")
                    employees.add(
                        EmployeeProject(
                            splittedText[0].toInt(),
                            splittedText[1].toInt(),
                            splittedText[2],
                            splittedText[3]
                        )
                    )
                }
            }

            compareEmployeesData(employees)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "choose a valid formatted text file", Toast.LENGTH_LONG).show()
        } finally {
            inputStream?.close()
            inputStreamReader.close()
            bufferedReader.close()
        }
    }

    // compare each employee's data to find shared projects time
    private fun compareEmployeesData(employees: List<EmployeeProject>) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sharedProjects = ArrayList<SharedProject>()

        employees.forEachIndexed { i, emp1 ->
            employees.forEachIndexed { j, emp2 ->
                if (i != j && emp1.empID != emp2.empID) {
                    if (emp1.projectID == emp2.projectID) {
                        // check if list has this project
                        sharedProjects.find {
                            it.projectID.toInt() == emp1.projectID
                                    && (it.emp1ID.toInt() == emp1.empID || it.emp1ID.toInt() == emp2.empID)
                                    && (it.emp2ID.toInt() == emp2.empID || it.emp2ID.toInt() == emp1.empID)
                        } ?: apply {
                            // format dates to millis to calculate diff between theme
                            val emp1StartDate = sdf.parse(emp1.dateFrom).time
                            val emp1EndDate =
                                if (emp1.dateTo != "NULL")
                                    sdf.parse(emp1.dateTo).time
                                else
                                    sdf.parse(sdf.format(Date())).time

                            val emp2StartDate = sdf.parse(emp2.dateFrom).time
                            val emp2EndDate: Long =
                                if (emp2.dateTo != "NULL")
                                    sdf.parse(emp2.dateTo).time
                                else
                                    sdf.parse(sdf.format(Date())).time

                            // get last date of starts
                            val sharedStartDateValue =
                                if (emp1StartDate > emp2StartDate) emp1StartDate else emp2StartDate
                            // get first date of ends
                            val sharedEndDateValue =
                                if (emp1EndDate < emp2EndDate) emp1EndDate else emp2EndDate

                            // convert millis to days 'count'
                            val daysWorked = TimeUnit.DAYS.convert(
                                sharedEndDateValue - sharedStartDateValue,
                                TimeUnit.MILLISECONDS
                            )

                            sharedProjects.add(
                                SharedProject(
                                    emp1.empID.toString(),
                                    emp2.empID.toString(),
                                    emp1.projectID.toString(),
                                    daysWorked.toString()
                                )
                            )
                        }
                    }
                }
            }
        }

        displayData(sharedProjects)
    }

    // display shared projects on UI
    private fun displayData(sharedProjects: ArrayList<SharedProject>) {
        // add titles to the 1st row
        sharedProjects.add(
            0,
            SharedProject(
                "Employee ID #1",
                "employee ID #2",
                "Project ID",
                "Days worked"
            )
        )

        Log.d("check_shared_projects", sharedProjects.toString())
        findViewById<RecyclerView>(R.id.rv_shared_projects).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = SharedProjectsAdapter(sharedProjects)
        }
    }
}
