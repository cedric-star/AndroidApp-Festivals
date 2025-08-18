package com.example.mycolorchangingbox

import android.content.Context
import androidx.compose.material3.Snackbar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import org.xmlpull.v1.XmlPullParser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainApp()
        }
    }
}

data class Event(
    val title: String,
    val date: String,
    val time: String,
    val description: String,
    val genre: String,
    val stage: Number,
    val iconPath: Int,

)

//beinhaltet NavBar
@Composable
fun MainApp() {
    val navController = rememberNavController()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            NavHost(
                navController = navController,
                startDestination = "myMainPage",
                modifier = Modifier.weight(1f)
            ) {
                composable("myMainPage") { MyMainPage() }
                composable("counter") { MyCounter() }
                composable("snackBar") { MinimalSnackbar() }
                composable("mylist") { MyList() }
                composable("mybilder") { MyBilder() }
            }

            // Bottom Navigation
            BottomNavigationBar(navController)
        }
    }
}

//stellt Objekte für Screen/Ansicht bereit
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object MainPage : Screen("myMainPage", "Main", Icons.Default.Home)
    object Counter : Screen("counter", "Buy Now", Icons.Default.Star)
    object SnackBar : Screen("snackBar", "Nachricht", Icons.Default.Notifications)
    object MyList : Screen("mylist", "My Tickets", Icons.Default.PlayArrow)
    object MyBilder : Screen("mybilder", "About", Icons.Default.ThumbUp)
}

//eigentliche NavBar, wird in MainApp() aufgerufen
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.MainPage,
        Screen.Counter,
        Screen.SnackBar,
        Screen.MyList,
        Screen.MyBilder
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun MyBilder() {
    Box (modifier = Modifier.fillMaxSize().background(Color.Cyan)) {
        // Beispiel-Daten
        val horizontalPics = listOf(
            R.drawable.b1,
            R.drawable.b2,
            R.drawable.b3,
            R.drawable.b4,
            R.drawable.b5,
            R.drawable.b6,
            R.drawable.b7,
            R.drawable.b8,
            R.drawable.b9,
            R.drawable.b10,
            R.drawable.b11
            )
        val listState = rememberLazyListState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(color = Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card {
                Text(
                    text = "MyBildList580Dong",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .background(color = Color.Yellow)
                        .fillMaxWidth()
                        .padding(8.dp),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Magenta
                )
            }


            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxHeight()
            ) {
                items(horizontalPics) { img ->
                    var showDialog by remember { mutableStateOf(false) }

                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Bild in groß") },
                            text = {
                                Image(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .fillMaxSize(),

                                    //color = Color.Transparent,
                                    painter = painterResource(id = img),
                                    contentDescription = "Mein Logo",
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { showDialog = false},) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                    TextButton(
                        onClick = { showDialog = true },
                        border = BorderStroke(0.dp, Color.Black),

                        modifier = Modifier
                            .background(Color.Transparent)
                            .border(BorderStroke(1.dp, Color.Black)),
                        shape = RectangleShape // oder RoundedCornerShape(0.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(Color.Transparent)
                        ) {
                            Image(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(100.dp),
                                //color = Color.Transparent,
                                painter = painterResource(id = img),
                                contentDescription = "Mein Logo",
                            )

                        }


                    }


                }
            }
        }
    }

}

@Composable
fun MyList() {
    Box (modifier = Modifier.fillMaxSize().background(Color.Cyan)) {
        // Beispiel-Daten
        val horizontalWords = listOf(
            "Apfel",
            "MarkusMarkus",
            "Kirsche",
            "Dattel",
            "Erdbeere",
            "deindad",
            "du",
            "CasaBonita",
            "580 Dong"
        )
        val listState = rememberLazyListState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(color = Color.Cyan),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card {
                Text(
                    text = "Vertikale Wortliste",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .background(color = Color.Yellow)
                        .fillMaxWidth()
                        .padding(8.dp),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Magenta
                )
            }


            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxHeight()
            ) {
                items(horizontalWords) { word ->
                    Card(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),

                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = word,
                            modifier = Modifier
                                .background(Color.Magenta)
                                .padding(16.dp)
                                .fillMaxSize(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Yellow
                        )

                    }
                }
            }
        }
    }
}

@Composable
fun MyCounter() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var counter by remember { mutableIntStateOf(0) }
        var inputValue by remember { mutableStateOf("") }

        Text(
            text = "Zählerstand: $counter",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.headlineMedium
        )
        Row {
            Button(
                onClick = { counter++ },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("+", fontWeight = FontWeight.Bold, fontSize = 30.sp)
            }
            Button(
                onClick = { counter-- },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("-", fontWeight = FontWeight.Bold, fontSize = 30.sp)
            }
        }



        OutlinedTextField(
            value = "$inputValue",
            onValueChange = { inputValue = it },
            label = { Text("Wert zum addieren") },
            modifier = Modifier.padding(8.dp)
        )

        Button(
            onClick = {
                val numberToAdd = inputValue.toIntOrNull() ?: 0
                counter += numberToAdd
                inputValue = "" // Feld leeren nach dem Hinzufügen
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Benutzerwert addieren")
        }

        Button(
            onClick = {
                counter = 0
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("auf 0 zurücksetzen")
        }

    }
}

@Preview
@Composable
fun MyMainPage() {
    val events = remember { mutableListOf<Event>() }
    events.addAll(getEvents())
    Text(
        text = "d",
        modifier = Modifier
            .background(Color.Magenta)
            .fillMaxWidth()
            .padding(10.dp),
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Yellow
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(modifier = Modifier.fillMaxHeight()) {
            items(events) { event ->
                Card(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),

                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = event.title,
                        modifier = Modifier
                            .background(Color.Magenta)
                            .padding(16.dp)
                            .fillMaxSize(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Yellow
                    )
                    Text(
                        text = "when? "+event.date+", "+event.time+" Uhr\nwo? "+event.stage+"\nwas? "+event.genre+"\n"+event.description,
                        modifier = Modifier
                            .background(Color.Magenta)
                            .padding(18.dp)
                            .fillMaxSize(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Yellow
                    )
                    Image(
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxSize()
                            .size(100.dp)
                            .background(Color.Magenta),
                        painter = painterResource(id =  event.iconPath),
                        contentDescription = "Mein Logo",
                    )
                    Text (
                        text = "k",
                        modifier = Modifier
                            .background(Color.Magenta)
                            .padding(8.dp)
                            .fillMaxSize(),
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Thin,
                        color = Color.Magenta
                    )
                }
            }
        }
    }
}


//liest json aus
fun getEvents(): List<Event> {
    val events = mutableListOf<Event>()
    events.add( Event(
        title = "RoggnRollerrudi",
        date = "23.11.2025",
        time = "19:00",
        description = "The new era of Rock'n Roll",
        genre = "Rock",
        stage = 1,
        iconPath = R.drawable.b7
    ))
    return events
}

@Composable
fun MinimalSnackbar() {
    var show by remember { mutableStateOf(false) }

    Column {
        Button(onClick = { show = true }) {
            Text("Zeige Nachricht")
        }

        if (show) {
            LaunchedEffect(Unit) {
                delay(1500)
                show = false
            }
            Snackbar { Text("Fertig!") }
        }
    }
}
