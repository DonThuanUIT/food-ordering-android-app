package com.foodorderingapp.ui.voucher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.model.response.VoucherResponse;
import com.foodorderingapp.ui.adapter.VoucherAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoucherManagementActivity extends AppCompatActivity implements VoucherAdapter.VoucherActionHandler {

    private RecyclerView rvVouchers;
    private View layoutEmptyState;
    private FloatingActionButton fabAddVoucher;
    private VoucherAdapter adapter;
    private final List<VoucherResponse> voucherList = new ArrayList<>();
    private UUID currentShopId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_management);

        rvVouchers = findViewById(R.id.rv_vouchers);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        fabAddVoucher = findViewById(R.id.fab_add_voucher);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VoucherAdapter(voucherList, this);
        rvVouchers.setAdapter(adapter);

        fabAddVoucher.setOnClickListener(v -> {
            if (currentShopId != null) {
                Intent intent = new Intent(VoucherManagementActivity.this, VoucherFormActivity.class);
                intent.putExtra("SHOP_ID", currentShopId.toString());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Chưa xác định được cửa hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchShopAndLoadVouchers();
    }

    private void fetchShopAndLoadVouchers() {
        ApiClient.getApiService().getVendorShops().enqueue(new Callback<List<ShopResponse>>() {
            @Override
            public void onResponse(Call<List<ShopResponse>> call, Response<List<ShopResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String idStr = response.body().get(0).getId();
                    if (idStr != null) {
                        currentShopId = UUID.fromString(idStr);
                        loadVouchers();
                    }
                } else {
                    Toast.makeText(VoucherManagementActivity.this, "Không thể xác minh thông tin cửa hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ShopResponse>> call, Throwable t) {
                Toast.makeText(VoucherManagementActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVouchers() {
        if (currentShopId == null) return;
        ApiClient.getApiService().getShopVouchers(currentShopId).enqueue(new Callback<List<VoucherResponse>>() {
            @Override
            public void onResponse(Call<List<VoucherResponse>> call, Response<List<VoucherResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<VoucherResponse> body = response.body();
                    adapter.updateData(body);
                    if (body.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        rvVouchers.setVisibility(View.GONE);
                    } else {
                        layoutEmptyState.setVisibility(View.GONE);
                        rvVouchers.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(VoucherManagementActivity.this, "Không thể tải danh sách voucher", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VoucherResponse>> call, Throwable t) {
                Toast.makeText(VoucherManagementActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStatusToggled(VoucherResponse voucher, boolean isActive) {
        if (currentShopId == null || voucher.getId() == null) return;
        Map<String, Boolean> body = new HashMap<>();
        body.put("isActive", isActive);

        ApiClient.getApiService().toggleVoucherStatus(currentShopId, voucher.getId(), body).enqueue(new Callback<VoucherResponse>() {
            @Override
            public void onResponse(Call<VoucherResponse> call, Response<VoucherResponse> response) {
                if (response.isSuccessful()) {
                    String statusStr = isActive ? "đã kích hoạt" : "đã tắt kích hoạt";
                    Toast.makeText(VoucherManagementActivity.this, "Voucher " + voucher.getCode() + " " + statusStr, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VoucherManagementActivity.this, "Không thể cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                    loadVouchers(); // Refresh list to revert UI state
                }
            }

            @Override
            public void onFailure(Call<VoucherResponse> call, Throwable t) {
                Toast.makeText(VoucherManagementActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadVouchers(); // Refresh list to revert UI state
            }
        });
    }

    @Override
    public void onEditClicked(VoucherResponse voucher) {
        if (currentShopId == null) return;
        Intent intent = new Intent(this, VoucherFormActivity.class);
        intent.putExtra("SHOP_ID", currentShopId.toString());
        intent.putExtra("VOUCHER_ID", voucher.getId().toString());
        intent.putExtra("CODE", voucher.getCode());
        intent.putExtra("TITLE", voucher.getTitle());
        intent.putExtra("DISCOUNT_TYPE", voucher.getDiscountType());
        intent.putExtra("DISCOUNT_VALUE", voucher.getDiscountValue().toString());
        intent.putExtra("MIN_ORDER_VALUE", voucher.getMinOrderValue() != null ? voucher.getMinOrderValue().toString() : "0");
        intent.putExtra("MAX_DISCOUNT_VALUE", voucher.getMaxDiscountValue() != null ? voucher.getMaxDiscountValue().toString() : "");
        intent.putExtra("APPLY_TYPE", voucher.getApplyType());
        intent.putExtra("START_DATE", voucher.getStartDate());
        intent.putExtra("END_DATE", voucher.getEndDate());
        intent.putExtra("IS_ACTIVE", voucher.getActive() != null ? voucher.getActive() : false);
        
        // Pass foodIds list if present
        if (voucher.getFoodIds() != null && !voucher.getFoodIds().isEmpty()) {
            ArrayList<String> foodIdsStrList = new ArrayList<>();
            for (UUID uuid : voucher.getFoodIds()) {
                foodIdsStrList.add(uuid.toString());
            }
            intent.putStringArrayListExtra("FOOD_IDS", foodIdsStrList);
        }
        
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(VoucherResponse voucher) {
        if (currentShopId == null || voucher.getId() == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Xóa Voucher?")
                .setMessage("Bạn có chắc chắn muốn xóa voucher " + voucher.getCode() + "? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteVoucherOnServer(voucher.getId()))
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteVoucherOnServer(UUID voucherId) {
        ApiClient.getApiService().deleteVoucher(currentShopId, voucherId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == 204) {
                    Toast.makeText(VoucherManagementActivity.this, "Đã xóa voucher thành công!", Toast.LENGTH_SHORT).show();
                    loadVouchers();
                } else {
                    Toast.makeText(VoucherManagementActivity.this, "Không thể xóa voucher", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(VoucherManagementActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
