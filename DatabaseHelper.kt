package id.mamalele.bukukas

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "mamalele.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                type TEXT NOT NULL,
                note TEXT NOT NULL,
                amount INTEGER NOT NULL
            )""".trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun addTransaction(date: String, type: String, note: String, amount: Long): Long {
        val values = ContentValues().apply {
            put("date", date)
            put("type", type)
            put("note", note)
            put("amount", amount)
        }
        return writableDatabase.insert("transactions", null, values)
    }

    fun allTransactions(): List<Transaction> {
        val result = mutableListOf<Transaction>()
        readableDatabase.rawQuery(
            "SELECT id, date, type, note, amount FROM transactions ORDER BY date DESC, id DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                result.add(
                    Transaction(
                        c.getLong(0), c.getString(1), c.getString(2),
                        c.getString(3), c.getLong(4)
                    )
                )
            }
        }
        return result
    }

    fun totals(): Pair<Long, Long> {
        var income = 0L
        var expense = 0L
        readableDatabase.rawQuery(
            "SELECT type, SUM(amount) FROM transactions GROUP BY type", null
        ).use { c ->
            while (c.moveToNext()) {
                if (c.getString(0) == "MASUK") income = c.getLong(1)
                else expense = c.getLong(1)
            }
        }
        return Pair(income, expense)
    }

    fun transactionsForPeriod(start: String, endExclusive: String): List<Transaction> {
        val result = mutableListOf<Transaction>()
        readableDatabase.rawQuery(
            """SELECT id, date, type, note, amount FROM transactions
               WHERE date >= ? AND date < ? ORDER BY date ASC, id ASC""".trimIndent(),
            arrayOf(start, endExclusive)
        ).use { c ->
            while (c.moveToNext()) {
                result.add(Transaction(c.getLong(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getLong(4)))
            }
        }
        return result
    }

    fun deleteTransaction(id: Long): Int =
        writableDatabase.delete("transactions", "id=?", arrayOf(id.toString()))
}
