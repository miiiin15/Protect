package com.save.protect.activity

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import com.save.protect.BaseActivity
import com.save.protect.R
import com.save.protect.custom.BottomSheetChat
import com.save.protect.data.UserInfo
import com.save.protect.data.UserManagement
import com.save.protect.database.LocationTransmitter
import com.save.protect.util.ImageUtils
import com.save.protect.util.KakaoUtils
import com.save.protect.util.PermissionUtils.checkShowLocationPermission
import com.save.protect.util.PermissionUtils.hasLocationPermission
import com.save.protect.util.PermissionUtils.requestLocationPermission
import com.save.protect.util.PermissionUtils.showLocationPermissionDialog
import java.util.*

class ShareholderActivity : BaseActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap

    private lateinit var button_invite: Button
    private lateinit var checkboxAutoFocus: CheckBox

    private lateinit var userData: UserInfo
    private var uid: String? = null
    private var isAutoFocus = true
    private var marker = Marker()


    // 사용자 입력값
    private var setting_markLimit = 3
    private var setting_updateInterval = 10
    private var setting_minimunInterval = 3


    // 위치 데이터를 저장하는 리스트 (제한된 길이 유지)
    private val locationDataList = mutableListOf<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shareholder)
        initializeMapView(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setInitialValues()
        BottomSheetChat.initBottomSheet(
            findViewById(R.id.bottom_sheet_chat),
            onChange = { bttomSheet, newState ->
                Log.d("바텀시트 newState : ", " $newState")
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                }
            },
            onSlide = { bttomSheet, slideOffset ->
//                Log.d("바텀시트 slideOffset : ", " $slideOffset")
            }
        )

        createLocationCallback()

        // 위치 권한 확인
        checkLocationPermission()


        button_invite.setOnClickListener {
            KakaoUtils.shareText(this, "위치공유 초대", "위치공유 초대가 왔습니다.", "${uid}")
        }

        checkboxAutoFocus.setOnCheckedChangeListener { buttonView, isChecked ->
            isAutoFocus = isChecked
        }

    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        startLocationUpdates(setting_updateInterval * 100L, setting_minimunInterval * 100L)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    private fun setInitialValues() {
        // Intent로 전달받은 설정 값을 읽어옵니다.
        val markLimit = intent.getIntExtra("MARK_LIMIT", 3)
        val updateInterval = intent.getIntExtra("UPDATE_INTERVAL", 10)
        val minimumInterval = intent.getIntExtra("MINIMUM_INTERVAL", 3)
        uid = UserManagement.uid
        userData = UserManagement.getUserInfo()!!

        Log.e("저장된유저 ", "${UserManagement.getUserInfo()}")
        Log.e("저장된유저 ", UserManagement.uid)

        // 값이 null인 경우 처리
        if (markLimit != 0 || updateInterval != 0 || minimumInterval != 0) {
            setting_markLimit = markLimit
            setting_minimunInterval = minimumInterval
            setting_updateInterval = updateInterval
        }
    }

    private fun initializeMapView(savedInstanceState: Bundle?) {
        mapView = findViewById(R.id.map_view)
        button_invite = findViewById(R.id.button_invite)
        checkboxAutoFocus = findViewById(R.id.checkbox_autoFocus)
        mapView.onCreate(savedInstanceState)
    }

    // 위치 권한 확인
    private fun checkLocationPermission() {
        when {
            hasLocationPermission(this) -> {
            }

            checkShowLocationPermission(this) -> {
                showLocationPermissionDialog(this)
            }
            PackageManager.PERMISSION_DENIED == -1 -> {
                showLocationPermissionDialog(this)
            }
            else -> {
                requestLocationPermission(this)
            }
        }
    }

    private fun _checkList(locationData: Any): MutableList<Any> {
        // 위치 데이터를 리스트에 추가
        locationDataList.add(0, locationData)
        // 리스트가 제한된 길이를 초과하는 경우, 가장 오래된 데이터를 제거 (선입선출)
        if (locationDataList.size > setting_markLimit) {
            locationDataList.removeAt(setting_markLimit)
        }

        return locationDataList
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // 위치 변경 시 작업
                    setMapMarker(location.latitude, location.longitude)
                    uid?.let { LocationTransmitter.sendLocationData(it, _checkList(location)) }
                    // 위치 변경을 출력
                    Log.d("위치 추적", "위도: ${location.latitude}, 경도: ${location.longitude}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(updateInterval: Long = 10000, minimumInterval: Long = 3000) {
        Log.d("위치 추적", "시작")
//        Log.d("타이머 값들 : ", "$setting_markLimit / $updateInterval / $minimunInterval")

        if (hasLocationPermission(this)) {
            val locationRequest = LocationRequest()
            locationRequest.interval = updateInterval // 위치 업데이트 간격 (밀리초)
            locationRequest.fastestInterval = minimumInterval // 가장 빠른 업데이트 간격 (밀리초)
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    fun stopLocationUpdates() {
        Log.d("위치 추적", "중지")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 지도에 마커 표시
    private fun setMapMarker(userLatitude: Double, userLongitude: Double) {
        mapView.getMapAsync { nMap ->
            naverMap = nMap

            val initialLatLng = LatLng(userLatitude, userLongitude)
            val cameraUpdate = CameraUpdate.scrollAndZoomTo(initialLatLng, 17.0).animate(
                CameraAnimation.Fly
            )
            if (isAutoFocus) {
                naverMap!!.moveCamera(cameraUpdate)
            }


            marker.position = initialLatLng
            marker.map = naverMap
            if (userData.userName.isNotEmpty()) {
                marker.captionText = userData.userName
            } else {
                marker.captionText = "익명의 유저"
            }
            if (userData.imageUrl.isNotEmpty()) {
                // 마커 이미지를 URL에서 로드하여 설정
                Log.d("이미지 주소", " ${userData.imageUrl}")
                ImageUtils.loadBitmapFromUrl(this, userData.imageUrl) {
                    marker.icon = OverlayImage.fromBitmap(
                        // 이미지 리사이징
                        ImageUtils.resizeAndCropToCircle(it!!, 100, 100, 3, Color.BLACK)
                    )
                }
            } else {
                marker.icon = MarkerIcons.BLACK
                marker.iconTintColor = Color.LTGRAY
            }
        }
    }
}
