package com.save.protect.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.save.protect.R
import com.save.protect.custom.CustomInput
import com.save.protect.custom.IsValidListener
import com.save.protect.databinding.ActivityCustomShareholderBinding


class CustomShareholderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomShareholderBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_custom_shareholder)

        // TODO : 값검사 에러 return 컴포넌트 내부기능으로 만들기
        // TODO : toInt 때문에 발생하는 NumberFormatException 개선하기
        binding.editTextMarkLimit.setIsValidListener(object : IsValidListener {
            override fun isValid(text: String): Boolean {
                return text.isNotEmpty() && text.toInt() in 1..10
            }
        })

        binding.editTextUpdateInterval.setIsValidListener(object : IsValidListener {
            override fun isValid(text: String): Boolean {
                return text.isNotEmpty() && text.toInt() in 1..60
            }
        })

        binding.editTextMinimumInterval.setIsValidListener(object : IsValidListener {
            override fun isValid(text: String): Boolean {
                return text.isNotEmpty() && text.toInt() in 1..60
            }
        })

        binding.textViewMarkLimit.text = binding.editTextMarkLimit.text.toString()


        val buttonOpenNextScreen: Button = findViewById(R.id.button_openNextScreen)
        buttonOpenNextScreen.setOnClickListener {
            val markLimitText = binding.editTextMarkLimit.text.toString()
            val updateIntervalText = binding.editTextUpdateInterval.text.toString()
            val minimumIntervalText = binding.editTextMinimumInterval.text.toString()

            // 입력 값 검증
            if (isValidInput(markLimitText, updateIntervalText, minimumIntervalText)) {
//                // 입력값을 다음 화면으로 넘겨서 처리하는 코드
                val intent = Intent(this, ShareholderActivity::class.java)
                intent.putExtra("MARK_LIMIT", markLimitText.toInt())
                intent.putExtra("UPDATE_INTERVAL", updateIntervalText.toInt())
                intent.putExtra("MINIMUM_INTERVAL", minimumIntervalText.toInt())
                startActivity(intent)
                finish() // 현재 화면 종료
            }
        }
    }

    private fun isValidInput(
        markLimit: String,
        updateInterval: String,
        minimumInterval: String
    ): Boolean {
        if (markLimit.isEmpty() || updateInterval.isEmpty() || minimumInterval.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        val markLimitValue = markLimit.toIntOrNull()
        val updateIntervalValue = updateInterval.toIntOrNull()
        val minimumIntervalValue = minimumInterval.toIntOrNull()

        if (markLimitValue == null || updateIntervalValue == null || minimumIntervalValue == null ||
            markLimitValue !in 1..10 || updateIntervalValue !in 1..60 || minimumIntervalValue !in 1..60 || minimumIntervalValue > updateIntervalValue
        ) {
            Toast.makeText(this, "입력값이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
