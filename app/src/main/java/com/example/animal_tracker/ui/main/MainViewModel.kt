package com.example.animal_tracker.ui.main

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animal_tracker.data.SupabaseClient
import com.example.animal_tracker.data.model.*
import com.example.animal_tracker.data.repository.*
import com.example.animal_tracker.data.storage.PhotoUploader
import com.example.animal_tracker.utils.AlarmReceiver
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class MainUiState(
    val isLoading: Boolean = true,
    val user: UserProfile? = null,
    val pets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val lastWeightKg: Double? = null,
    val reminders: List<Reminder> = emptyList(),
    val error: String? = null
)

class MainViewModel : ViewModel() {

    private val userRepo = UserRepository()
    private val petRepo = PetRepository()
    private val weightRepo = WeightRepository()
    private val reminderRepo = ReminderRepository()
    private val foodRepo = FoodRepository()
    private val feedingRepo = FeedingRepository()
    private val activityRepo = ActivityRepository()
    private val healthRepo = HealthRepository()

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state

    private val _weightLogs = MutableStateFlow<List<WeightLog>>(emptyList())
    val weightLogs: StateFlow<List<WeightLog>> = _weightLogs

    private val _foodList = MutableStateFlow<List<Food>>(emptyList())
    val foodList: StateFlow<List<Food>> = _foodList

    private val _feedingLogs = MutableStateFlow<List<FeedingLog>>(emptyList())
    val feedingLogs: StateFlow<List<FeedingLog>> = _feedingLogs

    private val _activityLogs = MutableStateFlow<List<ActivityLog>>(emptyList())
    val activityLogs: StateFlow<List<ActivityLog>> = _activityLogs

    private val _vaccinations = MutableStateFlow<List<Vaccination>>(emptyList())
    val vaccinations: StateFlow<List<Vaccination>> = _vaccinations

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications

    private val _allReminders = MutableStateFlow<List<Reminder>>(emptyList())
    val allReminders: StateFlow<List<Reminder>> = _allReminders

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val authId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException("Пользователь не авторизован")

                val user = userRepo.getByAuthId(authId) ?: throw IllegalStateException("Профиль не найден")
                val pets = petRepo.getPetsByUser(user.id!!)
                val first = pets.firstOrNull()

                _state.value = _state.value.copy(
                    isLoading = false,
                    user = user,
                    pets = pets,
                    selectedPet = first
                )
                first?.let { loadPetDetails(it.id!!) }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectPet(pet: Pet) {
        _state.value = _state.value.copy(selectedPet = pet, reminders = emptyList(), lastWeightKg = null)
        loadPetDetails(pet.id!!)
    }

