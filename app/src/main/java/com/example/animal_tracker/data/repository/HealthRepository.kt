package com.example.animal_tracker.data.repository

import com.example.animal_tracker.data.SupabaseClient
import com.example.animal_tracker.data.model.Medication
import com.example.animal_tracker.data.model.Vaccination
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class HealthRepository {
    suspend fun getVaccinations(petId: Int): List<Vaccination> {
        return SupabaseClient.client.from("Vaccination")
            .select {
                filter { eq("Pet_id", petId) }
                order("Date_given", Order.DESCENDING)
            }.decodeList()
    }

    suspend fun addVaccination(vac: Vaccination) {
        SupabaseClient.client.from("Vaccination").insert(vac)
    }

    suspend fun getMedications(petId: Int): List<Medication> {
        return SupabaseClient.client.from("Medication")
            .select {
                filter { eq("Pet_id", petId); eq("Is_active", true) }
            }.decodeList()
    }

    suspend fun addMedication(med: Medication) {
        SupabaseClient.client.from("Medication").insert(med)
    }

    suspend fun deleteVaccination(id: Int) {
        SupabaseClient.client.from("Vaccination").delete {
            filter { eq("id", id) }
        }
    }

    suspend fun updateVaccination(
        id: Int,
        name: String,
        dateGiven: String,
        nextDate: String?,
        vet: String?,
        clinic: String?
    ) {
        SupabaseClient.client.from("Vaccination").update({
            set("Vaccine_name", name)
            set("Date_given", dateGiven)
            set("Next_due_date", nextDate)
            set("Vet_name", vet)
            set("Clinic", clinic)
        }) {
            filter { eq("id", id) }
        }
    }

    suspend fun finishMedication(id: Int) {
        SupabaseClient.client.from("Medication").update({ set("Is_active", false) }) {
            filter { eq("id", id) }
        }
    }
}