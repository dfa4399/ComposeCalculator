package com.studyjam.composecalculator

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyjam.composecalculator.ui.theme.ComposeCalculatorTheme
import java.math.BigDecimal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeCalculatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Calculator()
                }
            }
        }
    }
}


data class CalculatorState(
    val num1: String = "0",
    val num2: String = "0",
    val opt: String? = null,
    val lastClickIsOpt: Boolean = false
)

@Composable
fun Calculator() {
    val buttons = arrayOf(
        arrayOf(
            "AC" to MaterialTheme.colors.primaryVariant,
            "+/-" to MaterialTheme.colors.primaryVariant,
            "%" to MaterialTheme.colors.primaryVariant,
            "÷" to MaterialTheme.colors.secondary
        ),
        arrayOf(
            "7" to MaterialTheme.colors.primary,
            "8" to MaterialTheme.colors.primary,
            "9" to MaterialTheme.colors.primary,
            "x" to MaterialTheme.colors.secondary
        ),
        arrayOf(
            "4" to MaterialTheme.colors.primary,
            "5" to MaterialTheme.colors.primary,
            "6" to MaterialTheme.colors.primary,
            "-" to MaterialTheme.colors.secondary
        ),
        arrayOf(
            "1" to MaterialTheme.colors.primary,
            "2" to MaterialTheme.colors.primary,
            "3" to MaterialTheme.colors.primary,
            "+" to MaterialTheme.colors.secondary
        ),
        arrayOf(
            "0" to MaterialTheme.colors.primary,
            "." to MaterialTheme.colors.primary,
            "=" to MaterialTheme.colors.secondary
        )
    )

    var state by remember {
        mutableStateOf(CalculatorState())
    }

    Column(
        Modifier
            .background(MaterialTheme.colors.background)
            .padding(horizontal = 8.dp)
    ) {
        Box(
            Modifier
                .height(200.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = if (state.lastClickIsOpt) state.num1 else state.num2,
                fontSize = 96.sp,
                color = MaterialTheme.colors.onBackground,
                lineHeight = 96.sp
            )
        }
        Column(Modifier.fillMaxSize()) {
            buttons.forEach {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    it.forEach {
                        CalculatorButton(
                            Modifier
                                .weight(if (it.first == "0") 2f else 1f)
                                .aspectRatio(if (it.first == "0") 2f else 1f)
                                .background(it.second), it.first
                        ) {
                            state = calculate(state, it.first)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(modifier: Modifier, symbol: String, onClick: () -> Unit = {}) {
    Box(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .then(modifier)
            .clickable { onClick.invoke() }, contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, fontSize = 48.sp, color = MaterialTheme.colors.onPrimary)
    }
}

fun calculate(curState: CalculatorState, input: String): CalculatorState {
    return when (input) {
        "AC" -> curState.copy(num1 = "0", num2 = "0", opt = null, lastClickIsOpt = false)

        "+/-" -> if (curState.lastClickIsOpt) curState.copy(
            num1 = BigDecimal(curState.num1).multiply(BigDecimal("-1")).stripTrailingZeros().toPlainString(),
        ) else curState.copy(
            num2 = BigDecimal(curState.num2).multiply(BigDecimal("-1")).stripTrailingZeros().toPlainString(),
        )

        "%" -> if (curState.lastClickIsOpt) curState.copy(
            num1 = BigDecimal(curState.num1).divide(BigDecimal("100")).stripTrailingZeros().toPlainString(),
        ) else curState.copy(
            num2 = BigDecimal(curState.num2).divide(BigDecimal("100")).stripTrailingZeros().toPlainString(),
        )

        in "0".."9" -> curState.copy(
            num1 = if (curState.opt == "=") "0" else curState.num1,
            num2 = if (curState.num2 == "0") input else curState.num2 + input,
            lastClickIsOpt = false
        )

        "." -> curState.copy(
            num1 = if (curState.opt == "=") "0" else curState.num1,
            num2 = if (curState.num2.contains(".")) curState.num2 else curState.num2 + input,
            lastClickIsOpt = false
        )

        in arrayOf("+", "-", "x", "÷") -> if (curState.lastClickIsOpt) curState.copy(
            opt = input,
            lastClickIsOpt = true
        ) else when (curState.opt) {
            "+" -> curState.copy(
                num1 = BigDecimal(curState.num1).add(BigDecimal(curState.num2)).stripTrailingZeros()
                    .toPlainString(),
                num2 = "0",
                opt = input,
                lastClickIsOpt = true
            )
            "-" -> curState.copy(
                num1 = BigDecimal(curState.num1).subtract(BigDecimal(curState.num2)).stripTrailingZeros()
                    .toPlainString(),
                num2 = "0",
                opt = input,
                lastClickIsOpt = true
            )
            "x" -> curState.copy(
                num1 = BigDecimal(curState.num1).multiply(BigDecimal(curState.num2)).stripTrailingZeros()
                    .toPlainString(),
                num2 = "0",
                opt = input,
                lastClickIsOpt = true
            )
            "÷" -> curState.copy(
                num1 = try {
                    BigDecimal(curState.num1).divide(BigDecimal(curState.num2), 15, BigDecimal.ROUND_UP)
                        .stripTrailingZeros()
                        .toPlainString()
                } catch (e: Exception) {
                    "0"
                },
                num2 = "0",
                opt = input,
                lastClickIsOpt = true
            )
            else -> curState.copy(
                num1 = curState.num2,
                num2 = "0",
                opt = input,
                lastClickIsOpt = true
            )
        }

        "=" -> when (curState.opt) {
            "+" -> curState.copy(
                num1 = BigDecimal(curState.num1).add(BigDecimal(curState.num2)).stripTrailingZeros()
                    .toPlainString(),
                num2 = "0",
                opt = input,
                lastClickIsOpt = true
            )
            "-" -> curState.copy(
                num1 = BigDecimal(curState.num1).subtract(BigDecimal(curState.num2)).stripTrailingZeros()
                    .toPlainString(),
                num2 = "0",
                opt = input,
                lastClickIsOpt = true
            )
            "x" -> curState.copy(
                num1 = BigDecimal(curState.num1).multiply(BigDecimal(curState.num2)).stripTrailingZeros()
                    .toPlainString(),
                num2 = "0",
                opt = input,
                lastClickIsOpt = true
            )
            "÷" -> curState.copy(
                num1 = try {
                    BigDecimal(curState.num1).divide(BigDecimal(curState.num2), 15, BigDecimal.ROUND_UP)
                        .stripTrailingZeros()
                        .toPlainString()
                } catch (e: Exception) {
                    "0"
                },
                num2 = "0",
                opt = input,
                lastClickIsOpt = true
            )
            else -> curState
        }

        else -> curState
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ComposeCalculatorTheme {
        Calculator()
    }
}