package com.example.module.videoview.ai.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.aitrae.api.ApiClient
import com.example.aitrae.api.Content
import com.example.aitrae.api.DouBaoModelParameters
import com.example.aitrae.api.ImageAnalysisRequest
import com.example.aitrae.api.ImageAnalysisResponse
import com.example.aitrae.api.ImageUrl
import com.example.aitrae.api.Message
import com.example.aitrae.chat.ChatAdapter
import com.example.aitrae.chat.ChatMessageUi
import com.example.aitrae.utils.ImageUtils
import com.example.communityfragment.utils.ImagePickerUtil
import com.example.module.videoview.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

@Route(path = "/aiPage/PlantChatFragment")
class PlantChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var input: EditText
    private lateinit var sendBtn: ImageButton
    private lateinit var imageBtn: ImageButton
    private val adapter = ChatAdapter()
    private val disposables = CompositeDisposable()
    private lateinit var imagePickerUtil: ImagePickerUtil

    private val uiMessages = mutableListOf<ChatMessageUi>()
    private val messages = mutableListOf<Message>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_plant_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        input = view.findViewById(R.id.editTextMessage)
        sendBtn = view.findViewById(R.id.buttonSend)
        imageBtn = view.findViewById(R.id.buttonImage)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        ApiClient.setApiKey(DouBaoModelParameters.API_KEY)

        // 初始化图片选择工具
        initImagePickerUtil()

        val systemPrompt = """
            你是植物健康顾问。判断用户提供的图片和文字是否显示病虫害：
            - 若存在病虫害：给出结论、依据、可执行的处理方案（药剂名称、剂量与频率、补充的环境管理）。
            - 若不存在：指出潜在风险与预防建议（浇水、光照、通风、营养、监测）。
            - 若图片和文字同时提供：综合考虑两者，给出最准确的判断。
            - 若图片和文字都不存在：提示用户提供图片或文字。
            - 请不要输出任何无关信息。
            - 请不要使用Markdown格式输出。
            输出分为：结论/依据/建议 三段，尽量简洁。
        """.trimIndent()
        messages.add(Message(role = "system", content = listOf(Content(type = "text", text = systemPrompt))))

        sendBtn.setOnClickListener {
            val text = input.text?.toString()?.trim()
            if (text.isNullOrEmpty()) return@setOnClickListener
            appendUserText(text)
            sendToModel()
            input.setText("")
        }

        // 直接使用ImagePickerUtil的对话框，不再自定义
        imageBtn.setOnClickListener {
            imagePickerUtil.showImageSourceDialog()
        }
    }

    /**
     * 初始化图片选择工具
     */
    private fun initImagePickerUtil() {
        val activity = requireActivity() as ComponentActivity
        imagePickerUtil = ImagePickerUtil(activity) { uri ->
            // 图片选择回调：处理选中的图片Uri
            handleSelectedImageUri(uri)
        }
    }

    /**
     * 处理选中的图片Uri，转换为Bitmap并添加到聊天
     */
    private fun handleSelectedImageUri(uri: Uri) {
        try {
            // 从Uri获取Bitmap（使用ContentResolver）
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            if (bitmap != null) {
                // 压缩图片（可选，根据需求调整）
                val compressedBitmap = ImageUtils.compressBitmap(bitmap)
                appendUserImage(compressedBitmap)
                sendToModel()
            } else {
                Toast.makeText(requireContext(), "图片解析失败", Toast.LENGTH_SHORT).show()
            }

            inputStream?.close()
        } catch (e: Exception) {
            Log.e("PlantChatFragment", "处理图片失败", e)
            Toast.makeText(requireContext(), "图片处理失败：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun appendUserText(text: String) {
        uiMessages.add(ChatMessageUi(fromUser = true, text = text))
        adapter.submitList(uiMessages.toList())
        messages.add(Message(role = "user", content = listOf(Content(type = "text", text = text))))
        recyclerView.scrollToPosition(uiMessages.lastIndex)
    }

    private fun appendUserImage(bitmap: Bitmap) {
        uiMessages.add(ChatMessageUi(fromUser = true, image = bitmap))
        adapter.submitList(uiMessages.toList())
        val dataUrl = ImageUtils.bitmapToBase64DataUrl(bitmap)
        Log.d("PlantChatFragment", "压缩后的Base64长度: ${dataUrl.length}")
        Log.d("PlantChatFragment", "压缩后的Base64大小约: ${dataUrl.length * 0.75 / 1024} KB")
        messages.add(
            Message(
                role = "user",
                content = listOf(Content(type = "image_url", image_url = ImageUrl(url = dataUrl, detail = "high")))
            )
        )
        recyclerView.scrollToPosition(uiMessages.lastIndex)
    }

    private fun sendToModel() {
        uiMessages.add(ChatMessageUi(fromUser = false, text = "正在分析...", pending = true))
        adapter.submitList(uiMessages.toList())
        recyclerView.scrollToPosition(uiMessages.lastIndex)

        val req = ImageAnalysisRequest(messages = messages.toList(), model = DouBaoModelParameters.MODEL_ID)
        val d = ApiClient.apiService.chat(req)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { resp: ImageAnalysisResponse ->
                    val idx = uiMessages.indexOfLast { !it.fromUser && it.pending }
                    val content = resp.choices.firstOrNull()?.message?.content ?: "无法解析模型回复"
                    if (idx >= 0) {
                        uiMessages[idx] = ChatMessageUi(fromUser = false, text = content, pending = false)
                    } else {
                        uiMessages.add(ChatMessageUi(fromUser = false, text = content))
                    }
                    adapter.submitList(uiMessages.toList())
                    recyclerView.scrollToPosition(uiMessages.lastIndex)

                    messages.add(Message(role = "assistant", content = listOf(Content(type = "text", text = content))))
                },
                { e ->
                    val idx = uiMessages.indexOfLast { !it.fromUser && it.pending }
                    val text = when (e) {
                        is retrofit2.HttpException -> {
                            val body = e.response()?.errorBody()?.string().orEmpty()
                            "请求失败：HTTP${e.code()} ${e.message()}\n${body}"
                        }
                        else -> "请求失败：${e.message}"
                    }
                    if (idx >= 0) uiMessages[idx] = ChatMessageUi(fromUser = false, text = text, pending = false)
                    adapter.submitList(uiMessages.toList())
                    recyclerView.scrollToPosition(uiMessages.lastIndex)
                    Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
                }
            )
        disposables.add(d)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

}