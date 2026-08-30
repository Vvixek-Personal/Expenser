import re

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "r") as f:
    content = f.read()

# 1. Update AnalyticsScreen to remove unwanted charts
old_analytics_content = """        // 📈 1. ANIMATED GRAPH FOR INCOME AND EXPENSE
        IncomeExpenseLineGraphCard(
            expenses = filteredPeriodExpenses,
            selectedTimeFilter = selectedTimeFilter
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 💎 1.5 NET WORTH OVER TIME CHART CARD
        NetWorthOverTimeChartCard(
            allExpenses = allExpenses,
            accounts = accounts,
            selectedTimeFilter = selectedTimeFilter,
            currencySymbol = currencySymbol
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 💵 CASH AT END OF THE MONTH CHART CARD
        CashAtEndOfMonthChartCard(
            allExpenses = allExpenses,
            currencySymbol = currencySymbol
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 📊 2. BAR CHART FOR CATEGORY OVER INCOME AND EXPENSE
        CategoryIncomeExpenseBarChartCard(
            expenses = filteredPeriodExpenses
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 🥧 3. PIE CHART / DONUT CHART
        CategoryPieChartCard(
            expenses = filteredPeriodExpenses
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 🎯 4. CATEGORY BUDGET PROGRESS & TRACKING CARD
        CategoryBudgetProgressCard(
            viewModel = viewModel,
            budgets = budgets,
            allExpenses = allExpenses,
            currencySymbol = currencySymbol
        )
        Spacer(modifier = Modifier.height(110.dp))"""

new_analytics_content = """        // 💎 1.5 NET WORTH OVER TIME CHART CARD
        NetWorthOverTimeChartCard(
            allExpenses = allExpenses,
            accounts = accounts,
            selectedTimeFilter = selectedTimeFilter,
            currencySymbol = currencySymbol
        )
        Spacer(modifier = Modifier.height(110.dp))"""

if old_analytics_content in content:
    content = content.replace(old_analytics_content, new_analytics_content)
else:
    print("WARNING: Could not find old_analytics_content")

# 2. Remove function definitions for the charts.
# We will use regex to remove everything from @Composable fun FunctionName( to the next // ========== or next @Composable

def remove_function(func_name, content):
    pattern = re.compile(r'(?:// ==========================================\n//.*?\n// ==========================================\n)?@Composable\s*\nfun ' + func_name + r'\b.*?\n}\n', re.DOTALL)
    # We might have multiple nested braces, so regex is tricky for entire function body.
    # Instead, we can find the start and then match braces.
    return content

def remove_function_brace_matching(func_name, text):
    start_pattern = r'(?://[^\n]*\n)*@Composable\s*\nfun ' + func_name + r'\b'
    match = re.search(start_pattern, text)
    if not match:
        print(f"Function {func_name} not found.")
        return text
    start_idx = match.start()
    
    # Now find the opening brace {
    brace_start = text.find('{', match.end())
    if brace_start == -1:
        return text
    
    open_braces = 0
    end_idx = -1
    for i in range(brace_start, len(text)):
        if text[i] == '{':
            open_braces += 1
        elif text[i] == '}':
            open_braces -= 1
            if open_braces == 0:
                end_idx = i + 1
                break
                
    if end_idx != -1:
        # Also remove trailing newlines
        while end_idx < len(text) and text[end_idx] == '\n':
            end_idx += 1
        print(f"Removed {func_name}")
        return text[:start_idx] + text[end_idx:]
    return text

for func in ["IncomeExpenseLineGraphCard", "CashAtEndOfMonthChartCard", "CategoryIncomeExpenseBarChartCard", "CategoryPieChartCard", "CategoryBudgetProgressCard"]:
    content = remove_function_brace_matching(func, content)

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "w") as f:
    f.write(content)

