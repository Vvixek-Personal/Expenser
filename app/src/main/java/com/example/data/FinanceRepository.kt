package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: FinanceDao) {
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    val allAccounts: Flow<List<Account>> = dao.getAllAccounts()

    suspend fun insertExpense(expense: Expense): Long = dao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = dao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

    val allTransactions: Flow<List<Transaction>> = dao.getAllTransactions()
    val allBudgets: Flow<List<Budget>> = dao.getAllBudgets()
    val allSavingsGoals: Flow<List<SavingsGoal>> = dao.getAllSavingsGoals()

    suspend fun getAccountById(id: Int): Account? = dao.getAccountById(id)

    suspend fun insertAccount(account: Account): Long = dao.insertAccount(account)
    suspend fun updateAccount(account: Account) = dao.updateAccount(account)
    suspend fun deleteAccount(account: Account) = dao.deleteAccount(account)

    suspend fun insertTransaction(transaction: Transaction): Long {
        // Adjust account balance
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

    suspend fun deleteTransaction(transaction: Transaction) {
        // Reverse account balance adjustment
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

    suspend fun insertBudget(budget: Budget): Long = dao.insertBudget(budget)
    suspend fun updateBudget(budget: Budget) = dao.updateBudget(budget)
    suspend fun deleteBudget(budget: Budget) = dao.deleteBudget(budget)

    suspend fun insertSavingsGoal(goal: SavingsGoal): Long = dao.insertSavingsGoal(goal)
    suspend fun updateSavingsGoal(goal: SavingsGoal) = dao.updateSavingsGoal(goal)
    suspend fun deleteSavingsGoal(goal: SavingsGoal) = dao.deleteSavingsGoal(goal)

    // Transfer money from account to savings goal
    suspend fun allocateToSavingsGoal(goal: SavingsGoal, account: Account, amount: Double): Boolean {
        if (account.balance < amount) return false // Insufficient funds
        
        // Deduct from account
        val updatedAccount = account.copy(balance = account.balance - amount)
        dao.updateAccount(updatedAccount)
        
        // Add to savings goal
        val updatedGoal = goal.copy(currentAmount = goal.currentAmount + amount)
        dao.updateSavingsGoal(updatedGoal)
        
        // Add a system transaction recording this transfer
        val transferTx = Transaction(
            title = "Allocated to: ${goal.name}",
            amount = amount,
            type = "EXPENSE",
            category = "Savings Transfer",
            timestamp = System.currentTimeMillis(),
            accountId = account.id
        )
        dao.insertTransaction(transferTx)
        return true
    }

    // Withdraw money from savings goal back to account
    suspend fun withdrawFromSavingsGoal(goal: SavingsGoal, account: Account, amount: Double): Boolean {
        if (goal.currentAmount < amount) return false // Insufficient savings
        
        // Add back to account
        val updatedAccount = account.copy(balance = account.balance + amount)
        dao.updateAccount(updatedAccount)
        
        // Deduct from goal
        val updatedGoal = goal.copy(currentAmount = goal.currentAmount - amount)
        dao.updateSavingsGoal(updatedGoal)
        
        // Add a system transaction recording this withdrawal
        val transferTx = Transaction(
            title = "Withdrawn from: ${goal.name}",
            amount = amount,
            type = "INCOME",
            category = "Savings Withdrawal",
            timestamp = System.currentTimeMillis(),
            accountId = account.id
        )
        dao.insertTransaction(transferTx)
        return true
    }
}
