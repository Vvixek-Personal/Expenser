import re

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "r") as f:
    content = f.read()

target = """                                                    .background(
                                                        when {
                                                            isSelected && isProfit -> Color(0xFF10B981)
                                                            isSelected && isLoss -> Color(0xFFEF4444)
                                                            isSelected -> SleekPrimary
                                                            isProfit -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                            isLoss -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                                            isToday -> SleekPrimaryContainer.copy(alpha = 0.5f)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = when {
                                                            isSelected -> Color.Transparent
                                                            isProfit -> Color(0xFF10B981).copy(alpha = 0.6f)
                                                            isLoss -> Color(0xFFEF4444).copy(alpha = 0.6f)
                                                            isToday -> SleekPrimary
                                                            else -> Color.Transparent
                                                        },
                                                        shape = RoundedCornerShape(12.dp)
                                                    )"""

replacement = """                                                    .background(
                                                        when {
                                                            isSelected -> SleekPrimary
                                                            isToday -> SleekPrimaryContainer.copy(alpha = 0.5f)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = when {
                                                            isSelected -> Color.Transparent
                                                            isToday -> SleekPrimary
                                                            else -> Color.Transparent
                                                        },
                                                        shape = RoundedCornerShape(12.dp)
                                                    )"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched background and border!")
else:
    print("Could not find target block.")
    
# Also fix the text color so it's not green/red when not selected
target_text_color = """                                                        color = when {
                                                            isSelected -> Color.White
                                                            isProfit -> Color(0xFF10B981)
                                                            isLoss -> Color(0xFFEF4444)
                                                            isToday -> SleekPrimary
                                                            else -> SleekTextPrimary
                                                        }"""

replacement_text_color = """                                                        color = when {
                                                            isSelected -> Color.White
                                                            isToday -> SleekPrimary
                                                            else -> SleekTextPrimary
                                                        }"""

if target_text_color in content:
    content = content.replace(target_text_color, replacement_text_color)
    print("Patched text color!")
else:
    print("Could not find text color block.")

with open("app/src/main/java/com/example/ui/FinanceAppScreen.kt", "w") as f:
    f.write(content)

