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
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.response.BuildingResponse;
import com.foodorderingapp.utils.ToastUtils;

import java.util.HashMap;
import java.util.List;
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
    private RegisterViewModel viewModel;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private final Pattern phonePattern = Pattern.compile("^\\d{10}$");
    private final Pattern passwordPattern =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");
    private final Map<String, String> buildingMap = new HashMap<>();
    private String defaultBuildingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        initViews();
        setupListeners();
        observeViewModel();
        loadBuildings();
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

        edtBuildingId.setHint("Nhap ten hoac UUID toa nha");
        layoutStudentFields.setVisibility(View.VISIBLE);
        layoutVendorFields.setVisibility(View.GONE);
    }

    private void observeViewModel() {
        viewModel.getIsSuccess().observe(this, success -> {
            if (success != null && success) {
                String email = edtEmail.getText().toString().trim();
                Intent intent = new Intent(RegisterActivity.this, OtpActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getMessage().observe(this, msg -> {
            if (msg != null) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Dang ky");
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

    private void loadBuildings() {
        ApiClient.getApiService().getBuildings().enqueue(new Callback<List<BuildingResponse>>() {
            @Override
            public void onResponse(Call<List<BuildingResponse>> call, Response<List<BuildingResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                buildingMap.clear();
                defaultBuildingId = null;
                for (BuildingResponse building : response.body()) {
                    if (building == null || isBlank(building.getId())) {
                        continue;
                    }
                    if (defaultBuildingId == null) {
                        defaultBuildingId = building.getId();
                    }
                    buildingMap.put(building.getId().toLowerCase(), building.getId());
                    if (!isBlank(building.getName())) {
                        buildingMap.put(building.getName().trim().toLowerCase(), building.getId());
                    }
                }
                if (defaultBuildingId != null && edtBuildingId.getText().toString().trim().isEmpty()) {
                    edtBuildingId.setHint("VD: " + firstBuildingLabel(response.body()));
                }
            }

            @Override
            public void onFailure(Call<List<BuildingResponse>> call, Throwable t) {
                ToastUtils.info(RegisterActivity.this, "Khong tai duoc danh sach toa nha");
            }
        });
    }

    private void handleRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (!validateCommonInput(fullName, email, phone, password, confirmPassword)) {
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Dang xu ly...");
        String buildingId = "";
        String shopName = "";

        if (rbStudent.isChecked()) {
            String input = edtBuildingId.getText().toString().trim().toLowerCase();
            buildingId = input.isEmpty() ? defaultBuildingId : buildingMap.get(input);
            if (isBlank(buildingId)) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Dang ky");
                edtBuildingId.setError("Chon toa nha co trong he thong");
                return;
            }
        } else {
            shopName = edtShopName.getText().toString().trim();
            if (shopName.isEmpty()) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Dang ky");
                edtShopName.setError("Nhap ten quan");
                return;
            }
        }

        viewModel.register(fullName, phone, email, password, rbStudent.isChecked(), buildingId, shopName);
    }

    private boolean validateCommonInput(String fullName, String email, String phone, String password, String confirm) {
        if (fullName.isEmpty()) {
            edtFullName.setError("Nhap ho ten");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email sai");
            return false;
        }
        if (!phonePattern.matcher(phone).matches()) {
            edtPhone.setError("SDT 10 so");
            return false;
        }
        if (!passwordPattern.matcher(password).matches()) {
            edtPassword.setError("Mat khau it nhat 8 ky tu, gom hoa, thuong, so va ky tu dac biet");
            return false;
        }
        if (!password.equals(confirm)) {
            edtConfirmPassword.setError("Mat khau khong khop");
            return false;
        }
        return true;
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        edtPassword.setInputType(isPasswordVisible
                ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edtPassword.setSelection(edtPassword.length());
    }

    private void toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        edtConfirmPassword.setInputType(isConfirmPasswordVisible
                ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edtConfirmPassword.setSelection(edtConfirmPassword.length());
    }

    private String firstBuildingLabel(List<BuildingResponse> values) {
        for (BuildingResponse building : values) {
            if (building != null && !isBlank(building.getName())) {
                return building.getName();
            }
        }
        return "toa nha";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
