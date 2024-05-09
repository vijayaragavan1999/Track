package com.example.track.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

import com.example.track.R
import com.example.track.model.Location
import com.example.track.model.LocationUpdate

class LocationListAdapter(val context: Context, private var dataList: List<LocationUpdate>, private val listener: OnItemClickListener): RecyclerView.Adapter<LocationListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.location_list_view, parent, false)
            return ViewHolder(view)
    }

    private fun setlayout() {

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
        holder.locationName.text = dataList.get(position).address
            holder.rootLayout.setOnClickListener {
                listener.onGoingResponseRootLayoutClicked(dataList.get(position))
            }



    }


    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        lateinit var rootLayout: ConstraintLayout
        lateinit var locationName: TextView

        fun bind() {
                rootLayout = itemView.findViewById(R.id.rootLayout)
                locationName = itemView.findViewById(R.id.locationName)
        }
    }

    interface OnItemClickListener {
        fun onGoingResponseRootLayoutClicked(model: LocationUpdate)
    }
}