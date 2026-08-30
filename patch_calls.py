import re

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "r") as f:
    content = f.read()

pattern = re.compile(r'        // 📈 1\. ANIMATED GRAPH FOR INCOME AND EXPENSE\n        IncomeExpenseLineGraphCard\(\n            expenses = filteredPeriodExpenses,\n            selectedTimeFilter = selectedTimeFilter\n        \)\n        Spacer\(modifier = Modifier\.height\(16\.dp\)\)\n\n        // 💎 1\.5 NET WORTH OVER TIME CHART CARD\n        NetWorthOverTimeChartCard\(\n            allExpenses = allExpenses,\n            accounts = accounts,\n            selectedTimeFilter = selectedTimeFilter,\n            currencySymbol = currencySymbol\n        \)\n        Spacer\(modifier = Modifier\.height\(16\.dp\)\)\n\n        // 💵 CASH AT END OF THE MONTH CHART CARD\n        CashAtEndOfMonthChartCard\(\n            allExpenses = allExpenses,\n            currencySymbol = currencySymbol\n        \)\n        Spacer\(modifier = Modifier\.height\(16\.dp\)\)\n\n        // 📊 2\. BAR CHART FOR CATEGORY OVER INCOME AND EXPENSE\n        CategoryIncomeExpenseBarChartCard\(\n            expenses = filteredPeriodExpenses\n        \)\n        Spacer\(modifier = Modifier\.height\(16\.dp\)\)\n\n        // 🥧 3\. PIE CHART / DONUT CHART\n        CategoryPieChartCard\(\n            expenses = filteredPeriodExpenses\n        \)\n        Spacer\(modifier = Modifier\.height\(16\.dp\)\)\n\n        // 🎯 4\. CATEGORY BUDGET PROGRESS & TRACKING CARD\n        CategoryBudgetProgressCard\(\n            viewModel = viewModel,\n            budgets = budgets,\n            allExpenses = allExpenses,\n            currencySymbol = currencySymbol\n        \)\n        Spacer\(modifier = Modifier\.height\(110\.dp\)\)', re.DOTALL)

new_text = """        // 💎 1.5 NET WORTH OVER TIME CHART CARD
        NetWorthOverTimeChartCard(
            allExpenses = allExpenses,
            accounts = accounts,
            selectedTimeFilter = selectedTimeFilter,
            currencySymbol = currencySymbol
        )
        Spacer(modifier = Modifier.height(110.dp))"""

if pattern.search(content):
    content = pattern.sub(new_text, content)
    print("Replaced!")
else:
    print("Pattern not found!")

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "w") as f:
    f.write(content)
