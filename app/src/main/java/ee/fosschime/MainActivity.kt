package ee.fosschime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FosschimeTheme {

                Column {
                    AppHeader(titleResId = R.string.app_name) { }
                    OnOffToggle("Hourly chime") { }
                    OnOffToggle("Override silent mode") { }
                }
            }

        }
    }



@Composable
fun OnOffToggle(toggleDescription: String, toggleFunction: () -> Unit){
    val shape = RoundedCornerShape(32.dp)
    Row(modifier = Modifier
        .padding(40.dp,20.dp,40.dp,20.dp)
        .clip(shape)
        .background(color = Color.LightGray)
        .fillMaxWidth()
        .padding(10.dp, 0.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically)
        {

        Text(
            text = toggleDescription,
            textAlign = TextAlign.Center

        )
        var checked by remember { mutableStateOf(true) }

        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
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



@Preview
@Composable
fun Preview(){
    Column {
        AppHeader(titleResId = R.string.app_name) { }
        OnOffToggle("Hourly chime") { } }
        OnOffToggle("Override silent mode") { }
    }


}

