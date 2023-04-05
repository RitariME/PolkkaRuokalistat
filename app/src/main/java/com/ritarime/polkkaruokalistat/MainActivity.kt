@file:OptIn(ExperimentalMaterial3Api::class)

package com.ritarime.polkkaruokalistat

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData.Item
import android.content.Intent
import android.net.Uri
import android.nfc.Tag
import android.os.Build.VERSION
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
import java.time.DayOfWeek
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val version = packageManager.getPackageInfo(packageName, 0).versionName;

        setContent {
            PolkkaRuokalistatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var download by remember { mutableStateOf<String?>(null) }
                    GlobalScope.launch {
                        try {
                            val site = URL("https://raw.githubusercontent.com/RitariME/PolkkaRuokalistat/master/version").readText().split("\n");
                            if (site[0] > version) {
                                download = site[1];
                            }
                        }
                        catch (e: Exception) { }
                    }
                    val con = LocalContext.current;
                    if (download != null && (getSetting(con, "update") ?: version) < version) {
                        AlertDialog(
                            onDismissRequest = { download = null },
                            title = { Text("Lataa uusi versio!") },
                            text = { Text("Kannataa ladata uusin versio.\nJos et halua päivittää nyt, voit kumminkin päivittää myöhemmin sovelluksen asetuksista.") },
                            confirmButton = {
                                Button(onClick = {
                                    download = null;
                                    saveSetting(con, "update", version);
                                }
                                ) {
                                    Text("Myöhemmin")
                                }
                                Button(onClick = {
                                    download = null;
                                    saveSetting(con, "update", version);
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(download))
                                    startActivity(intent)
                                }
                                ) {
                                    Text("Joo")
                                }
                            }
                        )
                    }
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "main_screen") {
                        composable("main_screen") {
                            MainScreen(navController)
                        }
                        composable("settings_screen") {
                            SettingsScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun MainScreen(nav: NavController) {
    Scaffold(
        topBar = { TopBar() },
        floatingActionButton = { SettingsButton(navController = nav) },
        content = { innerPadding ->
            FoodList(con = innerPadding)
        }
    )
}

@Composable
fun TopBar() {
    TopAppBar(title = { Text(text = "Polkka Ruokalistat", style = Typography.headlineMedium) });
}

@Composable
fun SettingsButton(navController: NavController) {
    FloatingActionButton(
        onClick = { navController.navigate("settings_screen") },
    ) {
        Icon(Icons.Filled.Settings, contentDescription = "Settings")
    }
}

@Composable
fun FoodItem(kontent: Pair<String,String>) {
    Card(
        modifier = Modifier.padding(16.dp,8.dp)
            .wrapContentHeight()
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(text = kontent.first, style = Typography.titleMedium);
            Text(text = kontent.second, style = Typography.bodyMedium);
        }
    }
}

@Composable
fun FoodList(con: PaddingValues) {
    val foodStuff = remember { mutableStateListOf<Pair<String,String>>() }
    var err by remember { mutableStateOf<Exception?>(null) }

    val chosed = getSetting(LocalContext.current, "food_type") ?: "Tavallinen";

    val urlLink = if (LocalDate.now().dayOfWeek in DayOfWeek.MONDAY..DayOfWeek.FRIDAY) {
        "http://ruokalistat.polkkaoy.fi/rss/2/1/ac4752db-be7b-e911-920d-1cc1de04061c"
    } else {
        "http://ruokalistat.polkkaoy.fi/rss/2/2/ac4752db-be7b-e911-920d-1cc1de04061c"
    }
    GlobalScope.launch {
        try {
            val parser = Parser.Builder().build();
            val channel = parser.getChannel(urlLink)
            val tempStuff = mutableListOf<Pair<String,String>>();
            for (v in channel.articles) {
                v.title?.let { title ->
                    v.description?.let { description ->
                        val foodSplit = description.split('.')
                        if (foodSplit.count() == 3) {
                            when (chosed) {
                                "Tavallinen" -> tempStuff.add(title to foodSplit[0].trim())
                                "Kasvisruoka" -> tempStuff.add(title to foodSplit[1].trim())
                                else -> tempStuff.add(title to foodSplit[0].trim() + "\n" + foodSplit[1].trim())
                            }
                        }
                        else {
                            tempStuff.add(title to description.trim())
                        }
                    }
                }
            }
            parser.cancel();
            foodStuff.clear();
            foodStuff.addAll(tempStuff);
        } catch (e: Exception) {
            println(e);
            err = e;
        }
    }
    LazyColumn(
        contentPadding = con,
    ) {
        items(foodStuff) {
                item -> FoodItem(item)
        }
    }

    if (err != null) {
        AlertDialog(
            onDismissRequest = { err = null },
            title = { Text("Error") },
            text = { Text(err?.message ?: "An error occurred") },
            confirmButton = {
                Button(onClick = { err = null }) {
                    Text("Ei internet yhteyttä, ehkä")
                }
            }
        )
    }
}