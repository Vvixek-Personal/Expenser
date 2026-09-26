package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Expense::class,
        Account::class,
        Transaction::class,
        Budget::class,
        SavingsGoal::class,
        ReminderEntity::class
    ],
    version = 8,
    exportSchema = true
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                // Recovery path for existing installs on versions 1-6 (shipped
                // before schema export was turned on): rebuild any missing/older
                // table shape to match the current entities without deleting rows.
                // See FinanceMigrations.kt for why this exists and what it does.
                .addMigrations(*FINANCE_DB_MIGRATIONS)
                // Kept only as a last-resort safety net for a schema state the
                // recovery migrations above can't reconcile (e.g. someone was on
                // an even older, unknown version). Every version step Room will
                // actually hit for existing users (1->2 ... 6->7) is now covered
                // by a real migration above, so this should no longer fire for
                // any currently-installed version of the app.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}