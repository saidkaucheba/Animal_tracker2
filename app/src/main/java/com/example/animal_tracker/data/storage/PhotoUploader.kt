package com.example.animal_tracker.data.storage

import android.content.Context
import android.net.Uri
import com.example.animal_tracker.data.SupabaseClient
import io.github.jan.supabase.storage.storage

object PhotoUploader {

    suspend fun uploadPetPhoto(
        context: Context,
        userId: String,
        petId: Int,
        uri: Uri
    ): String? {
        return try {
            val bytes = context.contentResolver
                .openInputStream(uri)?.use { it.readBytes() }
                ?: return null

            val path = "$userId/$petId/photo.jpg"

            SupabaseClient.client.storage
                .from("pet-files")
                .upload(path, bytes) {
                    upsert = true
                    contentType = io.ktor.http.ContentType.Image.JPEG
                }

            SupabaseClient.client.storage
                .from("pet-files")
                .publicUrl(path)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadAvatar(
        context: Context,
        userId: String,
        uri: Uri
    ): String? {
        return try {
            val bytes = context.contentResolver
                .openInputStream(uri)?.use { it.readBytes() }
                ?: return null

            val path = "$userId/avatar.jpg"

            SupabaseClient.client.storage
                .from("pet-files")
                .upload(path, bytes) {
                    upsert = true
                    contentType = io.ktor.http.ContentType.Image.JPEG
                }

            SupabaseClient.client.storage
                .from("pet-files")
                .publicUrl(path)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}