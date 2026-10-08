package com.zakattracker.app.data.backup

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.zakattracker.app.data.model.Payment
import com.zakattracker.app.util.toDisplayDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Builds a plain-CSV export of a payment report and hands back a share [Intent] so it can
 * be sent to email, Drive, or any other app, the same way the raw database export works. */
class ReportExporter(private val context: Context) {

    suspend fun buildCsvExportIntent(
        rows: List<Pair<Payment, String>>,
        fileLabel: String
    ): Intent = withContext(Dispatchers.IO) {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(reportsDir, "${fileLabel}_$stamp.csv")

        file.bufferedWriter().use { writer ->
            writer.write("Date,Category,Amount,Recipient,Note")
            writer.newLine()
            rows.forEach { (payment, categoryName) ->
                writer.write(
                    listOf(
                        payment.paymentDateEpochDay.toDisplayDate(),
                        categoryName,
                        payment.amount.toString(),
                        payment.recipientName,
                        payment.note
                    ).joinToString(",") { csvEscape(it) }
                )
                writer.newLine()
            }
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$fileLabel - $stamp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        Intent.createChooser(intent, "Export report to...")
    }

    private fun csvEscape(value: String): String {
        val needsQuoting = value.contains(',') || value.contains('"') || value.contains('\n')
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuoting) "\"$escaped\"" else escaped
    }
}
