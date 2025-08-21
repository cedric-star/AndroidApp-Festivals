package com.example.MyFestivalApp

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    //DB Instanziieren
    private lateinit var db: MyDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //initialisieren der DB
        db = Room.databaseBuilder(
            applicationContext,
            MyDatabase::class.java, "tickets"
        ).build()

        setContent {
            MainApp(db = db)
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
    val id: Int,
    val price: Float
)


@Composable
fun MainApp(db: MyDatabase) {
    val navController = rememberNavController()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            NavHost(
                navController = navController,
                startDestination = "myMainPage",//muss auf myMainPage gesetzt werden
                modifier = Modifier.weight(1f)
            ) {
                composable("myMainPage") { MyMainPage(db) }
                composable("myTickets") { MyTickets(db) }
                composable("about") { About() }
            }

            // Bottom Navigation
            BottomNavigationBar(navController)
        }
    }
}

//stellt Objekte für Screen/Ansicht bereit
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object MainPage : Screen("myMainPage", "Acts", Icons.Default.Home)
    object Tickets : Screen("myTickets", "Tickets", Icons.Default.Star)
    object About : Screen("about", "Über uns", Icons.Default.ThumbUp)
}

//eigentliche NavBar, wird in MainApp() aufgerufen
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.MainPage,
        Screen.Tickets,
        Screen.About)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        contentColor = colorResource(R.color.secondary_text),//warum macht das nichts??
        containerColor = colorResource(R.color.background_secondary)
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        screen.icon,
                        //modifier = Modifier.background(colorResource(R.color.primary_text)),
                        contentDescription = screen.title,
                        tint = colorResource(R.color.secondary_text)) },
                label = { Text(screen.title, color = colorResource(R.color.secondary_text)) },
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
fun MyMainPage(db: MyDatabase) {
    val events = remember { mutableListOf<Event>() }
    events.addAll(getEvents())
    Text(
        textAlign = TextAlign.Center,
        text = "Unsere Acts",
        modifier = Modifier
            .background(colorResource(R.color.main_background))
            .fillMaxWidth()
            .padding(top = 30.dp),
        fontSize = 50.sp,
        fontWeight = FontWeight.SemiBold,
        color = colorResource(R.color.secondary_text)
    )
    LazyColumn(
        modifier = Modifier
            .padding(top = 100.dp)
            .fillMaxHeight()
    ) {
        items(events) { event ->
            var buyTicketDialog by remember { mutableStateOf(false) }
            if (buyTicketDialog) {
                AlertDialog(
                    containerColor = colorResource(R.color.background_secondary),
                    modifier = Modifier.background(colorResource(R.color.background_secondary)),
                    onDismissRequest = { buyTicketDialog = false },
                    title = { Text(
                        "Ticket Kaufen!",
                        color = colorResource(R.color.secondary_text),
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        modifier = Modifier.background(colorResource(R.color.background_secondary)),

                    ) },
                    text = {
                        Column {
                            Text(
                                text = "Ein Ticket für ${event.title} am ${event.date} (${event.time}Uhr)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorResource(R.color.secondary_text),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Preis: ${event.price}",
                                color = colorResource(R.color.secondary_text),
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                GlobalScope.launch {
                                    db.ticketDao().insert(Ticket(eventID = event.id, price = event.price))
                                }
                                buyTicketDialog = false

                            })
                        {
                            Text("Jetzt Kaufen")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {buyTicketDialog = false}) {
                            Text("Abbrechen")
                        }
                    }


                )
            }

            Card(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),

                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = event.title,
                    modifier = Modifier
                        .background(colorResource(R.color.background_primary))
                        .padding(16.dp)
                        .fillMaxSize(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.primary_text)
                )
                Text(
                    text = "Wann? " + event.date + ", " + event.time + " Uhr\nWo? Stage " + event.stage + "\nGenre? " + event.genre + "\n" + event.description,
                    modifier = Modifier
                        .background(colorResource(R.color.background_primary))
                        .padding(18.dp)
                        .fillMaxSize(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.primary_text)
                )
                Image(
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxSize()
                        .size(100.dp)
                        .background(colorResource(R.color.background_primary)),
                    painter = painterResource(id = event.iconPath),
                    contentDescription = "MyData",
                )
                Text(
                    text = "k",
                    modifier = Modifier
                        .background(colorResource(R.color.background_primary))
                        .padding(8.dp, bottom = 0.dp)
                        .fillMaxSize(),
                    fontSize = 6.sp,
                    fontWeight = FontWeight.Thin,
                    color = Color.Transparent
                )
                Button(
                    onClick = {
                        /*GlobalScope.launch {
                            db.ticketDao().insert(Ticket(eventID = 0, price = 29.99f))
                        }*/
                        buyTicketDialog = true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp)
                        .background(colorResource(R.color.background_secondary)),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.background_secondary))
                ) {
                    Text(
                        "Jetzt Kaufen",
                        color = colorResource(R.color.primary_text),
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    )
                }
            }

        }
    }
}

