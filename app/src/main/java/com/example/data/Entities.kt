package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val category: String,
    val date: Long,
    val note: String?,
    val imagePath: String? = null,
    val type: String = "EXPENSE"
)

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val balance: Double,
    val type: String // "CASH", "BANK", "SAVINGS", "CREDIT"
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["accountId"]), Index(value = ["timestamp"]), Index(value = ["category"])]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME", "EXPENSE"
    val category: String,
    val timestamp: Long,
    val accountId: Long,
    val note: String? = null,
    val imagePath: String? = null
)

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["category", "monthYear"], unique = true)]
)
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val amountLimit: Double,
    val monthYear: String // "07-2026"
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Long = 0L,
    val frequency: String = "WEEKLY", // "DAILY", "WEEKLY", "MONTHLY", "MANUAL"
    val contributionAmount: Double = 0.0,
    val isAutoGap: Boolean = true,
    val iconTag: String = "🎮",
    val category: String = "Saving",
    val imageUri: String? = null
)