package com.example.weather.features.alarm.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.utils.model.Local.AlarmEntity
import java.text.SimpleDateFormat
import java.util.*

class AlarmAdapter(
    private var alarms: List<AlarmEntity?>,
    private val onDeleteClicked: OnDeleteClicked,
    private val context: Context
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val alertTimeTextView: TextView = view.findViewById(R.id.alertTime)
        val alertDateTextView: TextView = view.findViewById(R.id.alertDate)
        val weatherIconImageView: ImageView = view.findViewById(R.id.deleteAlarm)
        val alarmTypeIconTextView: ImageView = view.findViewById(R.id.alarmType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]

        val fromTime =
            String.format(Locale.getDefault(), "%02d:%02d", alarm!!.fromHour, alarm.fromMinute)
        val toTime = String.format(Locale.getDefault(), "%02d:%02d", alarm.toHour, alarm.toMinute)

        if (alarm.isAlarm) {
            holder.alertTimeTextView.text = context.getString(R.string.from_to, fromTime, toTime)
            holder.alarmTypeIconTextView.setImageResource(R.drawable.ic_alarm)
        } else {
            holder.alertTimeTextView.text = context.getString(R.string.at, fromTime)
            holder.alarmTypeIconTextView.setImageResource(R.drawable.ic_notification)

        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(alarm.date))
        holder.alertDateTextView.text = context.getString(R.string.date, formattedDate)

        holder.weatherIconImageView.setOnClickListener {
            onDeleteClicked.deleteClicked(alarm)
        }
    }

    override fun getItemCount(): Int = alarms.size

    fun setAlarms(alarms: List<AlarmEntity?>) {
        this.alarms = emptyList()
        this.alarms = alarms
        notifyDataSetChanged()
    }
}
