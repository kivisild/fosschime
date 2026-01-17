package ee.fosschime

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_NO_CREATE
import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {
    private var context: Context? = null
    private var alarmManager: AlarmManager? = null



    @Before
    fun setUp(){
        context = ApplicationProvider.getApplicationContext()
        alarmManager =  context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    }

    @Rule
    @JvmField
    var composeTestRule: ComposeContentTestRule = createAndroidComposeRule<MainActivity>()


    @Test
    fun alarmShouldBeOnByDefault(){
        // arrange
        val intent = Intent(context, AlarmReceiver::class.java)

        // assert
        Assert.assertNotNull(PendingIntent.getBroadcast(context, 0, intent, FLAG_NO_CREATE + FLAG_IMMUTABLE))
    }
}



