import re

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "r") as f:
    content = f.read()

# 2. Remove insight card call
card_block = r"        // ✨ GEMINI-POWERED DAILY SPENDING INSIGHT CARD\n        DailySpendingInsightCard\(\n            insight = dailyInsight,\n            isLoading = isInsightLoading,\n            lastUpdated = insightLastUpdated,\n            onRefresh = \{ viewModel\.generateDailySpendingInsight\(forceRefresh = true\) \}\n        \)\n        Spacer\(modifier = Modifier\.height\(16\.dp\)\)\n"
content = re.sub(card_block, "", content)

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "w") as f:
    f.write(content)
