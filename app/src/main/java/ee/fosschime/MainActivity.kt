package ee.fosschime

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import ee.fosschime.ui.theme.FosschimeTheme

import ee.fosschime.composables.AppHeader
import java.util.Calendar
import androidx.core.content.edit
import java.time.LocalDateTime
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            FosschimeTheme {

                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){}
                val activity = LocalContext.current
                val sharedPref = activity.getSharedPreferences("Settings", MODE_PRIVATE)
                val isOnPref = sharedPref.getBoolean("isOn", true)
                val overrideSilentPref = sharedPref.getBoolean("overrideSilent", false)
                LaunchedEffect(true) {

                    val alarmManager = getSystemService(AlarmManager::class.java)
                    if (ContextCompat.checkSelfPermission(applicationContext, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED && !sharedPref.getBoolean("notificationPermissionRequested", false)){
                        launcher.launch(POST_NOTIFICATIONS)
                        savePreferences("notificationPermissionRequested", true)
                    }

                    if (!alarmManager.canScheduleExactAlarms() && !sharedPref.getBoolean("alarmAndBatteryPermissionsRequested", false)){


                        startActivity((Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)))
                        startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        savePreferences("alarmAndBatteryPermissionsRequested", true)



                    }




                    alarmEveryHour(isOnPref, overrideSilentPref)


                }

                var isChimeOn by remember {mutableStateOf(isOnPref)}
                var overrideSilent by remember {mutableStateOf(overrideSilentPref)}


                Column {
                    AppHeader(titleResId = R.string.app_name) { }
                    ExampleButton()
                    OnOffToggle("Hourly chime", isChimeOn) { newValue ->
                        isChimeOn = newValue
                        alarmEveryHour(isChimeOn, overrideSilent)
                        savePreferences("isOn", isChimeOn)
                    }
                    OnOffToggle("Override silent mode", overrideSilent) { newValue ->
                        overrideSilent = newValue
                        alarmEveryHour(isChimeOn, overrideSilent)
                        savePreferences("overrideSilent", overrideSilent)



                    }




                }
            }

        }
    }





    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("BatteryLife")
    @Composable
    fun OnOffToggle(description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {




        val shape = RoundedCornerShape(32.dp)
        Row(
            modifier = Modifier
                .padding(40.dp, 20.dp)
                .clip(shape)
                .background(color = Color.LightGray)
                .fillMaxWidth()
                .padding(10.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        )
        {

            Text(
                text = description,
                textAlign = TextAlign.Center

            )



            Switch(
                checked = checked,
                modifier = Modifier.testTag(description),
                onCheckedChange = {
                    onCheckedChange(it)

                },
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                }
            )

            }




    }
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun ExampleButton(){
        val notificationHandler = NotificationHandler(LocalContext.current)
        Column() {
            Button(onClick = {notificationHandler.showSimpleNotification()}){Text(text="test")}
        }
    }





    @RequiresApi(Build.VERSION_CODES.S)
    fun alarmEveryHour(isOn: Boolean, overrideSilent: Boolean){

        val context: Context = applicationContext
        AlarmReceiver.scheduleNextAlarm(context, isOn, overrideSilent)

        


    }

    fun savePreferences(name: String, boolValue: Boolean?){
        val activity = applicationContext
        val sharedPref = activity.getSharedPreferences("Settings", MODE_PRIVATE)

        if(boolValue !== null) {
            sharedPref.edit {
                putBoolean(name, boolValue)
            }
        }

    }




@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview
@Composable
fun Preview(){
    Column {
        AppHeader(titleResId = R.string.app_name) { }
        OnOffToggle("Hourly chime", true){}
        OnOffToggle("Override silent mode", false){}
    }


}
}

class AlarmReceiver : BroadcastReceiver() {
    companion object{
        @RequiresApi(Build.VERSION_CODES.S)
        fun scheduleNextAlarm(context: Context, isOn: Boolean, overrideSilent: Boolean) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)

            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val calendar = Calendar.getInstance()
            intent.putExtra("isOn", isOn)
            intent.putExtra("overrideSilent", overrideSilent)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            val timeTillNextAlarm = calendar.timeInMillis

            if (isOn && alarmManager?.canScheduleExactAlarms() == true) alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeTillNextAlarm,
                pendingIntent
            )
            else alarmManager?.cancel(pendingIntent)


        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent) {
        val isOverrideSilent = intent.getBooleanExtra("overrideSilent", false)
        val isOn = intent.getBooleanExtra("isOn", true)
        val mediaPlayer = MediaPlayer.create(context, R.raw.clock_chime_88027)
        val usage =
            if (isOverrideSilent) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA

        val attributes = AudioAttributes.Builder()
            .setUsage(usage)
            .build()

        mediaPlayer.apply{
            setAudioAttributes(attributes)
            mediaPlayer.start()
        }

        NotificationHandler(context).showSimpleNotification()
        scheduleNextAlarm(context, isOn, isOverrideSilent)




    }

}


class NotificationHandler(private val context: Context) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val notificationChannelID = "hourlyNotificationID"

    fun notificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hourly Chimes"
            val description = "Hourly chime sounds"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channelId = "hourlyNotificationID"
            val channel = NotificationChannel(channelId, name, importance)
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showSimpleNotification() {
        val channel = notificationChannel()
        val notification = NotificationCompat.Builder(context, notificationChannelID)
            .setContentTitle("Time is:")
            .setContentText((LocalDateTime.now().toString()))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setAutoCancel(true)
            .build()  // finalizes the creation

        notificationManager.notify(Random.nextInt(), notification)
    }
}


