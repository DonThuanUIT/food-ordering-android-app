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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.foodorderingapp.R;
import com.foodorderingapp.utils.ToastUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtPhone, edtPassword, edtConfirmPassword, edtFullName, edtEmail;
    private EditText edtBuildingId, edtShopName, edtDescription, edtOpenTime, edtCloseTime;
    private RadioGroup rgRole;
    private RadioButton rbStudent;
    private LinearLayout layoutStudentFields, layoutVendorFields;
    private Button btnRegister;
    private ImageView imgEyePassword, imgEyeConfirmPassword;

    private RegisterViewModel viewModel;

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

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        initBuildingMap();
        initViews();
        setupListeners();
        observeViewModel();
    }

    private void initBuildingMap() {
        buildingMap.put("e01", "11111111-1111-1111-1111-111111111111");
        buildingMap.put("e02", "22222222-2222-2222-2222-222222222222");
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

        layoutStudentFields.setVisibility(View.VISIBLE);
        layoutVendorFields.setVisibility(View.GONE);
    }

    // Trong RegisterActivity.java
    private void observeViewModel() {
        viewModel.getIsSuccess().observe(this, success -> {
            // Chỉ chuyển màn khi success đích thân là TRUE
            if (success != null && success) {
                // Lấy email trực tiếp từ EditText
                String email = edtEmail.getText().toString().trim();

                Intent intent = new Intent(RegisterActivity.this, OtpActivity.class);
                intent.putExtra("email", email); // Truyền email sang màn OTP
                startActivity(intent);
                finish(); // Đóng màn hình đăng ký
            }
        });

        // Đừng quên observe message để hiện thông báo lỗi
        viewModel.getMessage().observe(this, msg -> {
            if (msg != null) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");
                ToastUtils.info(this, msg);
            }
        });
    }

    private void setupListeners() {
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isStudent = (checkedId == R.id.rbStudent);
            layoutStudentFields.setVisibility(isStudent ? View.VISIBLE : View.GONE);
            layoutVendorFields.setVisibility(isStudent ? View.GONE : View.VISIBLE);
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

        btnRegister.setEnabled(false);
        btnRegister.setText("Đang xử lý...");
        String buildingId = "";
        String shopName = "";

        if (rbStudent.isChecked()) {
            String code = edtBuildingId.getText().toString().trim().toLowerCase();
            buildingId = buildingMap.getOrDefault(code, buildingMap.get("e01"));
        } else {
            shopName = edtShopName.getText().toString().trim();
            if (shopName.isEmpty()) {
                edtShopName.setError("Nhập tên quán");
                return;
            }
        }

        viewModel.register(fullName, phone, email, password, rbStudent.isChecked(), buildingId, shopName);
    }

    private boolean validateCommonInput(String fullName, String email, String phone, String password, String confirm) {
        if (fullName.isEmpty()) { edtFullName.setError("Nhập họ tên"); return false; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { edtEmail.setError("Email sai"); return false; }
        if (!PHONE_PATTERN.matcher(phone).matches()) { edtPhone.setError("SĐT 10 số"); return false; }
        if (!PASSWORD_PATTERN.matcher(password).matches()) { edtPassword.setError("Mật khẩu ít nhất 8 ký tự, gồm hoa, thường, số và ký tự đặc biệt"); return false; }
        if (!password.equals(confirm)) { edtConfirmPassword.setError("Mật khẩu không khớp"); return false; }
        return true;
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        edtPassword.setInputType(isPasswordVisible ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edtPassword.setSelection(edtPassword.length());
    }

    private void toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        edtConfirmPassword.setInputType(isConfirmPasswordVisible ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edtConfirmPassword.setSelection(edtConfirmPassword.length());
    }
}
