package com.fundamental.alarm_manager_android

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE)
        val message = intent.getStringExtra(EXTRA_MESSAGE)

        val title =
            if (type.equals(TYPE_ONE_TIME, ignoreCase = true)) TYPE_ONE_TIME else TYPE_REPEATING
        val notifId =
            if (type.equals(TYPE_ONE_TIME, ignoreCase = true)) ID_ONETIME else ID_REPEATING

        if (message != null) {
            showAlarmNotification(context, title, message, notifId)
        }
    }

    fun setOneTimeAlarm(
        context: Context,
        type: String,
        date: String,
        time: String,
        message: String
    ) {
        if (isDateInvalid(date, DATE_FORMAT) || isDateInvalid(time, TIME_FORMAT)) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(EXTRA_MESSAGE, message)
        intent.putExtra(EXTRA_TYPE, type)

        Log.e("ONE TIME", "$date $time")
        val dateArray = date.split("-").toTypedArray()
        val timeArray = time.split(":").toTypedArray()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, dateArray[0].toInt())
            set(Calendar.MONTH, dateArray[1].toInt() - 1)
            set(Calendar.DAY_OF_MONTH, dateArray[2].toInt())
            set(Calendar.HOUR_OF_DAY, timeArray[0].toInt())
            set(Calendar.MINUTE, timeArray[1].toInt())
            set(Calendar.SECOND, 0)
        }

        val pendingIntent =
            PendingIntent.getBroadcast(context, ID_ONETIME, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Toast.makeText(context, "One time alarm set up", Toast.LENGTH_SHORT).show()
    }

    private fun isDateInvalid(date: String, dateFormat: String): Boolean {
        return try {
            val df = SimpleDateFormat(dateFormat, Locale.getDefault())
            df.isLenient = false
            df.parse(date)
            false
        } catch (e: ParseException) {
            true
        }
    }

    private fun showAlarmNotification(
        context: Context,
        title: String,
        message: String,
        notifId: Int
    ) {
        val chanelId = "Chanel_1"
        val channelName = "AlarmManager channel"
        val vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)

        val notificationManagerCompat =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, chanelId)
            .setSmallIcon(R.drawable.time_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(ContextCompat.getColor(context, android.R.color.transparent))
            .setVibrate(vibrationPattern)
            .setSound(alarmSound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chanel =
                NotificationChannel(chanelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)

            chanel.enableVibration(true)
            chanel.vibrationPattern = vibrationPattern

            builder.setChannelId(chanelId)
            notificationManagerCompat.createNotificationChannel(chanel)
        }

        val notification = builder.build()

        notificationManagerCompat.notify(notifId, notification)
    }

    companion object {
        const val TYPE_ONE_TIME = "OneTimeAlarm"
        const val TYPE_REPEATING = "RepeatingAlarm"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_TYPE = "type"

        private const val ID_ONETIME = 100
        private const val ID_REPEATING = 101

        private const val TIME_FORMAT = "dd-MM-yyyy"
        private const val DATE_FORMAT = "HH:mm"
    }
}
