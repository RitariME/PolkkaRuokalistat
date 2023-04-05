package com.ritarime.polkkaruokalistat

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData.Item
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ritarime.polkkaruokalistat.ui.theme.PolkkaRuokalistatTheme
import com.ritarime.polkkaruokalistat.ui.theme.Typography
import java.net.URL
import kotlin.concurrent.thread
import com.prof.rssparser.Parser
import androidx.lifecycle.coroutineScope;
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.material.color.MaterialColors
import com.ritarime.polkkaruokalistat.getSetting
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

import android.widget.RadioGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import org.w3c.dom.Text

@Composable
fun SettingsScreen(nav: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = { SettingsTopBar(nav) },
            floatingActionButton = { SaveSettings(nav) },
            content = { innerPadding ->
                SettingsMenus(con = innerPadding)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(nav: NavController) {
    TopAppBar(
        title = { Text(text = "Asetukset", style = Typography.headlineMedium) },
    );
}

@Composable
fun SaveSettings(nav: NavController) {
    FloatingActionButton(
        onClick = {
                  nav.popBackStack()
        },
    ) {
        Icon(Icons.Filled.Done, contentDescription = "Save")
    }
}

@Composable
fun FoodType() {
    val con = LocalContext.current;
    Column(modifier = Modifier.padding(16.dp, 8.dp)) {
        Text(text = "Ruoka", style = Typography.titleMedium)

        val options = listOf("Tavallinen", "Kasvisruoka", "Kaikki")

        val selectedValue = remember { mutableStateOf(getSetting(con, "food_type")) }

        val isSelectedItem: (String) -> Boolean = { selectedValue.value == it }
        val onChangeState: (String) -> Unit = { selectedValue.value = it }
        options.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .selectable(
                        selected = isSelectedItem(item),
                        onClick = {
                            onChangeState(item);
                            saveSetting(con, "food_type", item);
                        },
                        role = Role.RadioButton
                    )
                    .padding(0.dp, 8.dp)
            ) {
                RadioButton(
                    selected = isSelectedItem(item),
                    onClick = null
                )
                Text(
                    text = item,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}



@Composable
fun SettingsMenus(con: PaddingValues) {
    LazyColumn(
        contentPadding = con
    ) {
        item {
            FoodType()
            Divider()
        }
    }
}