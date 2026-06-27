package com.foodorderingapp.ui.home.vendor;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.UpdateStatusRequest;
import com.foodorderingapp.model.response.OrderResponse;
import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.ui.adapter.VendorOrderAdapter;
import com.google.android.material.chip.ChipGroup;
import android.content.Intent;
import android.net.Uri;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.foodorderingapp.model.response.OrderDetailResponse;
import com.foodorderingapp.ui.chat.ChatActivity;
import android.widget.RelativeLayout;
import com.google.gson.Gson;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.foodorderingapp.utils.StompClient;
import com.foodorderingapp.utils.constants.AppConstants;
import com.foodorderingapp.utils.TokenManager;

public class VendorOrdersFragment extends Fragment implements VendorOrderAdapter.OrderActionHandler {

    private RecyclerView rvVendorOrders;
    private SwipeRefreshLayout swipeRefresh;
    private View layoutEmptyOrders;
    private ChipGroup chipGroupStatus;

    private VendorOrderAdapter adapter;
    private final List<OrderResponse> orderList = new ArrayList<>();
    private UUID currentShopId;
    private String selectedStatusFilter = null; // null represents "ALL"
    private StompClient stompClient;
    private final Gson gson = new Gson();



    public VendorOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_orders, container, false);

        // Bind Views
        rvVendorOrders = view.findViewById(R.id.rv_vendor_orders);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        layoutEmptyOrders = view.findViewById(R.id.layout_empty_orders);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);

        // Setup RecyclerView
        rvVendorOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VendorOrderAdapter(orderList, this);
        rvVendorOrders.setAdapter(adapter);

        // Setup Swipe Refresh
        swipeRefresh.setOnRefreshListener(() -> fetchShopInfoAndLoadOrders(true));

        // Setup Status Filter Chip Listener
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedStatusFilter = null;
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chip_all) {
                    selectedStatusFilter = null;
                } else if (checkedId == R.id.chip_pending) {
                    selectedStatusFilter = "PENDING";
                } else if (checkedId == R.id.chip_confirmed) {
                    selectedStatusFilter = "CONFIRMED";
                } else if (checkedId == R.id.chip_delivering) {
                    selectedStatusFilter = "DELIVERING";
                } else if (checkedId == R.id.chip_completed) {
                    selectedStatusFilter = "COMPLETED";
                } else if (checkedId == R.id.chip_cancelled) {
                    selectedStatusFilter = "CANCELLED";
                }
            }
            loadOrders(false);
        });

        // Load Initial Data
        fetchShopInfoAndLoadOrders(false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload when coming back to fragment
        fetchShopInfoAndLoadOrders(false);
    }

    private void fetchShopInfoAndLoadOrders(boolean isRefresh) {
        if (!isRefresh && swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
        }

        ApiClient.getApiService().getVendorShops().enqueue(new Callback<List<ShopResponse>>() {
            @Override
            public void onResponse(Call<List<ShopResponse>> call, Response<List<ShopResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    ShopResponse shop = response.body().get(0);
                    String idStr = shop.getId();
                    if (idStr != null) {
                        currentShopId = UUID.fromString(idStr);
                        connectWebSocket();
                        // Load Orders
                        loadOrders(isRefresh);
                    }
                } else {
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    Toast.makeText(getContext(), "Không thể tải thông tin cửa hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ShopResponse>> call, Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void connectWebSocket() {
        if (currentShopId == null) return;
        if (stompClient != null) {
            stompClient.disconnect();
        }

        String wsUrl = AppConstants.BASE_URL.replace("http://", "ws://").replace("https://", "wss://") + "ws-chat";
        Map<String, String> headers = new HashMap<>();
        String token = TokenManager.getInstance().getAccessToken();
        if (token != null && !token.isEmpty()) {
            headers.put("Authorization", "Bearer " + token);
        }

        stompClient = new StompClient(wsUrl, headers);
        stompClient.setConnectionListener(new StompClient.ConnectionListener() {
            @Override
            public void onConnected() {
                stompClient.subscribe("/topic/shop/" + currentShopId + "/orders", payload -> {
                    if (isAdded() && getContext() != null) {
                        try {
                            OrderResponse updatedOrder = parseOrderUpdate(payload);
                            loadOrders(false);
                            android.content.SharedPreferences prefs = getContext().getSharedPreferences("vendor_settings_pref", android.content.Context.MODE_PRIVATE);
                            boolean alertsEnabled = prefs.getBoolean("order_alerts", true);
                            if (alertsEnabled) {
                                showInAppOrderNotification(updatedOrder);
                            } else {
                                Toast.makeText(getContext(), "Có cập nhật đơn hàng mới!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onDisconnected() {
            }

            @Override
            public void onError(Throwable t) {
            }
        });
        stompClient.connect();
    }

    @Nullable
    private OrderResponse parseOrderUpdate(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return null;
        }

        try {
            return gson.fromJson(payload, OrderResponse.class);
        } catch (Exception exception) {
            android.util.Log.e("ORDER_NOTIF", "Cannot parse order websocket payload", exception);
            return null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
        }
    }

    private void loadOrders(boolean isRefresh) {
        if (currentShopId == null) {
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            return;
        }

        if (isRefresh && swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
        }

        ApiClient.getApiService().getShopOrders(currentShopId, selectedStatusFilter).enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderResponse> orders = response.body();
                    adapter.updateData(orders);
                    if (orders.isEmpty()) {
                        layoutEmptyOrders.setVisibility(View.VISIBLE);
                        rvVendorOrders.setVisibility(View.GONE);
                    } else {
                        layoutEmptyOrders.setVisibility(View.GONE);
                        rvVendorOrders.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Không thể tải danh sách đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void updateOrderStatusOnServer(OrderResponse order, String newStatus, @Nullable String cancelReason) {
        if (currentShopId == null || order.getId() == null) return;

        UpdateStatusRequest request = new UpdateStatusRequest(newStatus, cancelReason);
        UUID orderUuid = UUID.fromString(order.getId());

        ApiClient.getApiService().updateOrderStatus(currentShopId, orderUuid, request).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful()) {
                    String readableStatus = getReadableStatus(newStatus);
                    Toast.makeText(getContext(), "Đã cập nhật đơn hàng sang: " + readableStatus, Toast.LENGTH_SHORT).show();
                    loadOrders(false);
                } else {
                    Toast.makeText(getContext(), "Cập nhật thất bại: " + getErrorMsg(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getReadableStatus(String status) {
        if ("CONFIRMED".equalsIgnoreCase(status)) return "Đã xác nhận";
        if ("DELIVERING".equalsIgnoreCase(status)) return "Đang giao";
        if ("COMPLETED".equalsIgnoreCase(status)) return "Hoàn thành";
        if ("CANCELLED".equalsIgnoreCase(status)) return "Đã hủy";
        return status;
    }

    private String getErrorMsg(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                if (errorJson.contains("\"message\":")) {
                    int startIdx = errorJson.indexOf("\"message\":\"") + 11;
                    int endIdx = errorJson.indexOf("\"", startIdx);
                    return errorJson.substring(startIdx, endIdx);
                }
                return errorJson;
            }
        } catch (Exception ignored) {}
        return "Mã lỗi " + response.code();
    }

    @Override
    public void onAcceptClicked(OrderResponse order) {
        updateOrderStatusOnServer(order, "CONFIRMED", null);
    }

    @Override
    public void onDeliverClicked(OrderResponse order) {
        updateOrderStatusOnServer(order, "DELIVERING", null);
    }

    @Override
    public void onCompleteClicked(OrderResponse order) {
        updateOrderStatusOnServer(order, "COMPLETED", null);
    }

    @Override
    public void onCancelClicked(OrderResponse order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Xác nhận hủy đơn hàng?");
        builder.setMessage("Vui lòng nhập lý do hủy đơn hàng bắt buộc:");

        final EditText etReason = new EditText(requireContext());
        etReason.setHint("Lý do hủy đơn...");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        etReason.setLayoutParams(lp);
        builder.setView(etReason);

        builder.setPositiveButton("Hủy đơn", (dialog, which) -> {
            String reason = etReason.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(getContext(), "Bạn phải cung cấp lý do hủy đơn!", Toast.LENGTH_SHORT).show();
                onCancelClicked(order); // Reopen dialog
            } else {
                updateOrderStatusOnServer(order, "CANCELLED", reason);
            }
        });

        builder.setNegativeButton("Quay lại", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onContactStudentClicked(OrderResponse order) {
        if (order == null || order.getId() == null || order.getId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy đơn hàng để nhắn tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_ORDER_ID, order.getId());
        intent.putExtra(ChatActivity.EXTRA_PEER_NAME,
                order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()
                        ? "Sinh viên"
                        : order.getCustomerName());
        startActivity(intent);
    }

    @Override
    public void onOrderClicked(OrderResponse order) {
        showOrderDetailDialog(order);
    }

    private void showOrderDetailDialog(OrderResponse order) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_order_detail, null);

        TextView tvOrderId = view.findViewById(R.id.tv_detail_order_id);
        TextView tvOrderStatus = view.findViewById(R.id.tv_detail_order_status);
        TextView tvCustomerName = view.findViewById(R.id.tv_detail_customer_name);
        TextView tvCustomerPhone = view.findViewById(R.id.tv_detail_customer_phone);
        View btnCall = view.findViewById(R.id.btn_call_customer);
        View btnChat = view.findViewById(R.id.btn_chat_customer);
        TextView tvBuilding = view.findViewById(R.id.tv_detail_building);
        LinearLayout layoutItems = view.findViewById(R.id.layout_detail_order_items);
        View layoutDiscount = view.findViewById(R.id.layout_detail_discount);
        TextView tvDiscountLabel = view.findViewById(R.id.tv_detail_discount_label);
        TextView tvDiscountValue = view.findViewById(R.id.tv_detail_discount_value);
        TextView tvTotal = view.findViewById(R.id.tv_detail_total);
        View btnClose = view.findViewById(R.id.btn_close_detail);

        // Bind basic info
        String shortId = order.getId();
        if (shortId != null && shortId.length() > 6) {
            shortId = shortId.substring(shortId.length() - 6);
        }
        if (tvOrderId != null) tvOrderId.setText("Đơn hàng #" + shortId);

        String status = order.getStatus();
        if (tvOrderStatus != null) {
            tvOrderStatus.setText(getReadableStatus(status));
            int badgeColor = android.graphics.Color.parseColor("#E53935");
            if ("CONFIRMED".equalsIgnoreCase(status)) {
                badgeColor = android.graphics.Color.parseColor("#3182CE");
            } else if ("DELIVERING".equalsIgnoreCase(status)) {
                badgeColor = android.graphics.Color.parseColor("#DD6B20");
            } else if ("COMPLETED".equalsIgnoreCase(status)) {
                badgeColor = android.graphics.Color.parseColor("#38A169");
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                badgeColor = android.graphics.Color.parseColor("#718096");
            }
            tvOrderStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(badgeColor));
        }

        if (tvCustomerName != null) tvCustomerName.setText(order.getCustomerName() != null ? order.getCustomerName() : "Không tên");
        if (tvCustomerPhone != null) tvCustomerPhone.setText(order.getCustomerPhone() != null ? order.getCustomerPhone() : "Chưa có SĐT");

        if (btnCall != null && order.getCustomerPhone() != null && !order.getCustomerPhone().isEmpty()) {
            btnCall.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + order.getCustomerPhone()));
                startActivity(intent);
            });
        }

        if (tvBuilding != null) tvBuilding.setText("Giao đến: " + (order.getBuilding() != null ? order.getBuilding() : "Chưa rõ tòa nhà"));

        if (btnChat != null) {
            btnChat.setOnClickListener(v -> {
                dialog.dismiss();
                onContactStudentClicked(order);
            });
        }

        // Items list
        if (layoutItems != null) {
            layoutItems.removeAllViews();
            if (order.getDetails() != null) {
                for (OrderDetailResponse detail : order.getDetails()) {
                    RelativeLayout itemRow = new RelativeLayout(requireContext());
                    itemRow.setLayoutParams(new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    itemRow.setPadding(0, 6, 0, 6);

                    TextView tvName = new TextView(requireContext());
                    tvName.setText(detail.getQuantity() + "x " + detail.getFoodName());
                    tvName.setTextColor(android.graphics.Color.parseColor("#1A202C"));
                    tvName.setTextSize(13);
                    RelativeLayout.LayoutParams lpLeft = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lpLeft.addRule(RelativeLayout.ALIGN_PARENT_START);
                    tvName.setLayoutParams(lpLeft);

                    TextView tvPrice = new TextView(requireContext());
                    tvPrice.setText(formatCurrency(detail.getPrice() * detail.getQuantity()));
                    tvPrice.setTextColor(android.graphics.Color.parseColor("#718096"));
                    tvPrice.setTextSize(13);
                    RelativeLayout.LayoutParams lpRight = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lpRight.addRule(RelativeLayout.ALIGN_PARENT_END);
                    tvPrice.setLayoutParams(lpRight);

                    itemRow.addView(tvName);
                    itemRow.addView(tvPrice);
                    layoutItems.addView(itemRow);
                }
            }
        }

        // Voucher discount
        if (order.getDiscountAmount() > 0) {
            if (layoutDiscount != null) layoutDiscount.setVisibility(View.VISIBLE);
            if (tvDiscountLabel != null) {
                String codeInfo = order.getVoucherCode() != null ? " (" + order.getVoucherCode() + ")" : "";
                tvDiscountLabel.setText("Khuyến mãi" + codeInfo);
            }
            if (tvDiscountValue != null) tvDiscountValue.setText("-" + formatCurrency(order.getDiscountAmount()));
        } else {
            if (layoutDiscount != null) layoutDiscount.setVisibility(View.GONE);
        }

        if (tvTotal != null) tvTotal.setText(formatCurrency(order.getTotalPrice()));

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.setContentView(view);
        dialog.show();
    }

    private String formatCurrency(double value) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(value) + "đ";
    }

    private void showInAppOrderNotification(@Nullable OrderResponse order) {
        android.content.Context context = getContext();
        if (context == null) return;

        // Vibrate gently
        android.os.Vibrator vibrator = (android.os.Vibrator) context.getSystemService(android.content.Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(200);
            }
        }

        // Show Heads-Up Notification
        String channelId = "food_ordering_notifications";
        
        // Ensure channel is created locally
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId,
                    "Food ordering notifications",
                    android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Order, shop, and account updates");
            android.app.NotificationManager manager = (android.app.NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                new android.content.Intent(context, com.foodorderingapp.MainActivity.class)
                        .putExtra("USER_ROLE", "VENDOR")
                        .putExtra("OPEN_TAB", "ORDERS")
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_toast_info)
                .setContentTitle("Đơn hàng mới!")
                .setContentText("Bạn có một đơn hàng mới vừa được cập nhật.")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        String notificationBody = buildOrderUpdateBody(order);
        builder.setContentTitle(buildOrderUpdateTitle(order));
        builder.setContentText(notificationBody);
        builder.setStyle(new androidx.core.app.NotificationCompat.BigTextStyle().bigText(notificationBody));

        androidx.core.app.NotificationManagerCompat notificationManager = androidx.core.app.NotificationManagerCompat.from(context);
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException exception) {
            android.util.Log.e("ORDER_NOTIF", "Permission error showing notification", exception);
        }
    }

    private String buildOrderUpdateTitle(@Nullable OrderResponse order) {
        if (order != null && "CANCELLED".equalsIgnoreCase(order.getStatus())) {
            return "Đơn hàng đã bị hủy";
        }

        if (order != null && "PENDING".equalsIgnoreCase(order.getStatus())) {
            return "Đơn hàng mới!";
        }

        return "Có cập nhật đơn hàng";
    }

    private String buildOrderUpdateBody(@Nullable OrderResponse order) {
        if (order == null) {
            return "Bạn có một đơn hàng vừa được cập nhật.";
        }

        String orderId = shortOrderId(order.getId());
        String customerName = order.getCustomerName();
        String customerPart = customerName == null || customerName.trim().isEmpty()
                ? "Sinh viên"
                : customerName.trim();

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            String reason = order.getCancelReason();
            String reasonPart = reason == null || reason.trim().isEmpty()
                    ? ""
                    : " Lý do: " + reason.trim();
            return customerPart + " đã hủy đơn #" + orderId + "." + reasonPart;
        }

        if ("PENDING".equalsIgnoreCase(order.getStatus())) {
            return "Bạn có đơn mới #" + orderId + " từ " + customerPart + ".";
        }

        return "Đơn #" + orderId + " vừa được cập nhật trạng thái: " + getReadableStatus(order.getStatus()) + ".";
    }

    private String shortOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return "000000";
        }

        String compactId = orderId.replace("-", "");
        if (compactId.length() <= 6) {
            return compactId;
        }
        return compactId.substring(0, 6);
    }
}
