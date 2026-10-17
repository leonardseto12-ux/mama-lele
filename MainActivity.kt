package id.mamalele.bukukas

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var db: DatabaseHelper
    private lateinit var content: LinearLayout
    private val green = Color.rgb(34, 105, 76)
    private val cream = Color.rgb(250, 247, 237)
    private val currency = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DatabaseHelper(this)
        showHome()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun text(value: String, size: Float = 16f, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.rgb(45, 45, 45))
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(dp(4), dp(6), dp(4), dp(6))
        }

    private fun button(label: String, onClick: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { onClick() }
        }

    private fun makeLayout(title: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(cream)
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }
        val header = TextView(this).apply {
            text = "🐟  MAMA LELE\n$title"
            textSize = 22f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(green)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(18))
        }
        root.addView(header, LinearLayout.LayoutParams(-1, -2))
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, dp(18))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun showHome() {
        makeLayout("Buku Kas Digital UKM")
        val totals = db.totals()
        val balance = totals.first - totals.second
        content.addView(text("Ringkasan Keuangan", 19f, true))
        content.addView(text("Total pemasukan: ${formatMoney(totals.first)}", 16f, true))
        content.addView(text("Total pengeluaran: ${formatMoney(totals.second)}", 16f, true))
        content.addView(text("Saldo saat ini: ${formatMoney(balance)}", 20f, true))
        content.addView(text("Data tersimpan di HP dan dapat digunakan tanpa internet.", 13f))
        content.addView(button("＋ Catat Uang Masuk") { showAdd("MASUK") })
        content.addView(button("－ Catat Uang Keluar") { showAdd("KELUAR") })
        content.addView(button("Lihat Buku Kas") { showTransactions() })
        content.addView(button("Laporan Harian") { showDailyReport() })
        content.addView(button("Laporan Bulanan") { showMonthlyReport() })
        content.addView(button("Ekspor Laporan PDF") { exportPdf() })
        content.addView(text("Transaksi Terbaru", 18f, true))
        db.allTransactions().take(5).forEach { content.addView(transactionView(it, false)) }
        if (db.allTransactions().isEmpty()) {
            content.addView(text("Belum ada transaksi. Tekan Catat Uang Masuk atau Catat Uang Keluar untuk mulai."))
        }
    }

    private fun showAdd(type: String) {
        makeLayout(if (type == "MASUK") "Catat Uang Masuk" else "Catat Uang Keluar")
        val dateField = EditText(this).apply {
            hint = "Tanggal (YYYY-MM-DD)"
            setText(today())
            inputType = android.text.InputType.TYPE_CLASS_DATETIME
        }
        val noteField = EditText(this).apply { hint = "Keterangan (contoh: penjualan ikan)" }
        val amountField = EditText(this).apply {
            hint = "Nominal rupiah (contoh: 50000)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        content.addView(text("Tanggal"))
        content.addView(dateField)
        content.addView(text("Keterangan"))
        content.addView(noteField)
        content.addView(text("Nominal (Rp)"))
        content.addView(amountField)
        content.addView(button("Simpan Transaksi") {
            val date = dateField.text.toString().trim()
            val note = noteField.text.toString().trim()
            val amount = amountField.text.toString().trim().toLongOrNull()
            if (!date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) || note.isBlank() || amount == null || amount <= 0) {
                AlertDialog.Builder(this).setMessage("Isi tanggal YYYY-MM-DD, keterangan, dan nominal lebih dari nol.")
                    .setPositiveButton("OK", null).show()
            } else {
                val id = db.addTransaction(date, type, note, amount)
                if (id > 0) {
                    Toast.makeText(this, "Transaksi tersimpan", Toast.LENGTH_SHORT).show()
                    showHome()
                } else {
                    Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
                }
            }
        })
        content.addView(button("Kembali") { showHome() })
    }

    private fun showTransactions() {
        makeLayout("Buku Kas")
        content.addView(text("Tekan transaksi untuk menghapus jika salah input.", 13f))
        db.allTransactions().forEach { item -> content.addView(transactionView(item, true)) }
        content.addView(button("Kembali ke Beranda") { showHome() })
    }

    private fun transactionView(item: Transaction, canDelete: Boolean): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.WHITE)
        }
        val prefix = if (item.type == "MASUK") "+" else "−"
        box.addView(text("$prefix ${formatMoney(item.amount)}", 16f, true))
        box.addView(text("${item.date} • ${item.note} (${item.type})", 13f))
        if (canDelete) {
            box.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Hapus transaksi?")
                    .setMessage("${item.date} - ${item.note} - ${formatMoney(item.amount)}")
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Hapus") { _, _ ->
                        db.deleteTransaction(item.id)
                        showTransactions()
                    }.show()
            }
        }
        val params = LinearLayout.LayoutParams(-1, -2)
        params.setMargins(0, dp(4), 0, dp(4))
        box.layoutParams = params
        return box
    }

    private fun showDailyReport() {
        val date = today()
        val rows = db.transactionsForPeriod(date, nextDate(date))
        showReport("Laporan Harian", date, rows)
    }

    private fun showMonthlyReport() {
        val month = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val rows = db.transactionsForPeriod("$month-01", nextMonth(month))
        showReport("Laporan Bulanan", month, rows)
    }

    private fun showReport(title: String, period: String, rows: List<Transaction>) {
        makeLayout(title)
        val income = rows.filter { it.type == "MASUK" }.sumOf { it.amount }
        val expense = rows.filter { it.type == "KELUAR" }.sumOf { it.amount }
        content.addView(text("Periode: $period", 17f, true))
        content.addView(text("Pemasukan: ${formatMoney(income)}"))
        content.addView(text("Pengeluaran: ${formatMoney(expense)}"))
        content.addView(text("Selisih: ${formatMoney(income - expense)}", 18f, true))
        if (rows.isEmpty()) content.addView(text("Belum ada transaksi pada periode ini."))
        rows.forEach { content.addView(transactionView(it, false)) }
        content.addView(button("Kembali ke Beranda") { showHome() })
    }

    private fun exportPdf() {
        val rows = db.allTransactions().sortedBy { it.date }
        val totals = db.totals()
        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        val paint = android.graphics.Paint().apply { color = Color.BLACK; textSize = 12f }
        var y = 45f
        fun line(value: String, size: Float = 12f, bold: Boolean = false) {
            paint.textSize = size
            paint.typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
            canvas.drawText(value.take(90), 40f, y, paint)
            y += size + 8f
        }
        line("MAMA LELE - LAPORAN BUKU KAS", 18f, true)
        line("Dibuat: ${today()}")
        line("Total pemasukan: ${formatMoney(totals.first)}")
        line("Total pengeluaran: ${formatMoney(totals.second)}")
        line("Saldo: ${formatMoney(totals.first - totals.second)}", 14f, true)
        y += 8f
        line("DAFTAR TRANSAKSI", 14f, true)
        rows.forEach {
            if (y > 780f) {
                pdf.finishPage(page)
                pageNumber++
                page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                y = 45f
                line("MAMA LELE - LANJUTAN", 16f, true)
            }
            val label = "${it.date} | ${it.type} | ${it.note} | ${formatMoney(it.amount)}"
            line(label)
        }
        pdf.finishPage(page)
        try {
            val file = File(getExternalFilesDir(null), "MamaLele_Laporan_${today()}.pdf")
            FileOutputStream(file).use { pdf.writeTo(it) }
            AlertDialog.Builder(this)
                .setTitle("PDF berhasil dibuat")
                .setMessage("File disimpan di:\n${file.absolutePath}\n\nAnda dapat menemukannya melalui pengelola file perangkat pada folder Android/data/id.mamalele.bukukas/files.")
                .setPositiveButton("OK", null).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdf.close()
        }
    }

    private fun formatMoney(value: Long): String =
        "Rp " + NumberFormat.getNumberInstance(Locale("in", "ID")).format(value)

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun nextDate(date: String): String {
        val c = Calendar.getInstance()
        c.time = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date) ?: Date()
        c.add(Calendar.DAY_OF_MONTH, 1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
    }

    private fun nextMonth(month: String): String {
        val c = Calendar.getInstance()
        c.time = SimpleDateFormat("yyyy-MM", Locale.US).parse(month) ?: Date()
        c.add(Calendar.MONTH, 1)
        return SimpleDateFormat("yyyy-MM", Locale.US).format(c.time) + "-01"
    }
}
