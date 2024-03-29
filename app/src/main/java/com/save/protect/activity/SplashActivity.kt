package com.save.protect.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.save.protect.util.PermissionUtils
import com.save.protect.R
import com.save.protect.data.DocIdManagement
import com.save.protect.data.UserManagement
import com.save.protect.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        // 유저 정보 초기화
        UserManagement.resetUserInfo()
        initDocId()
        checkLocationPermission()

    }

    // 위치 권한 확인
    private fun checkLocationPermission() {
        when {
            PermissionUtils.hasLocationPermission(this) -> {
                next()
            }
            PermissionUtils.checkShowLocationPermission(this) -> {
                PermissionUtils.showLocationPermissionDialog(this)
            }
            PackageManager.PERMISSION_DENIED == -1 -> {
                PermissionUtils.showLocationPermissionDialog(this)
            }
            else -> {
                PermissionUtils.requestLocationPermission(this)
            }
        }
    }

    private fun initDocId() {
        // 딥링크로 들어온 경우 Intent에서 데이터 추출
        val data = intent?.data
        val deepLinkValue = data?.getQueryParameter("doc_id")

        if (deepLinkValue != null) {
            DocIdManagement.setReceivedId(deepLinkValue)
            deepLinkValue.let { Log.d("딥링크 값", it.toString()) }
        }
    }


    private fun next() {
        val intent = Intent(this, SigninActivity::class.java)
        startActivity(intent)
        finish()
    }
}