package com.example.fluentcalculator

import android.animation.ObjectAnimator
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.fluentcalculator.databinding.ActivityMainBinding
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentInput = "0"
    private var storedValue: BigDecimal? = null
    private var pendingOp: Char? = null
    private var justEvaluated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFluentEffects()
        setupButtons()
        refreshDisplay()
    }

    private fun setupFluentEffects() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.glassPanel.setRenderEffect(
                RenderEffect.createBlurEffect(14f, 14f, Shader.TileMode.CLAMP)
            )
        }
        animateIn(binding.glassPanel)
    }

    private fun animateIn(view: View) {
        view.alpha = 0f
        view.translationY = 20f
        ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
            duration = 260
            interpolator = DecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 20f, 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun setupButtons() {
        listOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2", binding.btn3 to "3",
            binding.btn4 to "4", binding.btn5 to "5", binding.btn6 to "6",
            binding.btn7 to "7", binding.btn8 to "8", binding.btn9 to "9"
        ).forEach { (btn, d) -> btn.setOnClickListener { appendDigit(d) } }

        binding.btnDot.setOnClickListener { appendDot() }

        binding.btnAdd.setOnClickListener { setOperator('+') }
        binding.btnSub.setOnClickListener { setOperator('-') }
        binding.btnMul.setOnClickListener { setOperator('*') }
        binding.btnDiv.setOnClickListener { setOperator('/') }

        binding.btnClear.setOnClickListener { clearAll() }
        binding.btnBack.setOnClickListener { backspace() }
        binding.btnSign.setOnClickListener { toggleSign() }
        binding.btnPercent.setOnClickListener { percent() }
        binding.btnEq.setOnClickListener { equalsOp() }
    }

    private fun appendDigit(d: String) {
        if (justEvaluated && pendingOp == null) {
            currentInput = "0"
            justEvaluated = false
        }
        currentInput = when {
            currentInput == "0" -> d
            currentInput == "-0" -> "-$d"
            else -> currentInput + d
        }
        refreshDisplay()
    }

    private fun appendDot() {
        if (justEvaluated && pendingOp == null) {
            currentInput = "0"
            justEvaluated = false
        }
        if (!currentInput.contains(".")) {
            currentInput += "."
            refreshDisplay()
        }
    }

    private fun setOperator(op: Char) {
        val current = parseCurrent() ?: return
        storedValue = if (storedValue == null) current else if (!justEvaluated && pendingOp != null) {
            compute(storedValue!!, current, pendingOp!!) ?: return
        } else storedValue

        pendingOp = op
        currentInput = "0"
        justEvaluated = false
        refreshDisplay()
    }

    private fun clearAll() {
        currentInput = "0"
        storedValue = null
        pendingOp = null
        justEvaluated = false
        refreshDisplay()
    }

    private fun backspace() {
        if (justEvaluated) return
        currentInput = if (currentInput.length <= 1 || (currentInput.length == 2 && currentInput.startsWith("-"))) {
            "0"
        } else currentInput.dropLast(1)
        if (currentInput == "-" || currentInput.isBlank()) currentInput = "0"
        refreshDisplay()
    }

    private fun toggleSign() {
        if (currentInput == "0") return
        currentInput = if (currentInput.startsWith("-")) currentInput.drop(1) else "-$currentInput"
        refreshDisplay()
    }

    private fun percent() {
        val value = parseCurrent() ?: return
        val result = if (storedValue != null && pendingOp != null) {
            storedValue!!.multiply(value).divide(BigDecimal(100), 12, RoundingMode.HALF_UP)
        } else {
            value.divide(BigDecimal(100), 12, RoundingMode.HALF_UP)
        }
        currentInput = format(result)
        refreshDisplay()
    }

    private fun equalsOp() {
        val op = pendingOp ?: run {
            justEvaluated = true
            refreshDisplay()
            return
        }
        val left = storedValue ?: return
        val right = parseCurrent() ?: return
        val result = compute(left, right, op) ?: run {
            binding.tvPreview.text = "= Invalid"
            return
        }
        currentInput = format(result)
        storedValue = null
        pendingOp = null
        justEvaluated = true
        refreshDisplay()
    }

    private fun parseCurrent(): BigDecimal? = try {
        currentInput.toBigDecimal()
    } catch (_: Exception) { null }

    private fun compute(a: BigDecimal, b: BigDecimal, op: Char): BigDecimal? = try {
        when (op) {
            '+' -> a.add(b)
            '-' -> a.subtract(b)
            '*' -> a.multiply(b)
            '/' -> if (b.compareTo(BigDecimal.ZERO) == 0) null else a.divide(b, 12, RoundingMode.HALF_UP)
            else -> null
        }
    } catch (_: Exception) { null }

    private fun refreshDisplay() {
        binding.tvExpression.text = currentInput
        binding.tvPreview.text = previewText()
    }

    private fun previewText(): String {
        val left = storedValue ?: return "="
        val op = pendingOp ?: return "="
        val current = parseCurrent() ?: return "="
        val result = compute(left, current, op) ?: return "= Invalid"

        val symbol = when (op) {
            '*' -> "×"
            '/' -> "÷"
            '-' -> "−"
            else -> op.toString()
        }
        return "${format(left)} $symbol ${format(current)} = ${format(result)}"
    }

    private fun format(v: BigDecimal): String {
        val s = v.stripTrailingZeros().toPlainString()
        return if (s == "-0") "0" else s
    }
}
