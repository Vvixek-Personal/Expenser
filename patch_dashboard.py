import re

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "r") as f:
    content = f.read()

dashboard_meter_pattern = r'        // Dynamic Small Fluid Wave Budget Status Bar\n        SmallFluidBudgetBar\(\n            thisMonthTotal = thisMonthTotal,\n            monthlyBudget = monthlyBudget\n        \)\n        Spacer\(modifier = Modifier\.height\(24\.dp\)\)\n'

if re.search(dashboard_meter_pattern, content):
    content = re.sub(dashboard_meter_pattern, '', content)
    print("Removed SmallFluidBudgetBar from DashboardScreen")
else:
    print("Could not find SmallFluidBudgetBar in DashboardScreen")

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "w") as f:
    f.write(content)
