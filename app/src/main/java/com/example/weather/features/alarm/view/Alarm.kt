package com.example.weather.features.alarm.view

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weather.R
import com.example.weather.features.alarm.model.DialogComponents
import com.example.weather.features.alarm.services.AlarmReceiver
import com.example.weather.features.alarm.services.AlarmService
import com.example.weather.features.alarm.services.NotificationWorker
import com.example.weather.features.alarm.view_model.AlarmViewModel
import com.example.weather.features.alarm.view_model.AlarmViewModelFactory
import com.example.weather.utils.Utils
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class Alarm : Fragment(), OnDeleteClicked {

    private lateinit var viewModel: AlarmViewModel
    private lateinit var alertIcon: ImageView
    private lateinit var addAlertFAB: FloatingActionButton
    private lateinit var noAlertsTextView: TextView
    private lateinit var alertsRecyclerView: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter

    val REQUEST_OVERLAY_PERMISSION = 1234


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
            observeNoAlerts(it.isNotEmpty())
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

    private fun setUpListeners() {
        addAlertFAB.setOnClickListener {
            openAddAlertDialog()
        }
    }

    private fun initializeDialogView(dialogView: View): DialogComponents {
        val dateFrom: LinearLayout = dialogView.findViewById(R.id.dateFromLayout)
        val dateTo: LinearLayout = dialogView.findViewById(R.id.dateToLayout)
        val dateFromBtn: Button = dialogView.findViewById(R.id.btnFrom)
        val saveBtn: Button = dialogView.findViewById(R.id.saveBtn)

        val timeFromTxt: TextView = dialogView.findViewById(R.id.timeFromTxt)
        val dateFromTxt: TextView = dialogView.findViewById(R.id.dateFromTxt)
        val timeToTxt: TextView = dialogView.findViewById(R.id.timeToTxt)
        val dateToTxt: TextView = dialogView.findViewById(R.id.dateToTxt)
        val alarmTypeRadioGroup: RadioGroup = dialogView.findViewById(R.id.alarmTypeRadioGroup)

        return DialogComponents(
            dateFrom,
            dateTo,
            dateFromBtn,
            saveBtn,
            timeFromTxt,
            dateFromTxt,
            timeToTxt,
            dateToTxt,
            alarmTypeRadioGroup
        )
    }

    private fun setupAlarmTypeRadioGroup(
        alarmTypeRadioGroup: RadioGroup,
        dateToLayout: LinearLayout,
        dateFromBtn: Button,

        ) {
        alarmTypeRadioGroup.check(R.id.alarmSoundRadioButton)
        alarmTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.alarmSoundRadioButton -> {
                    dateToLayout.visibility = View.VISIBLE
                    dateFromBtn.text = getString(R.string.from)
                }

                R.id.notificationRadioButton -> {
                    dateToLayout.visibility = View.GONE
                    dateFromBtn.text = getString(R.string.`in`)
                }
            }
        }
    }

    private fun openAddAlertDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.add_alert_dialog, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val components = initializeDialogView(dialogView)

        setupInitialDateTime(
            components.timeFromTxt,
            components.dateFromTxt,
            components.timeToTxt,
            components.dateToTxt
        )
        setupAlarmTypeRadioGroup(
            components.alarmTypeRadioGroup,
            components.dateTo,
            components.dateFromBtn
        )
        setupDateTimePickers(
            components.dateFrom,
            components.dateTo,
            components.timeFromTxt,
            components.dateFromTxt,
            components.timeToTxt,
            components.dateToTxt
        )

        setupSaveButton(
            components.saveBtn,
            components.dateFromTxt,
            components.dateToTxt,
            components.timeFromTxt,
            components.timeToTxt,
            components.alarmTypeRadioGroup,
            alertDialog
        )

        alertDialog.show()
    }


    private fun setupDateTimePickers(
        dateFrom: LinearLayout,
        dateTo: LinearLayout,
        timeFromTxt: TextView,
        dateFromTxt: TextView,
        timeToTxt: TextView,
        dateToTxt: TextView
    ) {
        dateFrom.setOnClickListener {
            openDatePickerDialog { selectedDate ->
                openTimePickerDialog(selectedDate) { selectedTime ->
                    timeFromTxt.text =
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time)
                    dateFromTxt.text = SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).format(selectedDate.time)
                }
            }
        }

        dateTo.setOnClickListener {
            openDatePickerDialog { selectedDate ->
                openTimePickerDialog(selectedDate) { selectedTime ->
                    timeToTxt.text =
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time)
                    dateToTxt.text = SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).format(selectedDate.time)
                }
            }
        }
    }

    private fun setupSaveButton(
        saveBtn: Button,
        dateFromTxt: TextView,
        dateToTxt: TextView,
        timeFromTxt: TextView,
        timeToTxt: TextView,
        alarmTypeRadioGroup: RadioGroup,
        alertDialog: AlertDialog
    ) {
        saveBtn.setOnClickListener {

            if (!Settings.canDrawOverlays(requireActivity())) {
                Toast.makeText(
                    requireContext(),
                    "Please grant overlay permission to display alerts",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${requireActivity().packageName}")
                )
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
                return@setOnClickListener
            }
            val dateFrom = dateFromTxt.text.toString()
            val dateTo = dateToTxt.text.toString()
            val timeFrom = timeFromTxt.text.toString()
            val timeTo = timeToTxt.text.toString()
            val selectedAlarmType = alarmTypeRadioGroup.checkedRadioButtonId


            // Validate if date and time fields are not empty
            if (dateFrom.isEmpty() || dateTo.isEmpty() || timeFrom.isEmpty() || timeTo.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please provide valid date and time.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            Log.d("Alarm", "Selected Date Range: $dateFrom - $dateTo")

            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateFromParsed = format.parse(dateFrom)
            val dateToParsed = format.parse(dateTo)

            if (dateFromParsed != null && dateToParsed != null) {
                val startDate = dateFromParsed
                val endDate = Calendar.getInstance().apply {
                    time = dateToParsed
                    add(Calendar.DAY_OF_MONTH, 1) // Inclusive end date
                    add(Calendar.MILLISECOND, -1) // Set end date to end of day
                }.time

                // Convert start and end time to milliseconds
                val startDateTimeMillis = Utils().convertToDateTimeInMillis(dateFrom, timeFrom)
                val endDateTimeMillis = Utils().convertToDateTimeInMillis(dateTo, timeTo)

                // Fetch the weather data
                viewModel.fetch30DayWeather()
                lifecycleScope.launch {
                    viewModel.weatherData.collect { weatherList ->
                        val selectedWeatherData = weatherList.filter { weather ->
                            val weatherDate = Date(weather.dt * 1000)
                            weatherDate in startDate..endDate
                        }


                        val weatherEntry = selectedWeatherData.firstOrNull()
                        weatherEntry?.let { weather ->

                            val alarmEntity = AlarmEntity(
                                title = weather.description.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                                },
                                description = "Min ${weather.minTemp}°/Max ${weather.maxTemp}°",
                                icon = weather.icon,
                                fromHour = timeFromTxt.text.toString().substring(0, 2).toInt(),
                                fromMinute = timeFromTxt.text.toString().substring(3, 5).toInt(),
                                toHour = timeToTxt.text.toString().substring(0, 2).toInt(),
                                toMinute = timeToTxt.text.toString().substring(3, 5).toInt(),
                                startDate = startDateTimeMillis!!,
                                endDate = endDateTimeMillis!!,
                                date = weather.dt * 1000,
                                isAlarm = selectedAlarmType == R.id.alarmSoundRadioButton
                            )
                            Log.i(
                                "Alarm",
                                "AlarmEntity: ${selectedAlarmType == R.id.alarmSoundRadioButton}"
                            )

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmPermissionGranted()) {
                                requestExactAlarmPermission(alarmEntity)
                            } else {
                                viewModel.addAlert(alarmEntity)
                                if (alarmEntity.isAlarm) {
                                    scheduleAlarm(alarmEntity)

                                } else {
                                    scheduleNotification(alarmEntity)
                                }
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Invalid date range", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleNotification(alarmEntity: AlarmEntity) {
        val triggerTimeMillis = alarmEntity.startDate + 10000 - System.currentTimeMillis()

        Log.i("Alarm", "triggerTimeMillis: $triggerTimeMillis")
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(triggerTimeMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putString("alarmTitle", alarmEntity.title)
                    .putString("alarmDescription", alarmEntity.description)
                    .putLong("alarmId", alarmEntity.startDate)

                    .build()
            )
            .addTag(alarmEntity.startDate.toString())
            .build()

        Log.i("Alarm", "workRequest: ${alarmEntity.startDate}")
        WorkManager.getInstance(requireContext()).enqueue(workRequest)
    }

    private fun cancelNotification(alarmEntity: AlarmEntity) {
        val uniqueId = alarmEntity.startDate.toString()
        Log.i("Alarm", "uniqueId: $uniqueId")
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(uniqueId)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(requireActivity())) {
                Toast.makeText(requireContext(), "Overlay permission granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Overlay permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun alarmPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requireActivity().getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun requestExactAlarmPermission(alarmEntity: AlarmEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("Alarm", "Sdk version is greater than 31")
            Toast.makeText(
                requireActivity(),
                "Please grant permission to schedule exact alarms",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${requireActivity().packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            requireActivity().startActivity(intent)
        } else {
            Log.d("Alarm", "Sdk version is less than 31")
            scheduleExactAlarm(alarmEntity)
        }
    }

    private fun scheduleAlarm(alarmEntity: AlarmEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("Alarm", "Sdk version is greater than 31")
            requestExactAlarmPermission(alarmEntity)
        } else {
            Log.d("Alarm", "Sdk version is less than 31")
            scheduleExactAlarm(alarmEntity)
        }
    }

    private fun scheduleExactAlarm(alarmEntity: AlarmEntity) {
        val intent = Intent(requireActivity(), AlarmReceiver::class.java).apply {
            putExtra("alarmTitle", alarmEntity.title)
            putExtra("alarmDescription", alarmEntity.description)
            putExtra("alarmIcon", alarmEntity.icon)
            putExtra("alarmId", alarmEntity.startDate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireActivity(),
            alarmEntity.date.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("AlarmService", "Alarm hascode for creation : ${alarmEntity.date.toInt()}")


        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = alarmEntity.startDate


        try {
            Log.d(
                "AlarmService",
                "Scheduling exact alarm for ${System.currentTimeMillis() + 60000}"
            )
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            Log.d("AlarmService", "Alarm scheduled at $triggerTime")
        } catch (e: SecurityException) {
            Log.e("AlarmService", "Permission denied: cannot schedule exact alarm", e)
        }
    }

    private fun setupInitialDateTime(
        timeFromTxt: TextView,
        dateFromTxt: TextView,
        timeToTxt: TextView,
        dateToTxt: TextView
    ) {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.time

        timeFromTxt.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime)
        dateFromTxt.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime)

        calendar.add(Calendar.HOUR_OF_DAY, 1)
        val futureTime = calendar.time
        timeToTxt.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(futureTime)
        dateToTxt.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(futureTime)
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
        viewModel.deleteAlert(alarm.startDate)
        Log.d("Alarm", "Alarm deleted: $alarm")
        if (alarm.isAlarm) {
            cancelExactAlarm(alarm)

        } else {
            cancelNotification(alarm)
        }


    }

    private fun cancelExactAlarm(alarmEntity: AlarmEntity) {
        // Cancel the alarm
        val intent = Intent(requireActivity(), AlarmReceiver::class.java).apply {
            putExtra("alarmTitle", alarmEntity.title)
            putExtra("alarmDescription", alarmEntity.description)
            putExtra("alarmIcon", alarmEntity.icon)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireActivity(),
            alarmEntity.date.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d("AlarmService", "Alarm hascode for deletion : ${alarmEntity.date.toInt()}")

        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        //  stopAlarmService(requireContext())
    }
    /*
        private fun stopAlarmService(context: Context) {
            Log.d("AlarmService", "Stopping alarm service")
            val stopIntent = Intent(context, AlarmService::class.java)
            context.stopService(stopIntent)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1)
        }*/

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

