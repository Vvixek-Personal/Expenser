package com.example.ui

import com.example.data.Expense
import com.example.data.SavingsGoal

/**
 * Core Financial Logic Extensions
 * These ensure consistent state calculations across Dashboard, Analytics, Calendar, and Savings tabs.
 * "Goal Withdrawal" and "Locked Savings" are system-generated transfer records, 
 * so they are EXCLUDED from real income and real expense metrics to prevent double-counting.
 */

// Revenue: True external income (excludes money moved from savings back to cash)
fun Iterable<Expense>.realIncome(): Double {
    return this.filter { it.type == "INCOME" && it.category != "Goal Withdrawal" }.sumOf { it.amount }
}

// Spend: True external expense (excludes money moved from cash to savings)
fun Iterable<Expense>.realExpense(): Double {
    return this.filter { it.type != "INCOME" && it.category != "Locked Savings" }.sumOf { it.amount }
}

// Cash Flow (Available Cash): All money in vs all money out (including savings transfers)
// This is your actual wallet/bank balance available to spend.
fun Iterable<Expense>.availableCash(): Double {
    val totalIn = this.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalOut = this.filter { it.type != "INCOME" }.sumOf { it.amount }
    return totalIn - totalOut
}

// Total Savings Balance
fun Iterable<SavingsGoal>.totalSavings(): Double {
    return this.sumOf { it.currentAmount }
}

// Net Worth: Available Cash + Total Savings Balance
fun Iterable<Expense>.netWorth(savingsGoals: Iterable<SavingsGoal>): Double {
    return this.availableCash() + savingsGoals.totalSavings()
}
