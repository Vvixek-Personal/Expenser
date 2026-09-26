package com.example.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migrations for FinanceDatabase.
 *
 * Reconciles database schemas from earlier versions (1-6) to the current Version 7 shape.
 * Older installs shipped before schema export was enabled are safely migrated in place
 * without destructive wipe.
 */

private fun SupportSQLiteDatabase.addColumnIfNotExists(table: String, column: String, columnDef: String) {
    try {
        val cursor = query("PRAGMA table_info(`$table`)")
        var exists = false
        cursor.use {
            val nameIndex = it.getColumnIndex("name")
            while (it.moveToNext()) {
                if (nameIndex != -1 && it.getString(nameIndex).equals(column, ignoreCase = true)) {
                    exists = true
                    break
                }
            }
        }
        if (!exists) {
            execSQL("ALTER TABLE `$table` ADD COLUMN `$column` $columnDef")
        }
    } catch (_: Exception) {}
}

private fun reconcileDatabaseSchema(db: SupportSQLiteDatabase) {
    // 1. expenses table
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `expenses` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `amount` REAL NOT NULL,
            `category` TEXT NOT NULL,
            `date` INTEGER NOT NULL,
            `note` TEXT,
            `imagePath` TEXT,
            `type` TEXT NOT NULL DEFAULT 'EXPENSE'
        )
        """.trimIndent()
    )
    db.addColumnIfNotExists("expenses", "imagePath", "TEXT")
    db.addColumnIfNotExists("expenses", "type", "TEXT NOT NULL DEFAULT 'EXPENSE'")

    // 2. accounts table
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `accounts` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `name` TEXT NOT NULL,
            `balance` REAL NOT NULL,
            `type` TEXT NOT NULL
        )
        """.trimIndent()
    )

    // 3. transactions table
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `transactions` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `title` TEXT NOT NULL,
            `amount` REAL NOT NULL,
            `type` TEXT NOT NULL,
            `category` TEXT NOT NULL,
            `timestamp` INTEGER NOT NULL,
            `accountId` INTEGER NOT NULL,
            `note` TEXT,
            `imagePath` TEXT,
            FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent()
    )
    db.addColumnIfNotExists("transactions", "note", "TEXT")
    db.addColumnIfNotExists("transactions", "imagePath", "TEXT")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_accountId` ON `transactions` (`accountId`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_timestamp` ON `transactions` (`timestamp`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_category` ON `transactions` (`category`)")

    // 4. budgets table
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `budgets` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `category` TEXT NOT NULL,
            `amountLimit` REAL NOT NULL,
            `monthYear` TEXT NOT NULL
        )
        """.trimIndent()
    )
    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_category_monthYear` ON `budgets` (`category`, `monthYear`)")

    // 5. savings_goals table
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `savings_goals` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `name` TEXT NOT NULL,
            `targetAmount` REAL NOT NULL,
            `currentAmount` REAL NOT NULL DEFAULT 0.0,
            `targetDate` INTEGER NOT NULL DEFAULT 0,
            `frequency` TEXT NOT NULL DEFAULT 'WEEKLY',
            `contributionAmount` REAL NOT NULL DEFAULT 0.0,
            `isAutoGap` INTEGER NOT NULL DEFAULT 1,
            `iconTag` TEXT NOT NULL DEFAULT '🎮',
            `category` TEXT NOT NULL DEFAULT 'Saving',
            `imageUri` TEXT
        )
        """.trimIndent()
    )
    db.addColumnIfNotExists("savings_goals", "currentAmount", "REAL NOT NULL DEFAULT 0.0")
    db.addColumnIfNotExists("savings_goals", "targetDate", "INTEGER NOT NULL DEFAULT 0")
    db.addColumnIfNotExists("savings_goals", "frequency", "TEXT NOT NULL DEFAULT 'WEEKLY'")
    db.addColumnIfNotExists("savings_goals", "contributionAmount", "REAL NOT NULL DEFAULT 0.0")
    db.addColumnIfNotExists("savings_goals", "isAutoGap", "INTEGER NOT NULL DEFAULT 1")
    db.addColumnIfNotExists("savings_goals", "iconTag", "TEXT NOT NULL DEFAULT '🎮'")
    db.addColumnIfNotExists("savings_goals", "category", "TEXT NOT NULL DEFAULT 'Saving'")
    db.addColumnIfNotExists("savings_goals", "imageUri", "TEXT")
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        reconcileDatabaseSchema(db)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        reconcileDatabaseSchema(db)
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        reconcileDatabaseSchema(db)
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        reconcileDatabaseSchema(db)
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        reconcileDatabaseSchema(db)
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        reconcileDatabaseSchema(db)
    }
}

val FINANCE_DB_MIGRATIONS: Array<Migration> = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7
)
