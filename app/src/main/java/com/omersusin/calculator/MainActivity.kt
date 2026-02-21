package com.omersusin.calculator

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.omersusin.calculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit binding: ActivityMainBinding
    private var currentExpression = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        val buttons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9,
            binding.btnDot, binding.btnPlus, binding.btnMinus, binding.btnMultiply, binding.btnDivide
        )

        for (btn in buttons) {
            btn.setOnClickListener {
                appendExpression((it as Button).text.toString())
            }
        }

        binding.btnAC.setOnClickListener {
            currentExpression = ""
            updateUI()
            binding.tvPreview.text = ""
        }

        binding.btnBackspace.setOnClickListener {
            if (currentExpression.isNotEmpty()) {
                currentExpression = currentExpression.dropLast(1)
                updateUI()
            }
        }

        binding.btnSign.setOnClickListener {
            if (currentExpression.isNotEmpty()) {
                currentExpression = if (currentExpression.startsWith("-")) {
                    currentExpression.substring(1)
                } else {
                    "-$currentExpression"
                }
                updateUI()
            }
        }

        binding.btnPercent.setOnClickListener {
            appendExpression("/100")
            calculateResult(true)
        }

        binding.btnEqual.setOnClickListener {
            calculateResult(true)
        }
    }

    private fun appendExpression(value: String) {
        val op = value.replace("×", "*").replace("÷", "/")
        currentExpression += op
        updateUI()
    }

    private fun updateUI() {
        binding.tvInput.text = currentExpression.replace("*", "×").replace("/", "÷")
        calculateResult(false)
    }

    private fun calculateResult(isEqualPressed: Boolean) {
        try {
            if (currentExpression.isEmpty()) return
            val result = eval(currentExpression)
            val formattedResult = if (result == result.toLong().toDouble()) {
                result.toLong().toString()
            } else {
                result.toString()
            }
            
            if (isEqualPressed) {
                currentExpression = formattedResult
                binding.tvInput.text = currentExpression
                binding.tvPreview.text = ""
            } else {
                binding.tvPreview.text = formattedResult
            }
        } catch (e: Exception) {
            if (isEqualPressed) {
                binding.tvPreview.text = "Error"
            }
        }
    }

    // A robust, simple math expression evaluator
    private fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0
            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code)) x /= parseFactor() // division
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // plus
                if (eat('-'.code)) return -parseFactor() // minus
                var x: Double
                val startPos = pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else {
                    return 0.0
                }
                return x
            }
        }.parse()
    }
}
