import re

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "r") as f:
    content = f.read()

var_block = """    val dailyInsight by viewModel.dailySpendingInsight.collectAsStateWithLifecycle()
    val isInsightLoading by viewModel.isInsightLoading.collectAsStateWithLifecycle()
    val insightLastUpdated by viewModel.insightLastUpdated.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.generateDailySpendingInsight(forceRefresh = false)
    }"""
content = content.replace(var_block, "")

card_block = """        // ✨ GEMINI-POWERED DAILY SPENDING INSIGHT CARD
        DailySpendingInsightCard(
            insight = dailyInsight,
            isLoading = isInsightLoading,
            lastUpdated = insightLastUpdated,
            onRefresh = { viewModel.generateDailySpendingInsight(forceRefresh = true) }
        )
        Spacer(modifier = Modifier.height(16.dp))"""
content = content.replace(card_block, "")

chart_call_target = """        // 💎 NET WORTH OVER TIME CHART CARD
        NetWorthOverTimeChartCard(
            allExpenses = allExpenses,
            accounts = accounts,"""
chart_call_replacement = """        // 💎 NET WORTH OVER TIME CHART CARD
        NetWorthOverTimeChartCard(
            allExpenses = filteredPeriodExpenses,
            accounts = accounts,"""
content = content.replace(chart_call_target, chart_call_replacement)

# Remove DailySpendingInsightCard function
def remove_function_brace_matching(func_name, text):
    start_pattern = r'(?://[^\n]*\n)*@Composable\s*\nfun ' + func_name + r'\b'
    match = re.search(start_pattern, text)
    if not match:
        print(f"Function {func_name} not found.")
        return text
    start_idx = match.start()
    
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
        while end_idx < len(text) and text[end_idx] == '\n':
            end_idx += 1
        print(f"Removed {func_name}")
        return text[:start_idx] + text[end_idx:]
    return text

content = remove_function_brace_matching("DailySpendingInsightCard", content)

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "w") as f:
    f.write(content)
