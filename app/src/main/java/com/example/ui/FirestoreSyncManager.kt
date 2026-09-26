package com.example.ui

import android.content.Context
import android.util.Log
import com.example.data.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SyncResult {
    data class Success(val message: String, val syncedCount: Int) : SyncResult()
    data class Error(val errorMessage: String) : SyncResult()
}

object FirestoreSyncManager {
    private const val TAG = "FirestoreSyncManager"

    fun getFirestore(context: Context): FirebaseFirestore {
        FirebaseAuthManager.getFirebaseAuth(context)
        return FirebaseFirestore.getInstance()
    }

    suspend fun syncDataToCloud(
        context: Context,
        userId: String,
        repository: FinanceRepository
    ): SyncResult = withContext(Dispatchers.IO) {
        try {
            val db = getFirestore(context)
            val userDocRef = db.collection("users").document(userId)

            val expenses = repository.allExpenses.first()
            val transactions = repository.allTransactions.first()
            val budgets = repository.allBudgets.first()
            val savingsGoals = repository.allSavingsGoals.first()
            val accounts = repository.allAccounts.first()
            val reminders = repository.allReminders.first()

            var totalCount = 0

            // 1. Transactions collection
            for (t in transactions) {
                val data = hashMapOf(
                    "id" to t.id,
                    "title" to t.title,
                    "amount" to t.amount,
                    "type" to t.type,
                    "category" to t.category,
                    "timestamp" to t.timestamp,
                    "accountId" to t.accountId,
                    "note" to (t.note ?: ""),
                    "imagePath" to (t.imagePath ?: "")
                )
                userDocRef.collection("transactions").document(t.id.toString()).set(data, SetOptions.merge()).awaitTask()
                totalCount++
            }

            // 2. Expenses collection
            for (e in expenses) {
                val data = hashMapOf(
                    "id" to e.id,
                    "amount" to e.amount,
                    "category" to e.category,
                    "date" to e.date,
                    "note" to (e.note ?: ""),
                    "imagePath" to (e.imagePath ?: ""),
                    "type" to e.type
                )
                userDocRef.collection("expenses").document(e.id.toString()).set(data, SetOptions.merge()).awaitTask()
                totalCount++
            }

            // 3. Budgets collection
            for (b in budgets) {
                val data = hashMapOf(
                    "id" to b.id,
                    "category" to b.category,
                    "amountLimit" to b.amountLimit,
                    "monthYear" to b.monthYear
                )
                userDocRef.collection("budgets").document("${b.category}_${b.monthYear}").set(data, SetOptions.merge()).awaitTask()
                totalCount++
            }

            // 4. Savings Goals collection
            for (g in savingsGoals) {
                val data = hashMapOf(
                    "id" to g.id,
                    "name" to g.name,
                    "targetAmount" to g.targetAmount,
                    "currentAmount" to g.currentAmount,
                    "targetDate" to g.targetDate,
                    "frequency" to g.frequency,
                    "contributionAmount" to g.contributionAmount,
                    "isAutoGap" to g.isAutoGap,
                    "iconTag" to g.iconTag,
                    "category" to g.category,
                    "imageUri" to (g.imageUri ?: "")
                )
                userDocRef.collection("savings_goals").document(g.id.toString()).set(data, SetOptions.merge()).awaitTask()
                totalCount++
            }

            // 5. Accounts collection
            for (a in accounts) {
                val data = hashMapOf(
                    "id" to a.id,
                    "name" to a.name,
                    "balance" to a.balance,
                    "type" to a.type
                )
                userDocRef.collection("accounts").document(a.id.toString()).set(data, SetOptions.merge()).awaitTask()
                totalCount++
            }

            // 6. Reminders collection
            for (r in reminders) {
                val data = hashMapOf(
                    "id" to r.id,
                    "text" to r.text,
                    "dueDate" to r.dueDate,
                    "isCompleted" to r.isCompleted,
                    "isEnabled" to r.isEnabled
                )
                userDocRef.collection("reminders").document(r.id.toString()).set(data, SetOptions.merge()).awaitTask()
                totalCount++
            }

            // Sync metadata
            val syncMeta = hashMapOf(
                "lastSyncedAt" to System.currentTimeMillis(),
                "totalRecords" to totalCount,
                "version" to "1.27"
            )
            userDocRef.set(syncMeta, SetOptions.merge()).awaitTask()

            Log.d(TAG, "Uploaded $totalCount items to Firestore for user $userId")
            SyncResult.Success("Cloud backup updated ($totalCount records)", totalCount)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload to Firestore", e)
            SyncResult.Error(e.localizedMessage ?: "Failed to sync data to Firestore")
        }
    }