    private fun loadPetDetails(petId: Int) {
        viewModelScope.launch {
            try {
                val weight = petRepo.getLastWeight(petId)

                val allRem = reminderRepo.getByPet(petId)

                val todayRem = allRem.filter {
                    try {
                        LocalDate.parse(it.remindAt.take(10)) == LocalDate.now() && !it.isDone
                    } catch(e: Exception) { false }
                }

                _state.value = _state.value.copy(lastWeightKg = weight?.weightKg, reminders = todayRem)
                _allReminders.value = allRem
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun loadReminders(petId: Int) {
        viewModelScope.launch {
            try {
                _allReminders.value = reminderRepo.getByPet(petId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addReminder(context: Context, petId: Int, type: String, title: String, timeStr: String, dateStr: String) {
        viewModelScope.launch {
            try {
                val isoDate = "${dateStr}T${timeStr}:00Z"
                val reminder = Reminder(petId = petId, type = type, title = title, remindAt = isoDate)
                reminderRepo.add(reminder)

                val dateTime = LocalDateTime.parse("${dateStr}T${timeStr}:00")
                val millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                scheduleNotification(context, millis, reminder.hashCode(), title)

                loadReminders(petId)
                loadPetDetails(petId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun toggleReminder(context: Context, petId: Int, id: Int, isDone: Boolean) {
        viewModelScope.launch {
            try {
                reminderRepo.toggleDone(id, isDone)
                if (isDone) {
                    cancelNotification(context, id)
                }
                loadReminders(petId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun scheduleNotification(context: Context, triggerTime: Long, id: Int, title: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply { putExtra("title", title) }
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun cancelNotification(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun addPet(name: String, breed: String, birthDay: String, gender: String, species: String, photoUri: Uri?, context: Context) {
        viewModelScope.launch {
            try {
                val user = _state.value.user ?: return@launch
                val newPet = Pet(userId = user.id!!, name = name, species = species, breed = breed.ifBlank { null }, birthDay = birthDay.ifBlank { null }, gender = gender, photoUrl = null)
                val created = petRepo.addPetAndReturn(newPet)
                val petId = created.id ?: return@launch

                if (photoUri != null) {
                    val url = PhotoUploader.uploadPetPhoto(context, user.authId, petId, photoUri)
                    if (url != null) {
                        val newUrlWithCacheBuster = "$url?t=${System.currentTimeMillis()}"
                        SupabaseClient.client.from("Pet").update({ set("Photo_url", newUrlWithCacheBuster) }) {
                            filter { eq("id", petId) }
                        }
                    }
                }
                load()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updatePet(petId: Int, name: String, breed: String, birthDay: String, gender: String, species: String, photoUri: Uri?, context: Context) {
        viewModelScope.launch {
            try {
                val user = _state.value.user ?: return@launch

                SupabaseClient.client.from("Pet").update({
                    set("Name", name)
                    set("Species", species)
                    set("Breed", breed.ifBlank { null })
                    set("Birth_day", birthDay.ifBlank { null })
                    set("Gender", gender)
                }) { filter { eq("id", petId) } }

                if (photoUri != null) {
                    val url = PhotoUploader.uploadPetPhoto(context, user.authId, petId, photoUri)
                    if (url != null) {
                        val newUrlWithCacheBuster = "$url?t=${System.currentTimeMillis()}"
                        SupabaseClient.client.from("Pet").update({ set("Photo_url", newUrlWithCacheBuster) }) {
                            filter { eq("id", petId) }
                        }
                    }
                }
                load()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun deletePet(petId: Int) {
        viewModelScope.launch {
            try {
                petRepo.deletePet(petId)
                load()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun loadWeightLogs(petId: Int) {
        viewModelScope.launch {
            try { _weightLogs.value = weightRepo.getByPet(petId) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addWeight(petId: Int, weightKg: Double, measuredAt: String, note: String?) {
        viewModelScope.launch {
            try {
                val log = WeightLog(petId = petId, weightKg = weightKg, measureAt = measuredAt, note = note)
                weightRepo.add(log)
                loadWeightLogs(petId)
                loadPetDetails(petId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateWeight(id: Int, petId: Int, weightKg: Double, note: String?) {
        viewModelScope.launch {
            try {
                weightRepo.update(id, weightKg, note)
                loadWeightLogs(petId)
                loadPetDetails(petId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun loadFoods() {
        viewModelScope.launch {
            try {
                val user = _state.value.user ?: return@launch
                _foodList.value = foodRepo.getAll(user.id!!)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addNewFood(brand: String, name: String, calories: Double) {
        viewModelScope.launch {
            try {
                val user = _state.value.user ?: return@launch
                foodRepo.add(Food(userId = user.id!!, brand = brand, name = name, caloriesPer100g = calories))
                loadFoods()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun loadFeedingLogs(petId: Int, dateStr: String) {
        viewModelScope.launch {
            try {
                val date = LocalDate.parse(dateStr)
                val zone = ZoneId.systemDefault()
                val startIso = date.atStartOfDay(zone).toOffsetDateTime().toString()
                val endIso = date.atTime(LocalTime.MAX).atZone(zone).toOffsetDateTime().toString()
                _feedingLogs.value = feedingRepo.getByDate(petId, startIso, endIso)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addFeeding(petId: Int, foodId: Int, amountG: Double, timeStr: String, dateStr: String) {
        viewModelScope.launch {
            try {
                val date = LocalDate.parse(dateStr)
                val time = LocalTime.parse(timeStr)
                val isoDate = date.atTime(time).atZone(ZoneId.systemDefault()).toOffsetDateTime().toString()
                feedingRepo.add(FeedingLog(petId = petId, foodId = foodId, amountG = amountG, fedAt = isoDate))
                loadFeedingLogs(petId, dateStr)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateFeeding(id: Int, petId: Int, amountG: Double, timeStr: String, dateStr: String) {
        viewModelScope.launch {
            try {
                val date = LocalDate.parse(dateStr)
                val time = LocalTime.parse(timeStr)
                val isoDate = date.atTime(time).atZone(ZoneId.systemDefault()).toOffsetDateTime().toString()
                feedingRepo.update(id, amountG, isoDate)
                loadFeedingLogs(petId, dateStr)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun deleteFeeding(id: Int, petId: Int, dateStr: String) {
        viewModelScope.launch {
            try {
                feedingRepo.delete(id)
                loadFeedingLogs(petId, dateStr)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun loadActivityLogs(petId: Int) {
        viewModelScope.launch {
            try { _activityLogs.value = activityRepo.getByPet(petId) } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addActivity(petId: Int, type: String, duration: Int, distance: Double?, repetitions: Int?, note: String?) {
        viewModelScope.launch {
            try {
                val isoDate = java.time.Instant.now().toString()
                activityRepo.add(ActivityLog(petId = petId, type = type, durationMin = duration, distanceKm = distance, repetitions = repetitions, startedAt = isoDate, note = note))
                loadActivityLogs(petId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun loadHealthData(petId: Int) {
        viewModelScope.launch {
            try {
                _vaccinations.value = healthRepo.getVaccinations(petId)
                _medications.value = healthRepo.getMedications(petId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addVaccination(petId: Int, name: String, dateGiven: String, nextDate: String?, vet: String?, clinic: String?) {
        viewModelScope.launch {
            try {
                healthRepo.addVaccination(Vaccination(petId = petId, vaccineName = name, dateGiven = dateGiven, nextDueDate = nextDate, vetName = vet, clinic = clinic))
                loadHealthData(petId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateVaccination(id: Int, petId: Int, name: String, dateGiven: String, nextDate: String?, vet: String?, clinic: String?) {
        viewModelScope.launch {
            try {
                healthRepo.updateVaccination(id, name, dateGiven, nextDate, vet, clinic)
                loadHealthData(petId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun deleteVaccination(petId: Int, id: Int) {
        viewModelScope.launch {
            try {
                healthRepo.deleteVaccination(id)
                loadHealthData(petId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun addMedication(petId: Int, name: String, dosage: String, times: String, days: String?, time: String, start: String, end: String) {
        viewModelScope.launch {
            try {
                healthRepo.addMedication(Medication(petId = petId, name = name, dosage = dosage, timesPerMonth = times, takeDays = days, intakeTime = time, startDate = start, endDate = end))
                loadHealthData(petId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun finishMedication(petId: Int, id: Int) {
        viewModelScope.launch {
            try {
                healthRepo.finishMedication(id)
                loadHealthData(petId)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val user = _state.value.user ?: return@launch
                val url = PhotoUploader.uploadAvatar(context, user.authId, uri) ?: return@launch

                val newUrlWithCacheBuster = "$url?t=${System.currentTimeMillis()}"

                SupabaseClient.client.from("User").update({ set("Avatar_url", newUrlWithCacheBuster) }) {
                    filter { eq("auth_id", user.authId) }
                }

                _state.value = _state.value.copy(user = user.copy(avatarUrl = newUrlWithCacheBuster))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            try {
                val user = _state.value.user ?: return@launch
                SupabaseClient.client.from("User").update({ set("Name", name); set("Email", email) }) { filter { eq("auth_id", user.authId) } }
                _state.value = _state.value.copy(user = user.copy(name = name, email = email))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try { SupabaseClient.client.auth.signOut() } catch (e: Exception) { e.printStackTrace() }
        }
    }
}