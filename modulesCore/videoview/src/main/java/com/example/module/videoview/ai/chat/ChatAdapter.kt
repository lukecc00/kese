package com.example.module.videoview.ai.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.module.videoview.R

class ChatAdapter : ListAdapter<ChatMessageUi, ChatAdapter.VH>(DIFF) {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatMessageUi>() {
            override fun areItemsTheSame(oldItem: ChatMessageUi, newItem: ChatMessageUi): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: ChatMessageUi, newItem: ChatMessageUi): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val leftText: TextView = itemView.findViewById(R.id.leftText)
        private val rightText: TextView = itemView.findViewById(R.id.rightText)
        private val leftImage: ImageView = itemView.findViewById(R.id.leftImage)
        private val rightImage: ImageView = itemView.findViewById(R.id.rightImage)
        private val leftPending: ProgressBar = itemView.findViewById(R.id.leftPending)
        private val rightPending: ProgressBar = itemView.findViewById(R.id.rightPending)

        fun bind(msg: ChatMessageUi) {
            // Reset visibility
            leftText.visibility = View.GONE
            rightText.visibility = View.GONE
            leftImage.visibility = View.GONE
            rightImage.visibility = View.GONE
            leftPending.visibility = View.GONE
            rightPending.visibility = View.GONE

            if (msg.fromUser) {
                if (!msg.text.isNullOrEmpty()) {
                    rightText.text = msg.text
                    rightText.visibility = View.VISIBLE
                }
                msg.image?.let {
                    rightImage.setImageBitmap(it)
                    rightImage.visibility = View.VISIBLE
                }
                if (msg.pending) rightPending.visibility = View.VISIBLE
            } else {
                if (!msg.text.isNullOrEmpty()) {
                    leftText.text = msg.text
                    leftText.visibility = View.VISIBLE
                }
                msg.image?.let {
                    leftImage.setImageBitmap(it)
                    leftImage.visibility = View.VISIBLE
                }
                if (msg.pending) leftPending.visibility = View.VISIBLE
            }
        }
    }
}

