package com.example.weather.features.alarm.view

import android.Manifest
import android.app.AlarmManager
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weather.R
import com.example.weather.databinding.AddAlertDialogBinding
import com.example.weather.databinding.FragmentAlarmBinding
import com.example.weather.features.alarm.model.DateTimeResult
import com.example.weather.features.alarm.services.AlarmWorker
import com.example.weather.features.alarm.services.NotificationWorker
import com.example.weather.features.alarm.view_model.AlarmViewModel
import com.example.weather.features.alarm.view_model.AlarmViewModelFactory
import com.example.weather.utils.Utils
import com.example.weather.utils.constants.Keys
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.managers.InternetChecker
import com.example.weather.utils.model.DataState
import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class Alarm : Fragment(), OnDeleteClicked {

    private lateinit var binding: FragmentAlarmBinding
    private lateinit var popUpBinding: AddAlertDialogBinding

    private lateinit var viewModel: AlarmViewModel
    private lateinit var alarmAdapter: AlarmAdapter
    private lateinit var alertDialog: AlertDialog
    private lateinit var fromDateTimePicker: Calendar
    private lateinit var toDateTimePicker: Calendar
    private lateinit var fromDateDatePicker: Calendar
    private lateinit var toDateDatePicker: Calendar

    private val REQUEST_OVERLAY_PERMISSION = 1234
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alarmModelViewFactory = AlarmViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(requireActivity()).weatherDao(),
                    AppDatabase.getDatabase(requireActivity()).alarmDao()
                ),
                sharedPreferences = SharedPreferencesManager(
                    requireActivity().getSharedPreferences(
                        Keys.SHARED_PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                    )
                )

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        popUpBinding = AddAlertDialogBinding.inflate(LayoutInflater.from(requireContext()))
        alertDialog = AlertDialog.Builder(requireContext())
            .setView(popUpBinding.root)
            .create()
        binding = FragmentAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()
        setUpListeners()
        setUpObservers()
    }
    private fun setUpObservers() {
        lifecycleScope.launch {
            viewModel.alerts.collect() {
                updateAlerts(it)
                observeNoAlerts(it.isNotEmpty())
            }
        }
        lifecycleScope.launch {
            viewModel.weatherDataState.collect { state ->
                when (state) {
                    is DataState.Loading -> {
                    }

                    is DataState.Success -> {
                        val dateResult = parseDatesAndTimes() ?: return@collect
                        Log.d("Alarsdsdsm", "dateResult: $dateResult")
                        var selectedWeatherData  : DailyWeatherEntity? = null
                         selectedWeatherData = state.data.firstOrNull { weather ->
                            val weatherDate = Date(weather.dt * 1000)
                            weatherDate in dateResult.startDate..dateResult.endDate
                        }

                            Log.d("Alarsdsdsm", "selectedWeatherData: $selectedWeatherData")


                        selectedWeatherData?.let { weather ->
                            val alarmEntity = AlarmEntity(
                                title = weather.description.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                                },
                                description = getString(
                                    R.string.min_max_temp,
                                    weather.minTemp.toString(),
                                    weather.maxTemp.toString()
                                ),
                                icon = weather.icon,
                                fromHour = popUpBinding.timeFromTxt.text.toString().substring(0, 2)
                                    .toInt(),
                                fromMinute = popUpBinding.timeFromTxt.text.toString()
                                    .substring(3, 5).toInt(),
                                toHour = popUpBinding.timeToTxt.text.toString().substring(0, 2)
                                    .toInt(),
                                toMinute = popUpBinding.timeToTxt.text.toString().substring(3, 5)
                                    .toInt(),
                                startDate = dateResult.startDateTimeMillis,
                                endDate = dateResult.endDateTimeMillis,
                                date = weather.dt * 1000,
                                isAlarm = popUpBinding.alarmTypeRadioGroup.checkedRadioButtonId == R.id.alarmSoundRadioButton
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

                    is DataState.Error -> {
                        Log.e("Alarmhol", "Error fetching weather data: ${state.message}")
                        Toast.makeText(
                            requireActivity(),
                            getString(state.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    private fun parseDatesAndTimes(): DateTimeResult? {
        val dateFrom = popUpBinding.dateFromTxt.text.toString()
        val dateTo = popUpBinding.dateToTxt.text.toString()
        val timeFrom = popUpBinding.timeFromTxt.text.toString()
        val timeTo = popUpBinding.timeToTxt.text.toString()

        if (dateFrom.isBlank() || dateTo.isBlank() || timeFrom.isBlank() || timeTo.isBlank()) {
            Log.e("Alarm", "Date or Time fields cannot be empty")
            return null
        }

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val dateFromParsed: Date?
        val dateToParsed: Date?
        try {
            dateFromParsed = format.parse(dateFrom)
            dateToParsed = format.parse(dateTo)
        } catch (e: ParseException) {
            Log.e("Alarm", "Unparseable date: ${e.message}")
            return null
        }

        if (dateFromParsed == null || dateToParsed == null) {
            Log.e("Alarm", "Date parsing failed")
            return null
        }

        val startDate = dateFromParsed
        val endDate = Calendar.getInstance().apply {
            time = dateToParsed
            add(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }.time

        val startDateTimeMillis = Utils().convertToDateTimeInMillis(dateFrom, timeFrom)
        val endDateTimeMillis = Utils().convertToDateTimeInMillis(dateTo, timeTo)

        if (startDateTimeMillis == null || endDateTimeMillis == null) {
            Log.e("Alarm", "Failed to convert date and time to milliseconds")
            return null
        }

        return DateTimeResult(
            startDate = startDate,
            endDate = endDate,
            startDateTimeMillis = startDateTimeMillis,
            endDateTimeMillis = endDateTimeMillis
        )
    }
    private fun observeNoAlerts(alertsExist: Boolean) {
        if (alertsExist) {
            binding.noAlertsText.visibility = View.GONE
            binding.alarmIcon.visibility = View.GONE
        } else {
            binding.noAlertsText.visibility = View.VISIBLE
            binding.alarmIcon.visibility = View.VISIBLE
        }
    }
    private fun setUpListeners() {
        binding.addAlertFAB.setOnClickListener {
            if(InternetChecker(requireActivity()).isInternetAvailable()){
                openAddAlertDialog()
            }else{
                Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun openAddAlertDialog() {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.time

        fromDateDatePicker = calendar.clone() as Calendar
        toDateDatePicker = calendar.clone() as Calendar
        fromDateTimePicker = calendar.clone() as Calendar
        toDateTimePicker = calendar.clone() as Calendar

        popUpBinding.timeFromTxt.text =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime)
        popUpBinding.dateFromTxt.text =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime)

        calendar.add(Calendar.HOUR_OF_DAY, 1)
        val futureTime = calendar.time
        popUpBinding.timeToTxt.text =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(futureTime)
        popUpBinding.dateToTxt.text =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(futureTime)


        popUpBinding.alarmTypeRadioGroup.check(R.id.alarmSoundRadioButton)
        popUpBinding.alarmTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.alarmSoundRadioButton -> {
                    popUpBinding.dateToLayout.visibility = View.VISIBLE
                    popUpBinding.btnFrom.text = getString(R.string.from)
                }

                R.id.notificationRadioButton -> {
                    popUpBinding.dateToLayout.visibility = View.GONE
                    popUpBinding.btnFrom.text = getString(R.string.`in`)
                }
            }
        }
        popUpBinding.saveBtn.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
                )
                Log.d("Alarm", "Permission not granted")
                return@setOnClickListener
            }
            if (!Settings.canDrawOverlays(requireActivity())) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.grant_overlay_permission),
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${requireActivity().packageName}")
                )
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
                return@setOnClickListener
            }
            viewModel.fetch30DayWeather()
        }

        popUpBinding.dateFrom.setOnClickListener {
            openFromDatePickerDialog { selectedDate ->
                openFromTimePickerDialog { selectedTime ->

                    popUpBinding.timeFromTxt.text =
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time)
                    popUpBinding.dateFromTxt.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)

                    popUpBinding.timeToTxt.text =
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time)
                    popUpBinding.dateToTxt.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)

                }
            }
        }

        popUpBinding.dateTo.setOnClickListener {
            openToDatePickerDialog { selectedDate ->
                openToTimePickerDialog { selectedTime ->
                    popUpBinding.timeToTxt.text =
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time)
                    popUpBinding.dateToTxt.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
                }
            }
        }

        alertDialog.show()
    }
    private fun scheduleNotification(alarmEntity: AlarmEntity) {
        val triggerTimeMillis = alarmEntity.startDate + 10000 - System.currentTimeMillis()

        Log.i("Alarm", "triggerTimeMillis: $triggerTimeMillis")
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>().setInitialDelay(
            triggerTimeMillis,
            TimeUnit.MILLISECONDS
        ).setInputData(
            Data.Builder().putString(Keys.ALARM_TITLE_KEY, alarmEntity.title)
                .putString(Keys.ALARM_DESCRIPTION_KEY, alarmEntity.description)
                .putLong(Keys.ALARM_ID_KEY, alarmEntity.startDate)

                .build()
        ).addTag(alarmEntity.startDate.toString()).build()

        Log.i("Alarm", "workRequest: ${alarmEntity.startDate}")
        WorkManager.getInstance(requireContext()).enqueue(workRequest)
    }
    private fun cancelNotificationOrAlarm(alarmEntity: AlarmEntity) {
        val uniqueId = alarmEntity.startDate.toString()
        Log.i("Alarm", "uniqueId: $uniqueId")
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(uniqueId)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(requireActivity())) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.overlay_permission_granted),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.overlay_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmPermissionGranted()) {
            Log.d("Alarm", "Sdk version is greater than 31")
            Toast.makeText(
                requireActivity(),
                getString(R.string.grant_alarm_permission),
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
            scheduleAlarmWithWorkManager(alarmEntity)
        }
    }
    private fun scheduleAlarm(alarmEntity: AlarmEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("Alarm", "Sdk version is greater than 31")
            requestExactAlarmPermission(alarmEntity)
        } else {
            Log.d("Alarm", "Sdk version is less than 31")
            scheduleAlarmWithWorkManager(alarmEntity)
        }
    }
    private fun scheduleAlarmWithWorkManager(alarmEntity: AlarmEntity) {
        val data = Data.Builder()
            .putString(Keys.ALARM_TITLE_KEY, alarmEntity.title)
            .putString(Keys.ALARM_DESCRIPTION_KEY, alarmEntity.description)
            .putString(Keys.ALARM_ICON_KEY, alarmEntity.icon)
            .putLong(Keys.ALARM_ID_KEY, alarmEntity.startDate)
            .putLong(Keys.ALARM_DISMISS_KEY, alarmEntity.endDate)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInputData(data)
            .setInitialDelay((alarmEntity.startDate - System.currentTimeMillis()), TimeUnit.MILLISECONDS)
            .addTag(alarmEntity.startDate.toString())
            .build()

        WorkManager.getInstance(requireActivity()).enqueue(workRequest)
    }
    private fun initUi() {
        binding.alertsRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        alarmAdapter = AlarmAdapter(mutableListOf(), this, requireActivity())
        binding.alertsRecyclerView.adapter = alarmAdapter

    }
    override fun deleteClicked(alarm: AlarmEntity) {
        viewModel.deleteAlert(alarm.startDate)
        Log.d("Alarm", "Alarm deleted: $alarm")
            cancelNotificationOrAlarm(alarm)
    }
    private fun openFromDatePickerDialog(onDateSet: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog.newInstance(
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                fromDateDatePicker = calendar.clone() as Calendar
                fromDateTimePicker = calendar.clone() as Calendar
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
        datePickerDialog.show(parentFragmentManager, "FromDatePickerDialog")
    }
    private fun openToDatePickerDialog(onDateSet: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog.newInstance(
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                toDateDatePicker = calendar.clone() as Calendar
                onDateSet(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            minDate = fromDateDatePicker.clone() as Calendar
            maxDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 30)
            }
        }
        datePickerDialog.show(parentFragmentManager, "ToDatePickerDialog")
    }
    private fun openFromTimePickerDialog(onTimeSet: (Calendar) -> Unit) {
        val currentTime = Calendar.getInstance()
        val isToday = fromDateTimePicker.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR) &&
                fromDateTimePicker.get(Calendar.MONTH) == currentTime.get(Calendar.MONTH) &&
                fromDateTimePicker.get(Calendar.DAY_OF_MONTH) == currentTime.get(Calendar.DAY_OF_MONTH)

        val minHour = if (isToday) currentTime.get(Calendar.HOUR_OF_DAY) else 0
        val minMinute = if (isToday) currentTime.get(Calendar.MINUTE) else 0

        val timePickerDialog = TimePickerDialog.newInstance(
            { _, hourOfDay, minute, _ ->
                fromDateTimePicker.set(Calendar.HOUR_OF_DAY, hourOfDay)
                fromDateTimePicker.set(Calendar.MINUTE, minute)
                onTimeSet(fromDateTimePicker)
            }, minHour, minMinute, true
        ).apply {
            if (isToday) {
                setMinTime(minHour, minMinute, 0)
            }
        }

        timePickerDialog.show(parentFragmentManager, "FromTimePickerDialog")
    }
    private fun openToTimePickerDialog(onTimeSet: (Calendar) -> Unit) {
        val currentTime = Calendar.getInstance()
        val isFromDateToday = fromDateTimePicker.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR) &&
                fromDateTimePicker.get(Calendar.MONTH) == currentTime.get(Calendar.MONTH) &&
                fromDateTimePicker.get(Calendar.DAY_OF_MONTH) == currentTime.get(Calendar.DAY_OF_MONTH)

        val minHour: Int
        val minMinute: Int

        if (fromDateTimePicker.after(toDateTimePicker)) {
            minHour = fromDateTimePicker.get(Calendar.HOUR_OF_DAY)
            minMinute = fromDateTimePicker.get(Calendar.MINUTE)
        } else {
            minHour = if (isFromDateToday) fromDateTimePicker.get(Calendar.HOUR_OF_DAY) else 0
            minMinute = if (isFromDateToday) fromDateTimePicker.get(Calendar.MINUTE) else 0
        }

        val timePickerDialog = TimePickerDialog.newInstance(
            { _, hourOfDay, minute, _ ->
                toDateTimePicker.set(Calendar.HOUR_OF_DAY, hourOfDay)
                toDateTimePicker.set(Calendar.MINUTE, minute)
                onTimeSet(toDateTimePicker)
            }, minHour, minMinute, true
        ).apply {
            setMinTime(minHour, minMinute, 0)
        }
        timePickerDialog.show(parentFragmentManager, "ToTimePickerDialog")
    }
}