@Composable
fun LoadOverlay(
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    var dots by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        //solange es laedt werden punkte animiert
        while (isLoading) {
            delay(200)
            if (dots.equals("...")) {
                dots = ""
            } else {
                dots += "."
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background((colorResource(R.color.background_primary)).copy(alpha = 0.6f))
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = "Bitte Warten${dots}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 150.dp),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorResource(R.color.primary_text).copy(alpha = 0.9f)
                )
            }
        }
    }
}


@Composable
fun MyTickets(db: MyDatabase) {
    var myTickets by remember { mutableStateOf<List<Ticket>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Tickets automatisch laden wenn Screen erscheint
    LaunchedEffect(Unit) {
        delay(1500)
        myTickets = db.ticketDao().getAll()
        isLoading = false
    }
    LoadOverlay(isLoading = isLoading) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.main_background))) {


            if (myTickets.isEmpty()) {
                Text(
                    textAlign = TextAlign.Center,
                    text = "Noch Keine Tickets vorhanden",
                    modifier = Modifier
                        .background(colorResource(R.color.main_background))
                        .fillMaxWidth()
                        .padding(top = 100.dp),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.secondary_text)
                )
            } else {
                Text(
                    textAlign = TextAlign.Center,
                    text = "Meine Tickets",
                    modifier = Modifier
                        .background(colorResource(R.color.main_background))
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    fontSize = 50.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.secondary_text)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp)
            ) {
                items(myTickets) { ticket ->
                    var showStrDialog by remember { mutableStateOf(false) }
                    if (showStrDialog) {
                        AlertDialog(
                            containerColor = colorResource(R.color.background_secondary),
                            modifier = Modifier.background(colorResource(R.color.background_secondary)),
                            onDismissRequest = { showStrDialog = false },
                            title = { Text(
                                "Ticket Stornieren!",
                                color = colorResource(R.color.secondary_text),
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                modifier = Modifier.background(colorResource(R.color.background_secondary)),

                                ) },

                            text = {
                                Column {
                                    val evt = getEventByID(ticket.eventID)
                                    Text(
                                        text = "Wollen sie wirklich das Ticket zum Auftritt von ${evt.title} stornieren?" +
                                                "\nSie erhalten ${ticket.price}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colorResource(R.color.secondary_text),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        GlobalScope.launch {
                                            db.ticketDao().deleteByID(ticket.id)
                                            myTickets = db.ticketDao().getAll()
                                        }
                                        showStrDialog = false

                                    }
                                )
                                {
                                    Text("Stornieren")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {showStrDialog = false}) {
                                    Text("Abbrechen")
                                }
                            }


                        )
                    }
                    Card(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),

                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        val event = getEventByID(ticket.eventID)
                        Text(
                            text = event.title.toString(),
                            modifier = Modifier
                                .background(colorResource(R.color.background_primary))
                                .padding(18.dp)
                                .fillMaxSize(),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(R.color.primary_text)
                        )
                        Text(
                            text = "when? "+event.date+", "+event.time+" Uhr\nwo? "+event.stage+"\nwas? "+event.genre+"\n"+event.description,
                            modifier = Modifier
                                .background(colorResource(R.color.background_primary))
                                .padding(18.dp)
                                .fillMaxSize(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorResource(R.color.primary_text)
                        )

                        TextButton(
                            modifier = Modifier.fillMaxWidth().background(colorResource(R.color.background_secondary)),
                            onClick = {
                                GlobalScope.launch {
                                    showStrDialog = true
                                    myTickets = db.ticketDao().getAll()
                                }
                            }
                        ) {
                            Text("Stornieren",
                                color = colorResource(R.color.primary_text),
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp)
                        }

                    }
                }
            }

        }
    }
}
//./>?<,.|":;'\}{=-
//Eintrag in Datenbank (ein Ticket)
@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Long =0,
    val eventID: Int,
    val price: Float
)

