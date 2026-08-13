package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: FinanceDao) {

    // --- Expenses ---
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    suspend fun getExpenseById(id: Int): Expense? = dao.getExpenseById(id)
    suspend fun insertExpense(expense: Expense): Long = dao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = dao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)
    suspend fun deleteExpenseById(id: Int) = dao.deleteExpenseById(id)

    // --- Accounts ---
    val allAccounts: Flow<List<Account>> = dao.getAllAccounts()
    suspend fun getAccountById(id: Long): Account? = dao.getAccountById(id)
    suspend fun insertAccount(account: Account): Long = dao.insertAccount(account)
    suspend fun updateAccount(account: Account) = dao.updateAccount(account)
    suspend fun deleteAccount(account: Account) = dao.deleteAccount(account)

    // --- Transactions ---
    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()
    
    fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>> =
        dao.getTransactionsByAccount(accountId)

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        dao.getTransactionsByDateRange(startDate, endDate)

    suspend fun insertTransaction(transaction: Transaction): Long {
        val account = dao.getAccountById(transaction.accountId)
        if (account != null) {
            val newBalance = if (transaction.type == "INCOME") {
                account.balance + transaction.amount
            } else {
                account.balance - transaction.amount
            }
            dao.updateAccount(account.copy(balance = newBalance))
        }
        return dao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) = dao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: Transaction) {
        val account = dao.getAccountById(transaction.accountId)
        if (account != null) {
            val newBalance = if (transaction.type == "INCOME") {
                account.balance - transaction.amount
            } else {
                account.balance + transaction.amount
            }
            dao.updateAccount(account.copy(balance = newBalance))
        }
        dao.deleteTransaction(transaction)
    }

    // --- Budgets ---
    val allBudgets: Flow<List<Budget>> = dao.getAllBudgets()
    fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>> = dao.getBudgetsForMonth(monthYear)
    suspend fun insertBudget(budget: Budget): Long = dao.insertBudget(budget)
    suspend fun updateBudget(budget: Budget) = dao.updateBudget(budget)
    suspend fun deleteBudget(budget: Budget) = dao.deleteBudget(budget)

    // --- Savings Goals ---
    val allSavingsGoals: Flow<List<SavingsGoal>> = dao.getAllSavingsGoals()
    suspend fun insertSavingsGoal(goal: SavingsGoal): Long = dao.insertSavingsGoal(goal)
    suspend fun updateSavingsGoal(goal: SavingsGoal) = dao.updateSavingsGoal(goal)
    suspend fun deleteSavingsGoal(goal: SavingsGoal) = dao.deleteSavingsGoal(goal)

    // --- Transfers & Allocations ---
    suspend fun allocateToSavingsGoal(goal: SavingsGoal, account: Account, amount: Double): Boolean {
        if (account.balance < amount) return false
        
        dao.updateAccount(account.copy(balance = account.balance - amount))
        dao.updateSavingsGoal(goal.copy(currentAmount = goal.currentAmount + amount))
        
        insertTransaction(
            Transaction(
                title = "Allocated to: ${goal.name}",
                amount = amount,
                type = "EXPENSE",
                category = "Savings Transfer",
                timestamp = System.currentTimeMillis(),
                accountId = account.id
            )
        )
        return true
    }

    suspend fun withdrawFromSavingsGoal(goal: SavingsGoal, account: Account, amount: Double): Boolean {
        if (goal.currentAmount < amount) return false
        
        dao.updateAccount(account.copy(balance = account.balance + amount))
        dao.updateSavingsGoal(goal.copy(currentAmount = goal.currentAmount - amount))
        
        insertTransaction(
            Transaction(
                title = "Withdrawn from: ${goal.name}",
                amount = amount,
                type = "INCOME",
                category = "Savings Withdrawal",
                timestamp = System.currentTimeMillis(),
                accountId = account.id
            )
        )
        return true
    }

    suspend fun restoreAllData(
        expenses: List<Expense>,
        accounts: List<Account>,
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>
    ) {
        dao.deleteAllTransactions()
        dao.deleteAllExpenses()
        dao.deleteAllAccounts()
        dao.deleteAllBudgets()
        dao.deleteAllSavingsGoals()

        if (accounts.isNotEmpty()) dao.insertAccounts(accounts)
        if (expenses.isNotEmpty()) dao.insertExpenses(expenses)
        if (transactions.isNotEmpty()) dao.insertTransactions(transactions)
        if (budgets.isNotEmpty()) dao.insertBudgets(budgets)
        if (goals.isNotEmpty()) dao.insertSavingsGoals(goals)
    }
}