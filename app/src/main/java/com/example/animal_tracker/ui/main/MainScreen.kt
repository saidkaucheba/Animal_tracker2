package com.example.animal_tracker.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.animal_tracker.data.model.*
import com.example.animal_tracker.ui.activity.ActivityScreen
import com.example.animal_tracker.ui.activity.AddActivityDialog
import com.example.animal_tracker.ui.feeding.FeedingScreen
import com.example.animal_tracker.ui.feeding.FoodDirectoryScreen
import com.example.animal_tracker.ui.health.AddMedicationDialog
import com.example.animal_tracker.ui.health.AddVaccinationDialog
import com.example.animal_tracker.ui.health.HealthScreen
import com.example.animal_tracker.ui.main.components.*
import com.example.animal_tracker.ui.navigation.BottomNavBar
import com.example.animal_tracker.ui.pets.PetsScreen
import com.example.animal_tracker.ui.pets.PetProfileScreen
import com.example.animal_tracker.ui.profile.ProfileScreen
import com.example.animal_tracker.ui.reminders.AddReminderDialog
import com.example.animal_tracker.ui.reminders.ReminderScreen
import com.example.animal_tracker.ui.weight.WeightScreen
import com.example.animal_tracker.ui.theme.Beige
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val weightLogsState by viewModel.weightLogs.collectAsStateWithLifecycle()
    val foodListState by viewModel.foodList.collectAsStateWithLifecycle()
    val feedingLogsState by viewModel.feedingLogs.collectAsStateWithLifecycle()
    val activityLogsState by viewModel.activityLogs.collectAsStateWithLifecycle()
    val vaccinationsState by viewModel.vaccinations.collectAsStateWithLifecycle()
    val medicationsState by viewModel.medications.collectAsStateWithLifecycle()
    val allRemindersState by viewModel.allReminders.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf("home") }

    var showAddDialog by remember { mutableStateOf(false) }
    var petToEdit by remember { mutableStateOf<Pet?>(null) }
    var petToDelete by remember { mutableStateOf<Pet?>(null) }

    var openPetProfile by remember { mutableStateOf<Pet?>(null) }
    var openWeightScreen by remember { mutableStateOf<Pet?>(null) }
    var openFeedingScreen by remember { mutableStateOf<Pet?>(null) }
    var openFoodDirectory by remember { mutableStateOf(false) }
    var openActivityScreen by remember { mutableStateOf<Pet?>(null) }
    var openHealthScreen by remember { mutableStateOf<Pet?>(null) }
    var openRemindersScreen by remember { mutableStateOf<Pet?>(null) }

    var showAddActivityDialog by remember { mutableStateOf(false) }
    var showAddVaccinationDialog by remember { mutableStateOf(false) }
    var vaccinationToEdit by remember { mutableStateOf<Vaccination?>(null) }
    var showAddMedicationDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }

    LaunchedEffect(openWeightScreen?.id) { openWeightScreen?.let { viewModel.loadWeightLogs(it.id!!) } }
    LaunchedEffect(openFeedingScreen?.id) { openFeedingScreen?.let { viewModel.loadFoods(); viewModel.loadFeedingLogs(it.id!!, LocalDate.now().toString()) } }
    LaunchedEffect(openActivityScreen?.id) { openActivityScreen?.let { viewModel.loadActivityLogs(it.id!!) } }
    LaunchedEffect(openHealthScreen?.id) { openHealthScreen?.let { viewModel.loadHealthData(it.id!!) } }
    LaunchedEffect(openRemindersScreen?.id) { openRemindersScreen?.let { viewModel.loadReminders(it.id!!) } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PetDrawer(
                pets = state.pets,
                selectedPet = state.selectedPet,
                onPetSelected = { pet ->
                    viewModel.selectPet(pet)
                    openPetProfile = null; openWeightScreen = null; openFeedingScreen = null; openFoodDirectory = false; openActivityScreen = null; openHealthScreen = null; openRemindersScreen = null
                    selectedTab = "home"
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = Beige,
            topBar = {
                TopBar(
                    avatarUrl = state.user?.avatarUrl,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    showMenu = selectedTab != "profile"
                )
            },
            bottomBar = {
                BottomNavBar(
                    selectedRoute = selectedTab,
                    onItemClick = { route ->
                        openPetProfile = null; openWeightScreen = null; openFeedingScreen = null; openFoodDirectory = false; openActivityScreen = null; openHealthScreen = null; openRemindersScreen = null
                        selectedTab = route
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when {
                    openRemindersScreen != null -> {
                        val remPet = openRemindersScreen!!
                        ReminderScreen(
                            pet = remPet,
                            reminders = allRemindersState,
                            onBack = { openRemindersScreen = null },
                            onToggle = { id, isDone -> viewModel.toggleReminder(context, remPet.id!!, id, isDone) },
                            onAddClick = { showAddReminderDialog = true }
                        )
                    }

                    openHealthScreen != null -> {
                        val healthPet = openHealthScreen!!
                        HealthScreen(
                            pet = healthPet,
                            vaccinations = vaccinationsState,
                            medications = medicationsState,
                            onBack = { openHealthScreen = null },
                            onAddVaccinationClick = { vaccinationToEdit = null; showAddVaccinationDialog = true },
                            onAddMedicationClick = { showAddMedicationDialog = true },
                            onEditVaccinationClick = { vac -> vaccinationToEdit = vac; showAddVaccinationDialog = true },
                            onFinishMedication = { id -> viewModel.finishMedication(healthPet.id!!, id) }
                        )
                    }

                    openActivityScreen != null -> {
                        val actPet = openActivityScreen!!
                        ActivityScreen(
                            pet = actPet,
                            activities = activityLogsState,
                            onBack = { openActivityScreen = null },
                            onAddClick = { showAddActivityDialog = true }
                        )
                    }

                    openFoodDirectory && openFeedingScreen != null -> {
                        val pet = openFeedingScreen!!
                        FoodDirectoryScreen(
                            pet = pet,
                            foods = foodListState,
                            onBack = { openFoodDirectory = false },
                            onAddToSchedule = { foodId, time, date -> viewModel.addFeeding(pet.id!!, foodId, 50.0, time, date); openFoodDirectory = false },
                            onAddNewFood = { brand, name, calories -> viewModel.addNewFood(brand, name, calories) }
                        )
                    }

                    openFeedingScreen != null -> {
                        val pet = openFeedingScreen!!
                        FeedingScreen(
                            pet = pet,
                            foods = foodListState,
                            feedingLogs = feedingLogsState,
                            onBack = { openFeedingScreen = null },
                            onDateChange = { date -> viewModel.loadFeedingLogs(pet.id!!, date) },
                            onUpdateFeeding = { id, amount, time, date -> viewModel.updateFeeding(id, pet.id!!, amount, time, date) },
                            onDeleteFeeding = { id, date -> viewModel.deleteFeeding(id, pet.id!!, date) },
                            onOpenDirectory = { openFoodDirectory = true }
                        )
                    }

                    openWeightScreen != null -> {
                        val weightPet: Pet = openWeightScreen!!
                        WeightScreen(
                            pet = weightPet,
                            weightLogs = weightLogsState,
                            onBack = { openWeightScreen = null },
                            onAddWeight = { w, d, n -> viewModel.addWeight(weightPet.id!!, w, d, n) },
                            onUpdateWeight = { id, w, d, n -> viewModel.updateWeight(id, weightPet.id!!, w, n) }
                        )
                    }

                    openPetProfile != null -> {
                        val petProfile: Pet = openPetProfile!!
                        PetProfileScreen(
                            pet = petProfile,
                            lastWeightKg = state.lastWeightKg,
                            onBack = { openPetProfile = null },
                            onEdit = { petToEdit = petProfile; showAddDialog = true },
                            onWeightClick = { openWeightScreen = petProfile },
                            onActivityClick = { openActivityScreen = petProfile },
                            onFeedingClick = { openFeedingScreen = petProfile },
                            onHealthClick = { openHealthScreen = petProfile }
                        )
                    }

                    else -> {
                        when (selectedTab) {
                            "home" -> HomeTabContent(
                                state = state,
                                weightLogs = weightLogsState,
                                onPetLongClick = { pet: Pet -> petToDelete = pet }
                            )
                            "pets" -> PetsScreen(
                                pets = state.pets,
                                lastWeights = emptyMap(),
                                onPetClick = { pet: Pet -> openPetProfile = pet },
                                onAddPet = { petToEdit = null; showAddDialog = true }
                            )
                            "reminders" -> {
                                if (state.selectedPet != null) {
                                    ReminderScreen(
                                        pet = state.selectedPet!!,
                                        reminders = allRemindersState,
                                        onBack = { selectedTab = "home" },
                                        onToggle = { id, isDone -> viewModel.toggleReminder(context, state.selectedPet!!.id!!, id, isDone) },
                                        onAddClick = { showAddReminderDialog = true }
                                    )
                                } else {
                                    Text("Выберите питомца", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.Center))
                                }
                            }
                            "profile" -> ProfileScreen(
                                name = state.user?.name ?: "—",
                                email = state.user?.email ?: "—",
                                avatarUrl = state.user?.avatarUrl,
                                onAvatarChange = { uri -> viewModel.updateAvatar(context, uri) },
                                onSaveProfile = { n, e -> viewModel.updateProfile(n, e) },
                                onLogout = { viewModel.logout() }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPetDialog(
            petToEdit = petToEdit,
            onDismiss = { showAddDialog = false; petToEdit = null },
            onSave = { name, breed, birth, gender, species, photoUri ->
                if (petToEdit == null) viewModel.addPet(name, breed, birth, gender, species, photoUri, context)
                else viewModel.updatePet(petToEdit!!.id!!, name, breed, birth, gender, species, photoUri, context)
                showAddDialog = false; petToEdit = null
            }
        )
    }

    if (showAddReminderDialog && state.selectedPet != null) {
        val petId = state.selectedPet!!.id!!
        val foodNames = foodListState.map { "${it.brand} ${it.name}" }
        val vaccineNames = vaccinationsState.map { it.vaccineName }
        val medNames = medicationsState.map { it.name }

        AddReminderDialog(
            foods = foodNames,
            vaccines = vaccineNames,
            medications = medNames,
            onDismiss = { showAddReminderDialog = false },
            onSave = { type, title, time, date ->
                viewModel.addReminder(context, petId, type, title, time, date)
                showAddReminderDialog = false
            }
        )
    }

    if (showAddActivityDialog && openActivityScreen != null) {
        AddActivityDialog(
            onDismiss = { showAddActivityDialog = false },
            onSave = { type, duration, distance, repetitions, note ->
                viewModel.addActivity(openActivityScreen!!.id!!, type, duration, distance, repetitions, note)
                showAddActivityDialog = false
            }
        )
    }

    if (showAddVaccinationDialog && openHealthScreen != null) {
        AddVaccinationDialog(
            vaccinationToEdit = vaccinationToEdit,
            onDismiss = { showAddVaccinationDialog = false; vaccinationToEdit = null },
            onSave = { name, date, next, vet, clinic ->
                if (vaccinationToEdit == null) viewModel.addVaccination(openHealthScreen!!.id!!, name, date, next, vet, clinic)
                else viewModel.updateVaccination(vaccinationToEdit!!.id!!, openHealthScreen!!.id!!, name, date, next, vet, clinic)
                showAddVaccinationDialog = false; vaccinationToEdit = null
            },
            onArchive = {
                vaccinationToEdit?.let { viewModel.deleteVaccination(openHealthScreen!!.id!!, it.id!!) }
                showAddVaccinationDialog = false; vaccinationToEdit = null
            }
        )
    }

    if (showAddMedicationDialog && openHealthScreen != null) {
        AddMedicationDialog(
            onDismiss = { showAddMedicationDialog = false },
            onSave = { name, dosage, times, time, start, end ->
                viewModel.addMedication(openHealthScreen!!.id!!, name, dosage, times, null, time, start, end)
                showAddMedicationDialog = false
            }
        )
    }

    petToDelete?.let { pet ->
        AlertDialog(
            onDismissRequest = { petToDelete = null },
            title = { Text("Удалить питомца?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = { Text("Питомец «${pet.name}» и все его записи будут удалены навсегда.", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = { TextButton(onClick = { viewModel.deletePet(pet.id!!); petToDelete = null }) { Text("Удалить", color = Color(0xFFB00020)) } },
            dismissButton = { TextButton(onClick = { petToDelete = null }) { Text("Отмена", color = MaterialTheme.colorScheme.onSurface) } },
            containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun HomeTabContent(
    state: MainUiState,
    weightLogs: List<WeightLog>,
    onPetLongClick: (Pet) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface) }
        } else if (state.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Ошибка: ${state.error}", color = MaterialTheme.colorScheme.onSurface) }
        } else {
            val pet = state.selectedPet
            if (pet == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Добавьте первого питомца", color = MaterialTheme.colorScheme.onSurface) }
            } else {
                PetCard(pet = pet, lastWeightKg = state.lastWeightKg, onLongClick = { onPetLongClick(pet) })
                SectionDivider()
                ReminderCard(reminders = state.reminders)
                Spacer(Modifier.height(16.dp))
                WeightSummaryChart(weightLogs = weightLogs)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun WeightSummaryChart(weightLogs: List<WeightLog>) {
    val weekAgo = LocalDate.now().minusDays(7)
    val recentLogs = weightLogs.filter {
        try { LocalDate.parse(it.measureAt.take(10)) >= weekAgo } catch(e: Exception) { false }
    }.sortedBy { it.measureAt }

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
        Column {
            Text("Сводка веса (за неделю)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))

            if (recentLogs.isEmpty()) {
                Text("Нет замеров за неделю", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Row {
                    Column(modifier = Modifier.height(150.dp).padding(top = 10.dp, bottom = 20.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
                        listOf("30", "20", "10", "0").forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface) }
                    }
                    Spacer(Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f).height(150.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp)) {
                            for (i in 0..3) {
                                val y = size.height - (i * (size.height / 3))
                                drawLine(Color.LightGray, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
                            }
                            drawLine(Color.DarkGray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 4f)

                            val maxW = 30.0
                            val stepX = size.width / (recentLogs.size - 1).coerceAtLeast(1)

                            val points = recentLogs.mapIndexed { index, log ->
                                val x = index * stepX
                                val y = size.height - ((log.weightKg / maxW) * size.height).toFloat()
                                Offset(x, y)
                            }

                            for (i in 0 until points.size - 1) { drawLine(color = Color(0xFF3D2B1F), start = points[i], end = points[i+1], strokeWidth = 5f) }

                            points.forEachIndexed { index, point ->
                                drawCircle(color = Color(0xFF3D2B1F), radius = 6f, center = point)
                                drawContext.canvas.nativeCanvas.drawText(
                                    "${recentLogs[index].weightKg.toInt()}", point.x, point.y - 15f,
                                    android.graphics.Paint().apply { color = android.graphics.Color.parseColor("#3D2B1F"); textSize = 30f; textAlign = android.graphics.Paint.Align.CENTER }
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface) }
                        }
                    }
                }
            }
        }
    }
}