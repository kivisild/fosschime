package ee.fosschime

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ee.fosschime.ui.theme.FosschimeTheme

import ee.fosschime.composables.AppHeader
import java.util.Calendar

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FosschimeTheme {

                var isChimeOn by remember {mutableStateOf(true)}
                var overrideSilent by remember {mutableStateOf(false)}

                Column {
                    AppHeader(titleResId = R.string.app_name) { }
                    OnOffToggle("Hourly chime", isChimeOn) { newValue ->
                        isChimeOn = newValue
                        alarmEveryHour(isChimeOn, overrideSilent)
                    }
                    OnOffToggle("Override silent mode", overrideSilent) { newValue ->
                        overrideSilent = newValue
                    }
                }
            }

        }
    }



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




    @RequiresApi(Build.VERSION_CODES.S)
    fun alarmEveryHour(isOn: Boolean, overrideSilent: Boolean){

        val context: Context = applicationContext
        AlarmReceiver.scheduleNextAlarm(context, isOn, overrideSilent)


    }



@Preview
@Composable
fun Preview(){
    Column {
        AppHeader(titleResId = R.string.app_name) { }
        OnOffToggle("Hourly chime", true){}
        OnOffToggle("Override silent mode", true){}
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

            if (isOn && alarmManager.canScheduleExactAlarms()) alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeTillNextAlarm,
                pendingIntent
            )
            else alarmManager.cancel(pendingIntent)
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
        scheduleNextAlarm(context, isOn, isOverrideSilent)




    }

}



