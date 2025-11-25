package com.example.communityfragment.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.module.libBase.R
import java.io.File
import kotlin.reflect.KParameter

/**
 * 图片选择工具类
 * 封装了从相机或相册选择图片的功能，包括权限处理。
 * @author sunwenyu@bytedance.com
 * @date 2025/11/26
 */
class ImagePickerUtil(
    private val activity: ComponentActivity,
    private val onImagePicked: (Uri) -> Unit
) {

    private var cameraImageUri: Uri? = null
    private var dialogPick: AlertDialog? = null

    // 权限和Activity结果启动器
    private val cameraPermissionLauncher: ActivityResultLauncher<String>
    private val storagePermissionLauncher: ActivityResultLauncher<Array<String>>
    private val pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private val takePictureLauncher: ActivityResultLauncher<Uri>

    init {
        val registry = activity.activityResultRegistry
        cameraPermissionLauncher = registry.register(
            "cameraPermission",
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(activity, "请允许摄像头权限以拍照", Toast.LENGTH_SHORT).show()
            }
        }

        storagePermissionLauncher = registry.register(
            "storagePermission",
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.all { it }) {
                launchImagePicker()
            } else {
                Toast.makeText(activity, "请允许权限以选择图片", Toast.LENGTH_SHORT).show()
            }
        }

        pickMediaLauncher = registry.register(
            "pickMedia",
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                dialogPick?.dismiss()
                onImagePicked(it)
            }
        }

        takePictureLauncher = registry.register(
            "takePicture",
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                cameraImageUri?.let {
                    dialogPick?.dismiss()
                    onImagePicked(it)
                }
            }
        }
    }

    /**
     * 显示选择图片来源的对话框（相机或相册）
     */
    fun showImageSourceDialog() {
        val builder = AlertDialog.Builder(activity)
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_pick_layout, null)
        val btnCamera: Button = dialogView.findViewById(R.id.btn_camera)
        val btnGallery: Button = dialogView.findViewById(R.id.btn_gallery)

        btnCamera.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }
        btnGallery.setOnClickListener {
            requestStoragePermissions()
        }

        builder.setView(dialogView)
        dialogPick = builder.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.default_dialog_background)
            show()
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            launchCamera()
        }
    }

    private fun launchCamera() {
        val imageFile = File(activity.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        cameraImageUri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            imageFile
        )
        takePictureLauncher.launch(cameraImageUri)
    }

    private fun requestStoragePermissions() {
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES
            )
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        storagePermissionLauncher.launch(permissions)
    }

    private fun launchImagePicker() {
        pickMediaLauncher.launch(
            PickVisualMediaRequest.Builder()
                .build()
        )
    }
}