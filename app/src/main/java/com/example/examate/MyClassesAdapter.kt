package com.example.examate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyClassesAdapter(private val classesList: List<MyClass>) :
    RecyclerView.Adapter<MyClassesAdapter.ClassViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_class, parent, false)
        return ClassViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val currentClass = classesList[position]
        holder.textClassName.text = currentClass.name
        holder.textClassDate.text = currentClass.date
        holder.textClassTime.text = currentClass.time
    }

    override fun getItemCount() = classesList.size

    class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textClassName: TextView = itemView.findViewById(R.id.textClassName)
        val textClassDate: TextView = itemView.findViewById(R.id.textClassDate)
        val textClassTime: TextView = itemView.findViewById(R.id.textClassTime)
    }
}
