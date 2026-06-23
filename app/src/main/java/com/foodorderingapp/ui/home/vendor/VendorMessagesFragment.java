package com.foodorderingapp.ui.home.vendor;

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
import com.foodorderingapp.model.response.ChatUserResponse;
import com.foodorderingapp.ui.adapter.ChatRoomAdapter;
import com.foodorderingapp.ui.chat.ChatActivity;
import com.foodorderingapp.utils.ToastUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorMessagesFragment extends Fragment {

    private ChatRoomAdapter adapter;
    private RecyclerView rvRooms;
    private TextView tvEmpty;

    public VendorMessagesFragment() {
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
                    showEmpty("Khong tai duoc tin nhan");
                    return;
                }

                List<ChatRoomResponse> rooms = response.body();
                adapter.submitList(rooms);
                if (rooms.isEmpty()) {
                    showEmpty("Chua co cuoc tro chuyen nao");
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvRooms.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<ChatRoomResponse>> call, Throwable t) {
                showEmpty("Loi ket noi khi tai tin nhan");
            }
        });
    }

    private void openRoom(ChatRoomResponse room) {
        if (room == null || room.getId() == null) {
            ToastUtils.error(getContext(), "Khong tim thay phong chat");
            return;
        }

        ChatUserResponse student = room.getStudent();
        String studentName = student == null ? "Sinh vien" : student.getFullName();
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_ROOM_ID, room.getId());
        intent.putExtra(ChatActivity.EXTRA_PEER_NAME,
                studentName == null || studentName.trim().isEmpty() ? "Sinh vien" : studentName);
        if (room.getShop() != null) {
            intent.putExtra(ChatActivity.EXTRA_SHOP_ID, room.getShop().getId());
            intent.putExtra(ChatActivity.EXTRA_SHOP_NAME, room.getShop().getName());
        }
        startActivity(intent);
    }

    private void showEmpty(String message) {
        rvRooms.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText(message);
    }
}
