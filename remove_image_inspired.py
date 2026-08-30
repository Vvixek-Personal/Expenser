import re

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "r") as f:
    text = f.read()

func_name = "ImageInspiredAnalyticsChartCard"
start_pattern = r'(?://[^\n]*\n)*@Composable\s*\nfun ' + func_name + r'\b'
match = re.search(start_pattern, text)
if match:
    start_idx = match.start()
    brace_start = text.find('{', match.end())
    if brace_start != -1:
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
            text = text[:start_idx] + text[end_idx:]

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "w") as f:
    f.write(text)

