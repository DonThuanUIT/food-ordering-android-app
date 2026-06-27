package com.foodorderingapp.ui.home.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.response.ChatRoomResponse;
import com.foodorderingapp.ui.adapter.ChatRoomAdapter;
import com.foodorderingapp.ui.chat.ChatActivity;
import com.foodorderingapp.utils.ToastUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentMessagesFragment extends Fragment {

    private ChatRoomAdapter adapter;
    private RecyclerView rvRooms;
    private TextView tvEmpty;

    public StudentMessagesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vendor_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvEmpty = view.findViewById(R.id.tvVendorMessagesEmpty);
        rvRooms = view.findViewById(R.id.rvVendorChatRooms);

        adapter = new ChatRoomAdapter();
        adapter.setOnRoomClickListener(this::openRoom);
        rvRooms.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRooms.setAdapter(adapter);

        loadRooms();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRooms();
    }

    private void loadRooms() {
        ApiClient.getApiService().getChatRooms().enqueue(new Callback<List<ChatRoomResponse>>() {
            @Override
            public void onResponse(Call<List<ChatRoomResponse>> call,
                                   Response<List<ChatRoomResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showEmpty("Không tải được tin nhắn");
                    return;
                }

                List<ChatRoomResponse> rooms = response.body();
                adapter.submitList(rooms);
                if (rooms.isEmpty()) {
                    showEmpty("Chưa có cuộc trò chuyện nào");
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvRooms.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<ChatRoomResponse>> call, Throwable t) {
                showEmpty("Lỗi kết nối khi tải tin nhắn");
            }
        });
    }

    private void openRoom(ChatRoomResponse room) {
        if (room == null || room.getRoomId() == null) {
            ToastUtils.error(getContext(), "Không tìm thấy phòng chat");
            return;
        }

        String partnerName = room.getPartnerName();
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_ROOM_ID, room.getRoomId());
        intent.putExtra(ChatActivity.EXTRA_PEER_NAME,
                partnerName == null || partnerName.trim().isEmpty() ? "Quán" : partnerName);
        startActivity(intent);
    }

    private void showEmpty(String message) {
        rvRooms.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText(message);
    }
}
