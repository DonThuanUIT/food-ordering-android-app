package com.foodorderingapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.RegisterRequest;
import com.foodorderingapp.model.response.ApiError;
import com.foodorderingapp.model.response.RegisterResponse;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtPhone, edtPassword, edtConfirmPassword, edtFullName, edtEmail;
    private EditText edtBuildingId, edtShopName, edtDescription, edtOpenTime, edtCloseTime;
    private RadioGroup rgRole;
    private RadioButton rbStudent;
    private LinearLayout layoutStudentFields, layoutVendorFields;
    private Button btnRegister;
    private ImageView imgEyePassword, imgEyeConfirmPassword;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    private final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");

    private final Map<String, String> buildingMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initBuildingMap();
        initViews();
        setupListeners();
    }

    private void initBuildingMap() {
        buildingMap.put("e01", "11111111-1111-1111-1111-111111111111");
        buildingMap.put("e02", "22222222-2222-2222-2222-222222222222");
        buildingMap.put("e03", "33333333-3333-3333-3333-333333333333");
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        
        edtBuildingId = findViewById(R.id.edtBuildingId);
        edtShopName = findViewById(R.id.edtShopName);
        edtDescription = findViewById(R.id.edtDescription);
        edtOpenTime = findViewById(R.id.edtOpenTime);
        edtCloseTime = findViewById(R.id.edtCloseTime);

        rgRole = findViewById(R.id.rgRole);
        rbStudent = findViewById(R.id.rbStudent);
        layoutStudentFields = findViewById(R.id.layoutStudentFields);
        layoutVendorFields = findViewById(R.id.layoutVendorFields);
        
        btnRegister = findViewById(R.id.btnRegister);
        imgEyePassword = findViewById(R.id.imgEyePassword);
        imgEyeConfirmPassword = findViewById(R.id.imgEyeConfirmPassword);

        // Mặc định hiện Student fields
        layoutStudentFields.setVisibility(View.VISIBLE);
        layoutVendorFields.setVisibility(View.GONE);
    }

    private void setupListeners() {
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbStudent) {
                layoutStudentFields.setVisibility(View.VISIBLE);
                layoutVendorFields.setVisibility(View.GONE);
            } else {
                layoutStudentFields.setVisibility(View.GONE);
                layoutVendorFields.setVisibility(View.VISIBLE);
            }
        });

        imgEyePassword.setOnClickListener(v -> togglePasswordVisibility());
        imgEyeConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());

        btnRegister.setOnClickListener(v -> handleRegister());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (!validateCommonInput(fullName, email, phone, password, confirmPassword)) return;

        if (rbStudent.isChecked()) {
            registerStudent(fullName, email, phone, password);
        } else {
            registerVendor(fullName, email, phone, password);
        }
    }

    private void registerStudent(String fullName, String email, String phone, String password) {
        String selectedBuildingCode = edtBuildingId.getText().toString().trim().toLowerCase();
        
        // Map building code sang UUID format
        String buildingId = buildingMap.get(selectedBuildingCode);
        
        if (buildingId == null) {
            // Nếu không tìm thấy, mặc định gửi UUID của e01 để qua bước parse của backend
            buildingId = buildingMap.get("e01");
        }

        RegisterRequest request = new RegisterRequest(phone, password, fullName, email, buildingId);
        callRegisterApi(ApiClient.getApiService().registerStudent(request));
    }

    private void registerVendor(String fullName, String email, String phone, String password) {
        String shopName = edtShopName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String openTime = edtOpenTime.getText().toString().trim();
        String closeTime = edtCloseTime.getText().toString().trim();

        if (shopName.isEmpty()) {
            edtShopName.setError("Shop name is required");
            return;
        }

        RegisterRequest request = new RegisterRequest(phone, password, fullName, email, 
                shopName, description, openTime, closeTime);
        callRegisterApi(ApiClient.getApiService().registerVendor(request));
    }

    private void callRegisterApi(Call<RegisterResponse> call) {
        btnRegister.setEnabled(false);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    
                    // Chuyển sang màn hình Login sau khi đăng ký thành công
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    // Xóa stack để khi nhấn back ở Login sẽ thoát app thay vì quay lại Register
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    showApiError(response);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateCommonInput(String fullName, String email, String phone, String password, String confirm) {
        if (fullName.isEmpty()) { edtFullName.setError("Nhập họ tên"); return false; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { edtEmail.setError("Email sai định dạng"); return false; }
        if (!PHONE_PATTERN.matcher(phone).matches()) { edtPhone.setError("SĐT phải đủ 10 số"); return false; }
        if (!PASSWORD_PATTERN.matcher(password).matches()) { 
            edtPassword.setError("Mật khẩu yếu (Cần chữ hoa, thường, số, ký tự đặc biệt, >=8 ký tự)"); 
            return false; 
        }
        if (!password.equals(confirm)) { edtConfirmPassword.setError("Mật khẩu không khớp"); return false; }
        return true;
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        edtPassword.setSelection(edtPassword.length());
    }

    private void toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        if (isConfirmPasswordVisible) {
            edtConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        edtConfirmPassword.setSelection(edtConfirmPassword.length());
    }

    private void showApiError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                ApiError error = new Gson().fromJson(response.errorBody().string(), ApiError.class);
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi hệ thống", Toast.LENGTH_SHORT).show();
        }
    }
}
