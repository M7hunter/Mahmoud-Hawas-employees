package com.m7.mahmoud_hawas_employees

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.m7.mahmoud_hawas_employees.model.SharedProject

class SharedProjectsAdapter(
    private val data: List<SharedProject>
) : RecyclerView.Adapter<SharedProjectsAdapter.ViewHolder>() {

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_shared_project, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class ViewHolder(iView: View) : RecyclerView.ViewHolder(iView) {

        private val tvEmp1ID: TextView = iView.findViewById(R.id.tv_emp_1_id)
        private val tvEmp2ID: TextView = iView.findViewById(R.id.tv_emp_2_id)
        private val tvProjectID: TextView = iView.findViewById(R.id.tv_project_id)
        private val tvDaysWorked: TextView = iView.findViewById(R.id.tv_days_worked)

        fun bind(itemData: SharedProject) {
            tvEmp1ID.text = itemData.emp1ID
            tvEmp2ID.text = itemData.emp2ID
            tvProjectID.text = itemData.projectID
            tvDaysWorked.text = itemData.daysWorked
        }
    }
}
