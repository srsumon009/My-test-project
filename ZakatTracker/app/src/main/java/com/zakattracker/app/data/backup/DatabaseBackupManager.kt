package com.zakattracker.app.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.zakattracker.app.data.db.AppDatabase
import com.zakattracker.app.data.db.DATABASE_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ImportResult {
    object Success : ImportResult()
    data class Failure(val message: String) : ImportResult()
}

private const val KEY_LAST_BACKUP = "last_backup_at_millis"

/**
 * Exports the raw SQLite database file (so it can be shared to Gmail, Google Drive, or any
 * other app via the normal Android share sheet) and imports a previously-exported file back,
 * fully replacing the current data.
 */
class DatabaseBackupManager(private val context: Context) {

    private val prefs by lazy { context.getSharedPreferences("zakat_backup_prefs", Context.MODE_PRIVATE) }

    /** Epoch millis of the last successful export, or null if one has never been made. Shown
     * in Settings as a gentle nudge to back up regularly - nothing here is sensitive, so a
     * plain (unencrypted) preferences file is fine. */
    fun lastBackupAtMillis(): Long? = prefs.getLong(KEY_LAST_BACKUP, -1L).takeIf { it > 0 }

    private fun markBackedUpNow() {
        prefs.edit().putLong(KEY_LAST_BACKUP, System.currentTimeMillis()).apply()
    }

    private fun dbFile(): File = context.getDatabasePath(DATABASE_NAME)

    /**
     * Flushes any pending WAL contents into the main database file, copies it to a
     * shareable cache location, and returns an [Intent] chooser the caller can start
     * directly (Gmail, Google Drive, Files, Bluetooth, etc. all register as share targets
     * for a generic file stream).
     */
    suspend fun buildExportIntent(): Intent = withContext(Dispatchers.IO) {
        checkpointWal()

        val backupsDir = File(context.cacheDir, "backups").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val exportFile = File(backupsDir, "zakat_tracker_backup_$stamp.db")
        dbFile().copyTo(exportFile, overwrite = true)

        val uri: Uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", exportFile
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Zakat Tracker backup - $stamp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        markBackedUpNow()
        Intent.createChooser(sendIntent, "Export Zakat database to...")
    }

    /**
     * Validates that [sourceUri] looks like a real SQLite file produced by this app, then
     * replaces the live database with it. The caller is responsible for restarting/recreating
     * any UI that was reading from the old (now-closed) database instance.
     */
    suspend fun importFrom(sourceUri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(context.cacheDir, "import_candidate.db")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext ImportResult.Failure("Could not read the selected file.")

            if (!looksLikeSqliteFile(tempFile)) {
                tempFile.delete()
                return@withContext ImportResult.Failure(
                    "That file doesn't look like a Zakat Tracker backup."
                )
            }

            // Close the live Room connection before swapping the file out from under it.
            AppDatabase.closeAndReset()

            val target = dbFile()
            target.parentFile?.mkdirs()
            tempFile.copyTo(target, overwrite = true)
            tempFile.delete()

            // Remove stale WAL/SHM sidecar files from the previous database so the copied
            // file is read cleanly on next open.
            File(target.path + "-wal").delete()
            File(target.path + "-shm").delete()

            ImportResult.Success
        } catch (e: Exception) {
            ImportResult.Failure(e.message ?: "Import failed.")
        }
    }

    private fun checkpointWal() {
        val db = AppDatabase.getInstance(context).openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(FULL)", emptyArray()).use { it.moveToFirst() }
    }

    private fun looksLikeSqliteFile(file: File): Boolean {
        if (file.length() < 16) return false
        val header = ByteArray(16)
        file.inputStream().use { it.read(header) }
        val magic = "SQLite format 3\u0000"
        return header.toString(Charsets.US_ASCII) == magic
    }
}
