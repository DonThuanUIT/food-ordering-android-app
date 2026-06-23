package com.foodorderingapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.ChatRoomResponse;
import com.foodorderingapp.model.response.ChatUserResponse;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private final List<ChatRoomResponse> rooms = new ArrayList<>();
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(ChatRoomResponse room);
    }

    public void setOnRoomClickListener(OnRoomClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ChatRoomResponse> newRooms) {
        rooms.clear();
        if (newRooms != null) {
            rooms.addAll(newRooms);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoomResponse room = rooms.get(position);
        ChatUserResponse student = room.getStudent();
        String name = nullToDefault(student == null ? null : student.getFullName(), "Sinh vien");
        String phone = student == null ? "" : nullToDefault(student.getPhone(), "");

        holder.tvAvatar.setText(initial(name));
        holder.tvTitle.setText(name);
        holder.tvSubtitle.setText(phone.isEmpty() ? "Nhan de tra loi student" : phone);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRoomClick(room);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    private String initial(String value) {
        return value == null || value.trim().isEmpty()
                ? "S"
                : value.trim().substring(0, 1).toUpperCase();
    }

    private String nullToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar;
        TextView tvTitle;
        TextView tvSubtitle;

        ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvChatRoomAvatar);
            tvTitle = itemView.findViewById(R.id.tvChatRoomTitle);
            tvSubtitle = itemView.findViewById(R.id.tvChatRoomSubtitle);
        }
    }
}
