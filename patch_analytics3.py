import re

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "r") as f:
    content = f.read()

start_str = "        // ✨ GEMINI-POWERED DAILY SPENDING INSIGHT CARD"
end_str = "        // Metrics Overview Cards Row (App Default Style)"

start_idx = content.find(start_str)
end_idx = content.find(end_str)

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + content[end_idx:]
    print("Removed from analytics")
else:
    print("Failed")

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "w") as f:
    f.write(content)
