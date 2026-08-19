package com.aipose.camera.posematch

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File
import java.io.FileNotFoundException

class DropDataProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        Log.d("DropDataProvider", "DropDataProvider initialized successfully.")
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        Log.d("DropDataProvider", "Query received for URI: $uri")
        return null
    }

    override fun getType(uri: Uri): String? {
        Log.d("DropDataProvider", "GetType requested for URI: $uri")
        return "image/*"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d("DropDataProvider", "Insert requested for URI: $uri")
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d("DropDataProvider", "Delete requested for URI: $uri")
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        Log.d("DropDataProvider", "Update requested for URI: $uri")
        return 0
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        Log.d("DropDataProvider", "OpenFile requested for URI: $uri, mode: $mode")
        val context = context ?: throw FileNotFoundException("No context available")
        
        // Use the last segment as filename, fallback to dropped_file.png
        val fileName = uri.lastPathSegment ?: "dropped_file.png"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        // Ensure parent directories exist
        file.parentFile?.mkdirs()
        
        val fileMode = when (mode) {
            "r" -> ParcelFileDescriptor.MODE_READ_ONLY
            "w" -> ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
            "rw" -> ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE
            else -> ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE
        }
        
        return try {
            ParcelFileDescriptor.open(file, fileMode)
        } catch (e: Exception) {
            Log.e("DropDataProvider", "Error opening parcel file descriptor for $fileName: ${e.message}")
            throw FileNotFoundException("Could not open file: ${e.message}")
        }
    }
}
