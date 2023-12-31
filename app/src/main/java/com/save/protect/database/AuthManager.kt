package com.save.protect.database

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object AuthManager {

    private val auth = Firebase.auth

    // 회원가입
    fun signUpFireBase(email: String?, password: String?, onSuccess: () -> Unit) {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            // 이메일 또는 비밀번호가 빈 문자열 또는 null인 경우
            Log.d("회원가입", "이메일 또는 비밀번호를 확인하세요.")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 회원 가입 성공
                    Log.d("회원가입", "회원 가입 성공")
                    onSuccess()
                } else {
                    // 회원 가입 실패
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        // Firebase Authentication 예외 처리
                        Log.d("회원가입", "Firebase Authentication 오류: ${exception.errorCode}")
                    } else {
                        // 기타 오류 처리
                        Log.d("회원가입", "회원 가입 실패: ${exception?.message}")
                    }
                }
            }
    }

    fun loginFireBase(
        context: Context,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    Toast.makeText(context, "로그인 성공", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    // 로그인 실패
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        // Firebase Authentication 예외 처리
                        val errorMessage = "Firebase Authentication 오류: ${exception.errorCode}"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onFailure(errorMessage)
                    } else {
                        // 기타 오류 처리
                        val errorMessage = "로그인 실패: ${exception?.message}"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        onFailure(errorMessage)
                    }
                }
            }
    }

}