//Funktionen für Datenbanknutzung
@Dao
interface TicketDao {
    @Insert
    suspend fun insert(ticket: Ticket): Long

    @Query("SELECT * FROM tickets")
    suspend fun getAll(): List<Ticket>

    @Query("DELETE FROM tickets WHERE id = :id")
    suspend fun deleteByID(id: Long)

    @Query("DELETE FROM tickets")
    suspend fun deleteAll()
}

//Datenbank Klasse
@Database(entities = [Ticket::class], version = 1)
abstract class MyDatabase: RoomDatabase() {
    abstract fun ticketDao(): TicketDao
}

fun getEvents(): List<Event> {
    val events = mutableListOf<Event>()
    events.add( Event(
        title = "Limp Schisskit",
        date = "22.11.2025",
        time = "20:00",
        description = "Scheisse geht das ab, Abriss pur!!!",
        genre = "Hard-Rock",
        stage = 2,
        iconPath = R.drawable.b8,
        id = 0,
        price = 24.99f
    ))
    events.add( Event(
        title = "RoggnRoller Jonny",
        date = "23.11.2025",
        time = "19:00",
        description = "The new era of Rock'n Roll",
        genre = "Rock",
        stage = 1,
        iconPath = R.drawable.b7,
        id = 1,
        price = 29.99f
    ))
    return events
}
fun getEventByID(id: Int): Event {
    for (event in getEvents()) {
        if (event.id.equals(id)) return event
    }
    return Event(
        title = TODO(),
        date = TODO(),
        time = TODO(),
        description = TODO(),
        genre = TODO(),
        stage = TODO(),
        iconPath = TODO(),
        id = TODO(),
        price = TODO()
    )
}

//@Preview
@Composable
fun About() {
    var stichPunkte = listOf<String>(
        "MyFestivalApp",
        "",
        "Hochschule Harz",
        "Friedrichstr. 57-59",
        "38855 Wernigerode ",
        "uXXXXX@hs-harz.de",
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(colorResource(R.color.main_background))) {
        Column(
            modifier = Modifier.padding(14.dp, top = 30.dp)
        ) {
            Text(
                text = "Über Uns:",
                modifier = Modifier
                    .padding(8.dp, bottom = 12.dp),
                color = colorResource(R.color.secondary_text),
                fontSize = 38.sp,
                textAlign = TextAlign.Center,

            )
            stichPunkte.forEach {
                    p ->
                Text(
                    text = p,
                    modifier = Modifier
                        .padding(4.dp),
                    color = colorResource(R.color.secondary_text),
                    fontSize = 30.sp,

                    )
            }
            Image(
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxSize()
                    .size(100.dp),
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "MyData",
            )

        }
    }
}

//--------------
@Composable
fun MinimalSnackbar() {/*
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
    }*/
}
