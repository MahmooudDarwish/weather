package com.example.weather.features.alarm.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.features.alarm.view_model.AlarmViewModel
import com.example.weather.features.alarm.view_model.AlarmViewModelFactory
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Alarm : Fragment(), OnDeleteClicked {

    private lateinit var viewModel: AlarmViewModel
    private lateinit var alertIcon: ImageView
    private lateinit var addAlertFAB: FloatingActionButton
    private lateinit var noAlertsTextView: TextView
    private lateinit var alertsRecyclerView: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alarmModelViewFactory = AlarmViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(requireActivity()).weatherDao(),
                    AppDatabase.getDatabase(requireActivity()).alarmDao()
                ),
                sharedPreferences = SharedPreferences(requireActivity())

            )
        )

        viewModel = ViewModelProvider(this, alarmModelViewFactory).get(AlarmViewModel::class.java)
        viewModel.getAlerts()
    }

    private fun updateAlerts(alerts: List<AlarmEntity?>?) {
        if (alerts != null) {
            alarmAdapter.setAlarms(alerts)
        }

    }

    private fun observeNoAlerts(alertsExist: Boolean) {
        if (alertsExist) {
            noAlertsTextView.visibility = View.GONE
            alertIcon.visibility = View.GONE
        } else {
            noAlertsTextView.visibility = View.VISIBLE
            alertIcon.visibility = View.VISIBLE

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi(view)
        setUpListeners()
        setUpObservers()
    }

    private fun setUpObservers() {
        viewModel.alerts.observe(viewLifecycleOwner) {
            updateAlerts(it)
            observeNoAlerts(it.isEmpty())
        }

    }

    private fun setUpListeners() {
        addAlertFAB.setOnClickListener {
            openAddAlertDialog()
        }
    }

    private fun openAddAlertDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_alert_dialog, null)

        val dateFrom: LinearLayout = dialogView.findViewById(R.id.dateFromLayout)
        val dateTo: LinearLayout = dialogView.findViewById(R.id.dateToLayout)
        val dateFromBtn : Button = dialogView.findViewById(R.id.btnFrom)
        val saveBtn : Button = dialogView.findViewById(R.id.saveBtn)

        val timeFromTxt: TextView = dialogView.findViewById(R.id.timeFromTxt)
        val dateFromTxt: TextView = dialogView.findViewById(R.id.dateFromTxt)
        val timeToTxt: TextView = dialogView.findViewById(R.id.timeToTxt)
        val dateToTxt: TextView = dialogView.findViewById(R.id.dateToTxt)
        val alarmTypeRadioGroup: RadioGroup = dialogView.findViewById(R.id.alarmTypeRadioGroup)

        // Initialize date and time
        val calendar = Calendar.getInstance()
        val currentTime = calendar.time

        timeFromTxt.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime)
        dateFromTxt.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime)

        calendar.add(Calendar.HOUR_OF_DAY, 1)
        val futureTime = calendar.time
        timeToTxt.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(futureTime)
        dateToTxt.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(futureTime)

        val defaultSelectedRadioButtonId = R.id.alarmSoundRadioButton
        alarmTypeRadioGroup.check(defaultSelectedRadioButtonId)

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        alarmTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.alarmSoundRadioButton -> {
                    dateTo.visibility = View.VISIBLE
                    dateFromBtn.text = getString(R.string.from)

                }
                R.id.notificationRadioButton -> {
                    dateTo.visibility = View.GONE
                    dateFromBtn.text = getString(R.string.`in`)
                }
            }
        }

        dateFrom.setOnClickListener {
            openDatePickerDialog { selectedDate ->
                openTimePickerDialog(selectedDate) { selectedTime ->
                    timeFromTxt.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time)
                    dateFromTxt.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
                }
            }
        }

        dateTo.setOnClickListener {
            openDatePickerDialog { selectedDate ->
                openTimePickerDialog(selectedDate) { selectedTime ->
                    timeToTxt.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time)
                    dateToTxt.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
                }
            }
        }

        saveBtn.setOnClickListener(
            {

            }
        )
    }




    private fun initUi(view: View) {
        alertIcon = view.findViewById(R.id.alarmIcon)
        noAlertsTextView = view.findViewById(R.id.noAlertsText)
        addAlertFAB = view.findViewById(R.id.addAlertFAB)
        alertsRecyclerView = view.findViewById(R.id.alertsRecyclerView)
        alertsRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)


        alarmAdapter = AlarmAdapter(mutableListOf(), this, requireActivity())
        alertsRecyclerView.adapter = alarmAdapter

    }

    override fun deleteClicked(alarm: AlarmEntity) {
        viewModel.deleteAlert(alarm)
    }

    private fun openDatePickerDialog(onDateSet: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog.newInstance(
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSet(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            minDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 0)
            }
            maxDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 30)
            }
        }
        datePickerDialog.show(parentFragmentManager, "DatePickerDialog")
    }

    private fun openTimePickerDialog(selectedDate: Calendar, onTimeSet: (Calendar) -> Unit) {
        val currentTime = Calendar.getInstance()

        val isToday = selectedDate.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR) &&
                selectedDate.get(Calendar.MONTH) == currentTime.get(Calendar.MONTH) &&
                selectedDate.get(Calendar.DAY_OF_MONTH) == currentTime.get(Calendar.DAY_OF_MONTH)

        val minHour = if (isToday) currentTime.get(Calendar.HOUR_OF_DAY) else 0
        val minMinute = if (isToday) currentTime.get(Calendar.MINUTE) else 0

        val timePickerDialog = TimePickerDialog.newInstance(
            { _, hourOfDay, minute, _ ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)
                onTimeSet(selectedDate)
            },
            minHour,
            minMinute,
            true
        ).apply {
            if (isToday) {
                setMinTime(minHour, minMinute, 0)
            }
        }

        timePickerDialog.show(parentFragmentManager, "TimePickerDialog")
    }


}