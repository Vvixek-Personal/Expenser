import re

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "r") as f:
    content = f.read()

start_str = "        // 📈 1. ANIMATED GRAPH FOR INCOME AND EXPENSE"
end_str = "        Spacer(modifier = Modifier.height(110.dp))"

start_idx = content.find(start_str)
end_idx = content.find(end_str, start_idx)

if start_idx != -1 and end_idx != -1:
    new_text = """        // 💎 NET WORTH OVER TIME CHART CARD
        NetWorthOverTimeChartCard(
            allExpenses = allExpenses,
            accounts = accounts,
            selectedTimeFilter = selectedTimeFilter,
            currencySymbol = currencySymbol
        )
"""
    content = content[:start_idx] + new_text + content[end_idx:]
    print("Replaced successfully.")
else:
    print("Could not find boundaries.")

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "w") as f:
    f.write(content)
