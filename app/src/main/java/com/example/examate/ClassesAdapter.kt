package com.example.examate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.examate.data.ClassItem

class ClassesAdapter(
    var classesList: List<ClassItem>,
    private val onDeleteClick: (ClassItem) -> Unit,
    private val onEditClick: (ClassItem) -> Unit,
    private val onStartExamClick: (ClassItem) -> Unit
) : RecyclerView.Adapter<ClassesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val className: TextView = view.findViewById(R.id.className)
        val classDate: TextView = view.findViewById(R.id.classDate)
        val classTime: TextView = view.findViewById(R.id.classTime)
        val buttonDelete: Button = view.findViewById(R.id.buttonDelete)
        val buttonEdit: Button = view.findViewById(R.id.buttonEdit)
        val buttonStartExam: Button = view.findViewById(R.id.buttonStartExam)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_class, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val classItem = classesList[position]
        holder.className.text = classItem.name
        holder.classDate.text = classItem.getFormattedDate()
        holder.classTime.text = classItem.testStartTime

        holder.buttonDelete.setOnClickListener {
            onDeleteClick(classItem)
        }

        holder.buttonEdit.setOnClickListener {
            onEditClick(classItem)
        }

        holder.buttonStartExam.setOnClickListener {
            onStartExamClick(classItem)
        }
    }

    override fun getItemCount() = classesList.size

    fun updateClasses(newClassesList: List<ClassItem>) {
        classesList = newClassesList
        notifyDataSetChanged()
    }
}
