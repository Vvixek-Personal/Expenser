package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.FinanceDatabase
import com.example.data.FinanceRepository
import com.example.ui.FinanceAppScreen
import com.example.ui.FinanceViewModel
import com.example.ui.FinanceViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = FinanceDatabase.getDatabase(applicationContext)
    val dao = database.financeDao()
    val repository = FinanceRepository(dao)

    val viewModel: FinanceViewModel by viewModels {
      FinanceViewModelFactory(application, repository)
    }

    setContent {
      MyApplicationTheme {
        FinanceAppScreen(viewModel = viewModel)
      }
    }
  }
}
