package com.fundamental.alarm_manager_android

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity(), DatePickerFragment.DialogDateListener,
    TimePickerFragment.DialogTimeListener {

    private lateinit var alarmReceiver: AlarmReceiver

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(
                    this,
                    "Notification permission granted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Notification permission rejected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val buttonOnceDate: Button = findViewById(R.id.btn_once_date)
        val buttonOnceTime: Button = findViewById(R.id.btn_once_time)
        val buttonSetOnceAlarm: Button = findViewById(R.id.btn_set_once_alarm)

        setupButton(buttonOnceTime, buttonOnceDate, buttonSetOnceAlarm)
        alarmReceiver = AlarmReceiver()
    }

    override fun onDialogDateSet(tag: String?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        findViewById<TextView>(R.id.tv_once_date).text = dateFormat.format(calendar.time)
    }

    override fun onDialogTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }

        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        when (tag) {
            TIME_PICKER_REPEAT_TAG -> {}
            TIME_PICKER_ONCE_TAG -> findViewById<TextView>(R.id.tv_once_time).text =
                dateFormat.format(calendar.time)

            else -> {}
        }
    }

    private fun setupButton(
        buttonOnceTime: Button,
        buttonOnceDate: Button,
        buttonSetOnceAlarm: Button
    ) {
        buttonOnceDate.setOnClickListener {
            val datePickerFragment = DatePickerFragment()
            datePickerFragment.show(supportFragmentManager, DATE_PICKER_TAG)
        }

        buttonOnceTime.setOnClickListener {
            val timePickerFragmentOne = TimePickerFragment()
            timePickerFragmentOne.show(supportFragmentManager, TIME_PICKER_ONCE_TAG)
        }

        buttonSetOnceAlarm.setOnClickListener {
            val onceDate = findViewById<TextView>(R.id.tv_once_date).text.toString()
            val onceTime = findViewById<TextView>(R.id.tv_once_time).text.toString()
            val onceMessage = findViewById<TextView>(R.id.edt_once_message).text.toString()

            alarmReceiver.setOneTimeAlarm(
                this,
                AlarmReceiver.TYPE_ONE_TIME,
                onceDate,
                onceTime,
                onceMessage
            )
        }
    }

    companion object {
        private const val DATE_PICKER_TAG = "DatePicker"
        private const val TIME_PICKER_ONCE_TAG = "TimePickerOnce"
        private const val TIME_PICKER_REPEAT_TAG = "TimePickerRepeat"

    }
}
