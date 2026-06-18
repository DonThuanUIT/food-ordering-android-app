package com.foodorderingapp.ui.home.vendor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.ShopUpdateRequest;
import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.model.response.FoodResponse;
import com.foodorderingapp.ui.auth.LoginActivity;
import com.foodorderingapp.ui.voucher.VoucherManagementActivity;
import com.foodorderingapp.utils.TokenManager;

import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorSettingsFragment extends Fragment {

    private ImageView imgShopCover;
    private ImageView imgShopLogo;
    private TextView tvShopName;
    private TextView tvShopDescription;

    private SwitchCompat switchOperational;
    private TextView tvOperationalStatus;
    private CardView cardStatusIconBg;
    private ImageView ivStatusIcon;

    private TextView btnUpdateHours;
    private TextView tvHoursMonFri;
    private TextView tvHoursSat;
    private TextView tvHoursSun;

    private TextView tvShopEmail;
    private TextView tvShopPhone;
    private Button btnEditContact;

    private TextView tvBankName;
    private TextView tvBankDetails;
    private Button btnEditPayment;

    private CheckBox checkboxOrderAlerts;
    private CheckBox checkboxTurboMode;
    private Button btnDeactivate;

    private View layoutPrefPromo;

    private View btnLogout;

    private View btnEditCover;
    private View btnEditLogo;

    private SharedPreferences sharedPreferences;
    private UUID currentShopId;
    private ShopResponse currentShopData;
    private List<FoodResponse> shopFoodList;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private boolean isEditingLogo = true;

    private final CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            toggleShopStatusOnServer(isChecked);
        }
    };

    private final CompoundButton.OnCheckedChangeListener orderAlertsListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            sharedPreferences.edit().putBoolean("order_alerts", isChecked).apply();
            updatePreference("orderAlertsEnabled", isChecked);
        }
    };



    private final CompoundButton.OnCheckedChangeListener turboModeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            sharedPreferences.edit().putBoolean("turbo_mode", isChecked).apply();
            updatePreference("turboModeEnabled", isChecked);
        }
    };

    private void updatePreference(String field, boolean value) {
        if (currentShopId == null) return;
        ShopUpdateRequest req = new ShopUpdateRequest();
        if ("orderAlertsEnabled".equals(field)) {
            req.setOrderAlertsEnabled(value);
        } else if ("turboModeEnabled".equals(field)) {
            req.setTurboModeEnabled(value);
        }
        saveShopProfile(req);
    }

    public VendorSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences("vendor_settings_pref", Context.MODE_PRIVATE);

        // Image picker callback
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        uploadAndSaveImage(selectedImageUri);
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_settings, container, false);

        // Bind views
        imgShopCover = view.findViewById(R.id.img_shop_cover);
        imgShopLogo = view.findViewById(R.id.img_shop_logo);
        tvShopName = view.findViewById(R.id.tv_shop_name);
        tvShopDescription = view.findViewById(R.id.tv_shop_description);

        switchOperational = view.findViewById(R.id.switch_operational);
        tvOperationalStatus = view.findViewById(R.id.tv_operational_status);
        cardStatusIconBg = view.findViewById(R.id.card_status_icon_bg);
        ivStatusIcon = view.findViewById(R.id.iv_status_icon);

        btnUpdateHours = view.findViewById(R.id.btn_update_hours);
        tvHoursMonFri = view.findViewById(R.id.tv_hours_mon_fri);
        tvHoursSat = view.findViewById(R.id.tv_hours_sat);
        tvHoursSun = view.findViewById(R.id.tv_hours_sun);

        tvShopEmail = view.findViewById(R.id.tv_shop_email);
        tvShopPhone = view.findViewById(R.id.tv_shop_phone);
        btnEditContact = view.findViewById(R.id.btn_edit_contact);

        tvBankName = view.findViewById(R.id.tv_bank_name);
        tvBankDetails = view.findViewById(R.id.tv_bank_details);
        btnEditPayment = view.findViewById(R.id.btn_edit_payment);

        btnLogout = view.findViewById(R.id.btn_logout);

        checkboxOrderAlerts = view.findViewById(R.id.checkbox_order_alerts);
        layoutPrefPromo = view.findViewById(R.id.layout_pref_promo);
        checkboxTurboMode = view.findViewById(R.id.checkbox_turbo_mode);
        btnDeactivate = view.findViewById(R.id.btn_deactivate);

        btnEditCover = view.findViewById(R.id.btn_edit_cover);
        btnEditLogo = view.findViewById(R.id.btn_edit_logo);

        setupClickListeners();
        loadContactAndBankInfo();
        loadSavedPreferences();
        fetchShopInfo();

        return view;
    }

    private void setupClickListeners() {
        // Edit Cover Banner Click
        if (btnEditCover != null) {
            btnEditCover.setOnClickListener(v -> {
                isEditingLogo = false;
                openImagePicker();
            });
        }

        // Edit Logo Click
        if (btnEditLogo != null) {
            btnEditLogo.setOnClickListener(v -> {
                isEditingLogo = true;
                openImagePicker();
            });
        }

        // Profile Details Click (opens edit profile dialog)
        View.OnClickListener profileEditListener = v -> {
            if (currentShopData != null) {
                showEditProfileDialog(currentShopData);
            } else {
                Toast.makeText(getContext(), "Đang tải dữ liệu cửa hàng...", Toast.LENGTH_SHORT).show();
            }
        };
        if (tvShopName != null) tvShopName.setOnClickListener(profileEditListener);
        if (tvShopDescription != null) tvShopDescription.setOnClickListener(profileEditListener);

        // Preference checkboxes listeners
        checkboxOrderAlerts.setOnCheckedChangeListener(orderAlertsListener);
        checkboxTurboMode.setOnCheckedChangeListener(turboModeListener);

        // Business Hour Update button click (opens edit profile dialog)
        btnUpdateHours.setOnClickListener(profileEditListener);

        // Edit Contact Info
        btnEditContact.setOnClickListener(v -> showEditContactDialog());

        // Edit Bank Info
        btnEditPayment.setOnClickListener(v -> showEditBankDialog());

        // Navigation to Voucher Management
        if (layoutPrefPromo != null) {
            layoutPrefPromo.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), VoucherManagementActivity.class);
                startActivity(intent);
            });
        }

        // Logout click listener
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất?")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    TokenManager.getInstance().clearTokens();
                    Toast.makeText(getContext(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
        });

        // Deactivate button click with warning alert dialog
        btnDeactivate.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Vô hiệu hóa Cửa hàng?")
                .setMessage("Bạn có chắc chắn muốn tạm thời vô hiệu hóa cửa hàng? Cửa hàng của bạn sẽ bị ẩn khỏi mọi kết quả tìm kiếm trên DormDash.")
                .setPositiveButton("Vô hiệu hóa", (dialog, which) -> {
                    Toast.makeText(getContext(), "Cửa hàng đã bị vô hiệu hóa tạm thời!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Hủy bỏ", (dialog, which) -> dialog.dismiss())
                .show();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadAndSaveImage(Uri uri) {
        File file = uriToFile(uri);
        if (file == null) return;

        Toast.makeText(getContext(), "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

        RequestBody rb = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), rb);

        ApiClient.getApiService().uploadImage(part).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().get("url");
                    if (url != null) {
                        ShopUpdateRequest req = new ShopUpdateRequest();
                        if (isEditingLogo) {
                            req.setLogoUrl(url);
                        } else {
                            req.setCoverUrl(url);
                        }
                        saveShopProfile(req);
                    }
                } else {
                    Toast.makeText(getContext(), "Tải ảnh lên thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi mạng tải ảnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private File uriToFile(Uri uri) {
        try {
            File file = new File(requireContext().getCacheDir(), "temp_settings_" + System.currentTimeMillis() + ".jpg");
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            FileOutputStream os = new FileOutputStream(file);
            byte[] buf = new byte[1024]; int len;
            while ((len = is.read(buf)) > 0) os.write(buf, 0, len);
            os.close(); is.close();
            return file;
        } catch (Exception e) { return null; }
    }

    private void loadSavedPreferences() {
        checkboxOrderAlerts.setChecked(sharedPreferences.getBoolean("order_alerts", true));
        checkboxTurboMode.setChecked(sharedPreferences.getBoolean("turbo_mode", true));
    }

    private void loadContactAndBankInfo() {
        String email = sharedPreferences.getString("contact_email", "contact@burgerloft.com");
        String phone = sharedPreferences.getString("contact_phone", "+1 (555) 098-7654");
        String bankName = sharedPreferences.getString("bank_name", "Chase Business");
        String bankDetails = sharedPreferences.getString("bank_details", "Ending in •••• 4402");

        if (tvShopEmail != null) tvShopEmail.setText(email);
        if (tvShopPhone != null) tvShopPhone.setText(phone);
        if (tvBankName != null) tvBankName.setText(bankName);
        if (tvBankDetails != null) tvBankDetails.setText(bankDetails);
    }

    private void showEditContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chỉnh sửa thông tin liên hệ");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final android.widget.EditText etEmail = new android.widget.EditText(requireContext());
        etEmail.setHint("Email liên hệ");
        etEmail.setText(tvShopEmail.getText().toString());
        layout.addView(etEmail);

        final android.widget.EditText etPhone = new android.widget.EditText(requireContext());
        etPhone.setHint("Số điện thoại hỗ trợ");
        etPhone.setText(tvShopPhone.getText().toString());
        layout.addView(etPhone);

        builder.setView(layout);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            if (email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            ShopUpdateRequest req = new ShopUpdateRequest();
            req.setEmail(email);
            req.setPhone(phone);
            saveShopProfile(req);
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEditBankDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chỉnh sửa thông tin ngân hàng");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final android.widget.EditText etBankName = new android.widget.EditText(requireContext());
        etBankName.setHint("Tên ngân hàng (ví dụ: Chase Business)");
        etBankName.setText(tvBankName.getText().toString());
        layout.addView(etBankName);

        final android.widget.EditText etAccount = new android.widget.EditText(requireContext());
        etAccount.setHint("Số tài khoản (hoặc 4 số cuối, ví dụ: Ending in •••• 4402)");
        etAccount.setText(tvBankDetails.getText().toString());
        layout.addView(etAccount);

        builder.setView(layout);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = etBankName.getText().toString().trim();
            String acc = etAccount.getText().toString().trim();
            if (name.isEmpty() || acc.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            ShopUpdateRequest req = new ShopUpdateRequest();
            req.setBankName(name);
            req.setBankAccountNumber(acc);
            saveShopProfile(req);
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }



    private void updateOperationalStatusUI(boolean isOpen) {
        if (isOpen) {
            tvOperationalStatus.setText("Open");
            tvOperationalStatus.setTextColor(Color.parseColor("#319795")); // Active Teal
            cardStatusIconBg.setCardBackgroundColor(Color.parseColor("#E6FFFA")); // Light Teal background
            ivStatusIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#319795")));
        } else {
            tvOperationalStatus.setText("Closed");
            tvOperationalStatus.setTextColor(Color.parseColor("#718096")); // Gray text
            cardStatusIconBg.setCardBackgroundColor(Color.parseColor("#EDF2F7")); // Light Gray background
            ivStatusIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#718096")));
        }
    }

    private void fetchShopInfo() {
        ApiClient.getApiService().getVendorShops().enqueue(new Callback<List<ShopResponse>>() {
            @Override
            public void onResponse(Call<List<ShopResponse>> call, Response<List<ShopResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    bindShopData(response.body().get(0));
                }
            }

            @Override
            public void onFailure(Call<List<ShopResponse>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Không thể tải thông tin cửa hàng", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void bindShopData(ShopResponse shop) {
        currentShopData = shop;
        currentShopId = shop.getId() != null ? UUID.fromString(shop.getId()) : null;

        if (tvShopName != null) {
            tvShopName.setText(shop.getName());
        }
        if (tvShopDescription != null) {
            tvShopDescription.setText(shop.getDescription() != null ? shop.getDescription() : "Chưa có mô tả cửa hàng");
        }

        // Hours
        String openStr = formatTime12Hour(shop.getMonFriOpenTime() != null ? shop.getMonFriOpenTime() : shop.getOpenTime());
        String closeStr = formatTime12Hour(shop.getMonFriCloseTime() != null ? shop.getMonFriCloseTime() : shop.getCloseTime());
        if (tvHoursMonFri != null) tvHoursMonFri.setText(openStr + " - " + closeStr);

        String satOpen = shop.getSatOpenTime() != null ? shop.getSatOpenTime() : sharedPreferences.getString("sat_open_time_" + currentShopId, "10:00");
        String satClose = shop.getSatCloseTime() != null ? shop.getSatCloseTime() : sharedPreferences.getString("sat_close_time_" + currentShopId, "01:00");
        if (tvHoursSat != null) tvHoursSat.setText(formatTime12Hour(satOpen) + " - " + formatTime12Hour(satClose));

        String sunOpen = shop.getSunOpenTime() != null ? shop.getSunOpenTime() : sharedPreferences.getString("sun_open_time_" + currentShopId, "10:00");
        String sunClose = shop.getSunCloseTime() != null ? shop.getSunCloseTime() : sharedPreferences.getString("sun_close_time_" + currentShopId, "22:00");
        if (tvHoursSun != null) tvHoursSun.setText(formatTime12Hour(sunOpen) + " - " + formatTime12Hour(sunClose));

        // Operational Status
        boolean isActive = shop.getIsActive() != null ? shop.getIsActive() : true;
        switchOperational.setOnCheckedChangeListener(null);
        switchOperational.setChecked(isActive);
        switchOperational.setOnCheckedChangeListener(switchListener);
        updateOperationalStatusUI(isActive);

        checkboxTurboMode.setOnCheckedChangeListener(null);
        checkboxTurboMode.setChecked(shop.getTurboModeEnabled() != null ? shop.getTurboModeEnabled() : false);
        checkboxTurboMode.setOnCheckedChangeListener(turboModeListener);

        // Contact Info
        String email = shop.getEmail() != null ? shop.getEmail() : sharedPreferences.getString("contact_email", "contact@burgerloft.com");
        String phone = shop.getPhone() != null ? shop.getPhone() : sharedPreferences.getString("contact_phone", "+1 (555) 098-7654");
        if (tvShopEmail != null) tvShopEmail.setText(email);
        if (tvShopPhone != null) tvShopPhone.setText(phone);

        // Bank Info
        String bankName = shop.getBankName() != null ? shop.getBankName() : sharedPreferences.getString("bank_name", "Chase Business");
        String bankDetails = shop.getBankAccountNumber() != null ? shop.getBankAccountNumber() : sharedPreferences.getString("bank_details", "Ending in •• 4402");
        if (tvBankName != null) tvBankName.setText(bankName);
        if (tvBankDetails != null) tvBankDetails.setText(bankDetails);

        // Load custom logo and cover URLs
        String logoUrl = shop.getLogoUrl() != null ? shop.getLogoUrl() : sharedPreferences.getString("shop_logo_url_" + currentShopId, null);
        String coverUrl = shop.getCoverUrl() != null ? shop.getCoverUrl() : sharedPreferences.getString("shop_cover_url_" + currentShopId, null);

        if (getContext() != null) {
            Glide.with(getContext())
                .load(logoUrl != null ? logoUrl : R.drawable.logo_food)
                .placeholder(R.drawable.logo_food)
                .into(imgShopLogo);

            Glide.with(getContext())
                .load(coverUrl != null ? coverUrl : R.drawable.burger_sample)
                .placeholder(R.drawable.burger_sample)
                .into(imgShopCover);
        }


    }

    private String formatTime12Hour(String timeStr) {
        if (timeStr == null || !timeStr.contains(":")) return "08:00 AM";
        try {
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int min = Integer.parseInt(parts[1]);
            String ampm = hour >= 12 ? "PM" : "AM";
            int h = hour % 12;
            if (h == 0) h = 12;
            return String.format(java.util.Locale.US, "%02d:%02d %s", h, min, ampm);
        } catch (Exception e) {
            return timeStr;
        }
    }

    private void toggleShopStatusOnServer(boolean isActive) {
        if (currentShopId == null) return;
        Map<String, Boolean> body = new HashMap<>();
        body.put("isActive", isActive);

        ApiClient.getApiService().toggleShopStatus(currentShopId, body).enqueue(new Callback<ShopResponse>() {
            @Override
            public void onResponse(Call<ShopResponse> call, Response<ShopResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bindShopData(response.body());
                    String status = isActive ? "Open" : "Closed";
                    Toast.makeText(getContext(), "Cửa hàng đã chuyển sang trạng thái: " + status, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Không thể cập nhật trạng thái hoạt động", Toast.LENGTH_SHORT).show();
                    revertSwitchUI(!isActive);
                }
            }

            @Override
            public void onFailure(Call<ShopResponse> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi mạng, không thể cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                }
                revertSwitchUI(!isActive);
            }
        });
    }

    private void revertSwitchUI(boolean targetState) {
        switchOperational.setOnCheckedChangeListener(null);
        switchOperational.setChecked(targetState);
        switchOperational.setOnCheckedChangeListener(switchListener);
        updateOperationalStatusUI(targetState);
    }

    private void showEditProfileDialog(ShopResponse shop) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Update Shop Settings");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        // General Shop Fields
        final android.widget.EditText etName = new android.widget.EditText(requireContext());
        etName.setHint("Shop Name");
        etName.setText(shop.getName());
        layout.addView(etName);

        final android.widget.EditText etDesc = new android.widget.EditText(requireContext());
        etDesc.setHint("Description");
        etDesc.setText(shop.getDescription() != null ? shop.getDescription() : "");
        layout.addView(etDesc);

        final android.widget.EditText etAddress = new android.widget.EditText(requireContext());
        etAddress.setHint("Address");
        etAddress.setText(shop.getAddress() != null ? shop.getAddress() : "");
        layout.addView(etAddress);

        // Section header for hours
        TextView tvHoursHeader = new TextView(requireContext());
        tvHoursHeader.setText("\nCài đặt giờ hoạt động chi tiết:");
        tvHoursHeader.setTextColor(Color.BLACK);
        tvHoursHeader.setTextSize(14);
        tvHoursHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(tvHoursHeader);

        // Spinner to choose Day Group
        final Spinner spinnerDays = new Spinner(requireContext());
        String[] dayGroups = {"Mon - Fri", "Saturday", "Sunday"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, dayGroups);
        spinnerDays.setAdapter(adapter);
        layout.addView(spinnerDays);

        final android.widget.EditText etOpen = new android.widget.EditText(requireContext());
        etOpen.setHint("Giờ mở cửa (HH:mm)");
        layout.addView(etOpen);

        final android.widget.EditText etClose = new android.widget.EditText(requireContext());
        etClose.setHint("Giờ đóng cửa (HH:mm)");
        layout.addView(etClose);

        // Day selection listener to load appropriate hours dynamically
        spinnerDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // Mon - Fri
                    String openVal = shop.getMonFriOpenTime() != null ? shop.getMonFriOpenTime() : (shop.getOpenTime() != null ? shop.getOpenTime() : "08:00");
                    String closeVal = shop.getMonFriCloseTime() != null ? shop.getMonFriCloseTime() : (shop.getCloseTime() != null ? shop.getCloseTime() : "22:00");
                    etOpen.setText(openVal.substring(0, 5));
                    etClose.setText(closeVal.substring(0, 5));
                } else if (position == 1) { // Saturday
                    String openVal = shop.getSatOpenTime() != null ? shop.getSatOpenTime() : "10:00";
                    String closeVal = shop.getSatCloseTime() != null ? shop.getSatCloseTime() : "01:00";
                    etOpen.setText(openVal.substring(0, 5));
                    etClose.setText(closeVal.substring(0, 5));
                } else { // Sunday
                    String openVal = shop.getSunOpenTime() != null ? shop.getSunOpenTime() : "10:00";
                    String closeVal = shop.getSunCloseTime() != null ? shop.getSunCloseTime() : "22:00";
                    etOpen.setText(openVal.substring(0, 5));
                    etClose.setText(closeVal.substring(0, 5));
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String addr = etAddress.getText().toString().trim();
            String open = etOpen.getText().toString().trim();
            String close = etClose.getText().toString().trim();
            int selectedDayPos = spinnerDays.getSelectedItemPosition();

            if (name.isEmpty() || open.isEmpty() || close.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save hours to respective day groups
            if (selectedDayPos == 0) { // Mon - Fri (Sent to backend)
                ShopUpdateRequest req = new ShopUpdateRequest(name, addr, desc, open, close);
                req.setMonFriOpenTime(open);
                req.setMonFriCloseTime(close);
                saveShopProfile(req);
            } else if (selectedDayPos == 1) { // Saturday
                ShopUpdateRequest req = new ShopUpdateRequest(name, addr, desc, shop.getOpenTime() != null ? shop.getOpenTime().substring(0,5) : "08:00", shop.getCloseTime() != null ? shop.getCloseTime().substring(0,5) : "22:00");
                req.setSatOpenTime(open);
                req.setSatCloseTime(close);
                saveShopProfile(req);
            } else { // Sunday
                ShopUpdateRequest req = new ShopUpdateRequest(name, addr, desc, shop.getOpenTime() != null ? shop.getOpenTime().substring(0,5) : "08:00", shop.getCloseTime() != null ? shop.getCloseTime().substring(0,5) : "22:00");
                req.setSunOpenTime(open);
                req.setSunCloseTime(close);
                saveShopProfile(req);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void saveShopProfile(ShopUpdateRequest req) {
        if (currentShopId == null) return;
        ApiClient.getApiService().updateShopProfile(currentShopId, req).enqueue(new Callback<ShopResponse>() {
            @Override
            public void onResponse(Call<ShopResponse> call, Response<ShopResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Cập nhật thông tin quán ăn thành công!", Toast.LENGTH_SHORT).show();
                    bindShopData(response.body());
                } else {
                    Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ShopResponse> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showManageLeadsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Danh sách khách hàng tiềm năng");

        String[] leads = {
            "● Nguyễn Văn A - Yêu cầu hợp tác làm Campus Event cơm trưa",
            "● Trần Thị B - Đặt tiệc trà bánh sự kiện CLB (150 khách)",
            "● Lê Văn C - Đăng ký giao cơm suất hàng tuần (20 phần/ngày)",
            "● Phạm Thị D - Đăng ký nhận thông báo món mới qua Email"
        };

        builder.setItems(leads, (dialog, which) -> {
            Toast.makeText(getContext(), "Đang xử lý yêu cầu: " + leads[which], Toast.LENGTH_SHORT).show();
        });
        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showStatementsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Lịch sử doanh thu & Sao kê");

        String[] statements = {
            "Tháng 06/2026 - Doanh thu: $1,280.50 (124 đơn) - ĐÃ DUYỆT CHI",
            "Tháng 05/2026 - Doanh thu: $2,450.00 (236 đơn) - ĐÃ DUYỆT CHI",
            "Tháng 04/2026 - Doanh thu: $1,890.20 (182 đơn) - ĐÃ DUYỆT CHI",
            "Tháng 03/2026 - Doanh thu: $980.00 (92 đơn) - ĐÃ DUYỆT CHI"
        };

        builder.setItems(statements, (dialog, which) -> {
            Toast.makeText(getContext(), "Mở chi tiết sao kê: " + statements[which].split(" - ")[0], Toast.LENGTH_SHORT).show();
        });
        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}