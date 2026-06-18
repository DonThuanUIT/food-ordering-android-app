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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorOrdersFragment extends Fragment implements VendorOrderAdapter.OrderActionHandler {

    private RecyclerView rvVendorOrders;
    private SwipeRefreshLayout swipeRefresh;
    private View layoutEmptyOrders;
    private SwitchCompat switchStoreStatus;
    private TextView tvStoreStatusDesc;
    private ChipGroup chipGroupStatus;

    private VendorOrderAdapter adapter;
    private final List<OrderResponse> orderList = new ArrayList<>();
    private UUID currentShopId;
    private String selectedStatusFilter = null; // null represents "ALL"

    private final CompoundButton.OnCheckedChangeListener statusSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            toggleShopStatusOnServer(isChecked);
        }
    };

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
        switchStoreStatus = view.findViewById(R.id.switch_store_status);
        tvStoreStatusDesc = view.findViewById(R.id.tv_store_status_desc);
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

                        // Set store switch state without triggering listener
                        switchStoreStatus.setOnCheckedChangeListener(null);
                        boolean isOpen = shop.getIsOpen() != null ? shop.getIsOpen() : true;
                        switchStoreStatus.setChecked(isOpen);
                        switchStoreStatus.setOnCheckedChangeListener(statusSwitchListener);
                        updateStoreStatusUI(isOpen);

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

    private void toggleShopStatusOnServer(boolean isOpen) {
        if (currentShopId == null) return;
        Map<String, Boolean> body = new HashMap<>();
        body.put("isOpen", isOpen);

        ApiClient.getApiService().toggleShopStatus(currentShopId, body).enqueue(new Callback<ShopResponse>() {
            @Override
            public void onResponse(Call<ShopResponse> call, Response<ShopResponse> response) {
                if (response.isSuccessful()) {
                    updateStoreStatusUI(isOpen);
                    String statusText = isOpen ? "Đã mở cửa nhận đơn" : "Đã đóng cửa nghỉ";
                    Toast.makeText(getContext(), statusText, Toast.LENGTH_SHORT).show();
                } else {
                    // Revert switch on fail
                    switchStoreStatus.setOnCheckedChangeListener(null);
                    switchStoreStatus.setChecked(!isOpen);
                    switchStoreStatus.setOnCheckedChangeListener(statusSwitchListener);
                    Toast.makeText(getContext(), "Không thể cập nhật trạng thái cửa hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ShopResponse> call, Throwable t) {
                // Revert switch on fail
                switchStoreStatus.setOnCheckedChangeListener(null);
                switchStoreStatus.setChecked(!isOpen);
                switchStoreStatus.setOnCheckedChangeListener(statusSwitchListener);
                Toast.makeText(getContext(), "Lỗi mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStoreStatusUI(boolean isOpen) {
        if (isOpen) {
            tvStoreStatusDesc.setText("Đang mở cửa nhận đơn hàng mới");
            tvStoreStatusDesc.setTextColor(getResources().getColor(R.color.status_success));
            switchStoreStatus.setText("OPEN ");
        } else {
            tvStoreStatusDesc.setText("Cửa hàng hiện đang tạm đóng cửa");
            tvStoreStatusDesc.setTextColor(getResources().getColor(R.color.text_secondary));
            switchStoreStatus.setText("CLOSED ");
        }
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
}