    suspend fun syncDataFromCloud(
        context: Context,
        userId: String,
        repository: FinanceRepository
    ): SyncResult = withContext(Dispatchers.IO) {
        try {
            val db = getFirestore(context)
            val userDocRef = db.collection("users").document(userId)

            var importedCount = 0

            // 1. Transactions
            val txSnap = userDocRef.collection("transactions").get().awaitTask()
            val txList = txSnap.documents.mapNotNull { doc ->
                try {
                    Transaction(
                        id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
                        title = doc.getString("title") ?: "Transaction",
                        amount = doc.getDouble("amount") ?: 0.0,
                        type = doc.getString("type") ?: "EXPENSE",
                        category = doc.getString("category") ?: "Other",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        accountId = doc.getLong("accountId") ?: 1L,
                        note = doc.getString("note"),
                        imagePath = doc.getString("imagePath")
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (txList.isNotEmpty()) {
                repository.insertTransactions(txList)
                importedCount += txList.size
            }

            // 2. Expenses
            val expSnap = userDocRef.collection("expenses").get().awaitTask()
            val expList = expSnap.documents.mapNotNull { doc ->
                try {
                    Expense(
                        id = (doc.getLong("id") ?: doc.id.toIntOrNull() ?: 0).toInt(),
                        amount = doc.getDouble("amount") ?: 0.0,
                        category = doc.getString("category") ?: "Other",
                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                        note = doc.getString("note"),
                        imagePath = doc.getString("imagePath"),
                        type = doc.getString("type") ?: "EXPENSE"
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (expList.isNotEmpty()) {
                repository.insertExpenses(expList)
                importedCount += expList.size
            }

            // 3. Budgets
            val bgSnap = userDocRef.collection("budgets").get().awaitTask()
            val bgList = bgSnap.documents.mapNotNull { doc ->
                try {
                    Budget(
                        id = doc.getLong("id") ?: 0L,
                        category = doc.getString("category") ?: "General",
                        amountLimit = doc.getDouble("amountLimit") ?: 0.0,
                        monthYear = doc.getString("monthYear") ?: "09-2026"
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (bgList.isNotEmpty()) {
                repository.insertBudgets(bgList)
                importedCount += bgList.size
            }

            // 4. Savings Goals
            val sgSnap = userDocRef.collection("savings_goals").get().awaitTask()
            val sgList = sgSnap.documents.mapNotNull { doc ->
                try {
                    SavingsGoal(
                        id = doc.getLong("id") ?: 0L,
                        name = doc.getString("name") ?: "Goal",
                        targetAmount = doc.getDouble("targetAmount") ?: 100.0,
                        currentAmount = doc.getDouble("currentAmount") ?: 0.0,
                        targetDate = doc.getLong("targetDate") ?: 0L,
                        frequency = doc.getString("frequency") ?: "WEEKLY",
                        contributionAmount = doc.getDouble("contributionAmount") ?: 0.0,
                        isAutoGap = doc.getBoolean("isAutoGap") ?: true,
                        iconTag = doc.getString("iconTag") ?: "🎯",
                        category = doc.getString("category") ?: "Saving",
                        imageUri = doc.getString("imageUri")
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (sgList.isNotEmpty()) {
                repository.insertSavingsGoals(sgList)
                importedCount += sgList.size
            }

            // 5. Accounts
            val accSnap = userDocRef.collection("accounts").get().awaitTask()
            val accList = accSnap.documents.mapNotNull { doc ->
                try {
                    Account(
                        id = doc.getLong("id") ?: 0L,
                        name = doc.getString("name") ?: "Cash",
                        balance = doc.getDouble("balance") ?: 0.0,
                        type = doc.getString("type") ?: "CASH"
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (accList.isNotEmpty()) {
                repository.insertAccounts(accList)
                importedCount += accList.size
            }

            // 6. Reminders
            val remSnap = userDocRef.collection("reminders").get().awaitTask()
            for (doc in remSnap.documents) {
                try {
                    val rem = ReminderEntity(
                        id = doc.getLong("id") ?: 0L,
                        text = doc.getString("text") ?: "Reminder",
                        dueDate = doc.getLong("dueDate") ?: System.currentTimeMillis(),
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        isEnabled = doc.getBoolean("isEnabled") ?: true
                    )
                    repository.insertReminder(rem)
                    importedCount++
                } catch (e: Exception) {
                    // skip malformed
                }
            }

            Log.d(TAG, "Restored $importedCount items from Firestore for user $userId")
            SyncResult.Success("Cloud data synced ($importedCount records)", importedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore from Firestore", e)
            SyncResult.Error(e.localizedMessage ?: "Failed to download from Firestore")
        }
    }

    suspend fun syncBidirectional(
        context: Context,
        userId: String,
        repository: FinanceRepository
    ): SyncResult = withContext(Dispatchers.IO) {
        val pushResult = syncDataToCloud(context, userId, repository)
        val pullResult = syncDataFromCloud(context, userId, repository)

        if (pushResult is SyncResult.Error && pullResult is SyncResult.Error) {
            pushResult
        } else {
            val total = (if (pushResult is SyncResult.Success) pushResult.syncedCount else 0) +
                    (if (pullResult is SyncResult.Success) pullResult.syncedCount else 0)
            SyncResult.Success("Cloud synchronization complete ($total items synced)", total)
        }
    }
}
