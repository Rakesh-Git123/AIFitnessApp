package com.example.aifitness

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.textMessage)
        val messageContainer: View = itemView.findViewById(R.id.messageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.message

        val layoutParams = holder.messageContainer.layoutParams as ViewGroup.MarginLayoutParams
        if (message.sender == "user") {
            layoutParams.marginStart = 60
            layoutParams.marginEnd = 16
            holder.messageContainer.setBackgroundResource(R.drawable.chat_message_user_background)
            holder.messageText.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
        } else {
            layoutParams.marginStart = 16
            layoutParams.marginEnd = 60
            holder.messageContainer.setBackgroundResource(R.drawable.chat_message_bot_background)
            holder.messageText.setTextColor(android.graphics.Color.parseColor("#000000"))
        }
        holder.messageContainer.layoutParams = layoutParams
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}

