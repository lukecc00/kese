package com.example.communityfragment.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.example.communityfragment.R
import com.example.communityfragment.adapter.ImageAdapter
import com.example.communityfragment.bean.PostPublishedEvent
import com.example.communityfragment.contract.IPublishContract
import com.example.communityfragment.databinding.ActivityPublishBinding
import com.example.communityfragment.presenter.PublishPresenter
import com.example.communityfragment.utils.ImagePickerUtil
import com.example.module.libBase.SPUtils
import com.yalantis.ucrop.UCrop
import org.greenrobot.eventbus.EventBus
import java.io.File

@Route(path = "/communityPageView/PublishActivity")
class PublishActivity : AppCompatActivity(), IPublishContract.View {
    companion object {
        const val TAG = "PublishFunctionTAG"
    }

    private lateinit var binding: ActivityPublishBinding
    private lateinit var mPresenter: PublishPresenter
    private lateinit var imagePickerUtil: ImagePickerUtil

    private val imageUrls = mutableListOf<String>()
    private var dialogLoading: AlertDialog? = null
    private lateinit var imageAdapter: ImageAdapter

    private val uCropLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                UCrop.getOutput(result.data!!)?.let { resultUri ->
                    imageUrls.add(resultUri.toString())
                    imageAdapter.notifyItemInserted(imageUrls.size - 1)
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = result.data?.let { UCrop.getError(it) }
                Toast.makeText(this, "裁剪出错：图片格式不支持", Toast.LENGTH_SHORT).show()
                Log.e("MyInfoActivityTAG", "onActivityResult: ", cropError)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublishBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.publish_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mPresenter = PublishPresenter(this)
        setupImagePicker()

        Glide.with(this)
            .load(avatar)
            .error(R.drawable.default_user2)
            .into(binding.imgPublishAvatar)
        binding.tvPublishUsername.text = userName

        binding.imgPublishSend.setOnClickListener {
            val content = binding.etPublishContent.text.toString()
            if (content.trim().isEmpty()) {
                Toast.makeText(this@PublishActivity, "请输入内容", Toast.LENGTH_SHORT).show()
            } else {
                binding.imgPublishSend.isEnabled = false
                binding.imgPublishSend.setImageResource(R.drawable.ic_publish_gray)
                val imagePaths = ArrayList(imageUrls)

                Toast.makeText(this@PublishActivity, "上传中", Toast.LENGTH_SHORT).show()
                if (imagePaths.isNotEmpty()) {
                    showPublishDialog()
                }

                mPresenter.publish(content, imagePaths, communityId)
            }
        }

        binding.imgPublishExit.setOnClickListener { finish() }

        setupRecyclerView()
        setupTextWatcher()
        setupCommunitySelector()
    }

    private fun setupImagePicker() {
        imagePickerUtil = ImagePickerUtil(this) { uri ->
            // 图片选择后的回调，启动裁剪
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
            val options = UCrop.Options().apply {
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
                setCompressionQuality(50)
            }
            val ucropIntent = UCrop.of(uri, destinationUri)
                .withOptions(options)
                .getIntent(this)
            uCropLauncher.launch(ucropIntent)
        }
    }

    private fun setupRecyclerView() {
        imageAdapter = ImageAdapter(this, imageUrls, object : ImageAdapter.OnImageActionListener {
            override fun onAddImageClick() {
                if (imageUrls.size < 9) {
                    imagePickerUtil.showImageSourceDialog()
                }
            }

            override fun onDeleteImage(position: Int) {
                imageUrls.removeAt(position)
                imageAdapter.notifyItemRemoved(position)
                imageAdapter.notifyItemRangeChanged(position, imageUrls.size)
            }
        })
        binding.rlvImage.setHasFixedSize(true)
        binding.rlvImage.layoutManager = GridLayoutManager(this, 3)
        binding.rlvImage.adapter = imageAdapter
    }

    private fun setupTextWatcher() {
        binding.etPublishContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length == 1000) {
                    Toast.makeText(this@PublishActivity, "字数已达上限", Toast.LENGTH_SHORT).show()
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun setupCommunitySelector() {
        binding.llPublishSelsect.setOnClickListener {
            val currentCommunityId = when (binding.tvPublishSelsect.text) {
                "种植交流" -> 1
                "农业资讯" -> 2
                else -> 0 // "农友杂谈"
            }
            Log.d("BottomSelectFragmentTAG", currentCommunityId.toString())
            val bottomSelectFragment = BottomSelectFragment(currentCommunityId)
            bottomSelectFragment.setOnOptionSelectedListener { selectedOption ->
                binding.tvPublishSelsect.text = when (selectedOption) {
                    1 -> "种植交流"
                    2 -> "农业资讯"
                    else -> "农友杂谈"
                }
            }
            bottomSelectFragment.show(supportFragmentManager, "BottomSelectFragment")
        }
    }

    private val communityId: Int
        get() = when (binding.tvPublishSelsect.text.toString()) {
            "农友杂谈" -> 1
            "种植交流" -> 2
            "农业资讯" -> 3
            else -> 1
        }

    private val avatar: String
        get() = SPUtils.getString(this@PublishActivity, SPUtils.AVATAR_KEY, "")

    private val userName: String
        get() = SPUtils.getString(this@PublishActivity, SPUtils.USERNAME_KEY, "")

    override fun publishSuccess() {
        runOnUiThread {
            EventBus.getDefault().postSticky(PostPublishedEvent())
            Toast.makeText(this@PublishActivity, "发布成功", Toast.LENGTH_SHORT).show()
            binding.imgPublishSend.isEnabled = true
            dialogLoading?.dismiss()
            finish()
        }
    }

    override fun publishFailure() {
        runOnUiThread {
            dialogLoading?.dismiss()
            Toast.makeText(this@PublishActivity, "发布失败，请重试", Toast.LENGTH_SHORT).show()
            binding.imgPublishSend.isEnabled = true
            binding.imgPublishSend.setImageResource(R.drawable.ic_publish)
        }
    }

    private fun showPublishDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null, false)
        builder.setView(dialogView)
        dialogLoading = builder.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.default_dialog_background)
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            show()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is android.widget.EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}