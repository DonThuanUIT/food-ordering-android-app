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
import android.widget.ScrollView;
import android.widget.TextView;

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
    private RadioButton rbStudent, rbOwner;
    private LinearLayout layoutStudentFields, layoutVendorFields;
    private Button btnRegister, btnStepBack, btnStepNext;
    private ImageView imgEyePassword, imgEyeConfirmPassword;
    private ScrollView registerScroll;
    private TextView txtStepLabel, txtStepTitle, txtStepSubtitle;
    private View[] stepViews;
    private View[] dotViews;
    private RegisterViewModel viewModel;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private int currentStep = STEP_ROLE;

    private static final int STEP_ROLE = 0;
    private static final int STEP_ACCOUNT = 1;
    private static final int STEP_DETAILS = 2;
    private static final int STEP_PASSWORD = 3;
    private static final int STEP_COUNT = 4;

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
        rbOwner = findViewById(R.id.rbOwner);
        layoutStudentFields = findViewById(R.id.layoutStudentFields);
        layoutVendorFields = findViewById(R.id.layoutVendorFields);
        btnRegister = findViewById(R.id.btnRegister);
        btnStepBack = findViewById(R.id.btnStepBack);
        btnStepNext = findViewById(R.id.btnStepNext);
        imgEyePassword = findViewById(R.id.imgEyePassword);
        imgEyeConfirmPassword = findViewById(R.id.imgEyeConfirmPassword);
        registerScroll = findViewById(R.id.registerScroll);
        txtStepLabel = findViewById(R.id.txtStepLabel);
        txtStepTitle = findViewById(R.id.txtStepTitle);
        txtStepSubtitle = findViewById(R.id.txtStepSubtitle);
        stepViews = new View[]{
                findViewById(R.id.stepRole),
                findViewById(R.id.stepAccount),
                findViewById(R.id.stepDetails),
                findViewById(R.id.stepPassword)
        };
        dotViews = new View[]{
                findViewById(R.id.dotStepRole),
                findViewById(R.id.dotStepAccount),
                findViewById(R.id.dotStepDetails),
                findViewById(R.id.dotStepPassword)
        };

        edtBuildingId.setHint("Nhập tên hoặc UUID tòa nhà");
        layoutStudentFields.setVisibility(View.VISIBLE);
        layoutVendorFields.setVisibility(View.GONE);
        updateStepUi();
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
            if (currentStep == STEP_ROLE) {
                showStep(STEP_ACCOUNT, true);
            }
        });

        rbStudent.setOnClickListener(v -> {
            if (currentStep == STEP_ROLE) {
                showStep(STEP_ACCOUNT, true);
            }
        });
        rbOwner.setOnClickListener(v -> {
            if (currentStep == STEP_ROLE) {
                showStep(STEP_ACCOUNT, true);
            }
        });
        btnStepBack.setOnClickListener(v -> {
            if (currentStep > STEP_ROLE) {
                showStep(currentStep - 1, false);
            }
        });
        btnStepNext.setOnClickListener(v -> {
            if (validateCurrentStep() && currentStep < STEP_PASSWORD) {
                showStep(currentStep + 1, true);
            }
        });
        imgEyePassword.setOnClickListener(v -> togglePasswordVisibility());
        imgEyeConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());
        btnRegister.setOnClickListener(v -> handleRegister());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void showStep(int nextStep, boolean forward) {
        if (nextStep < STEP_ROLE || nextStep >= STEP_COUNT || nextStep == currentStep) {
            return;
        }

        View currentView = stepViews[currentStep];
        View nextView = stepViews[nextStep];
        int distance = getResources().getDisplayMetrics().widthPixels;
        int startOffset = forward ? distance : -distance;
        int endOffset = forward ? -distance : distance;

        nextView.setVisibility(View.VISIBLE);
        nextView.setTranslationX(startOffset);
        nextView.setAlpha(0f);

        currentView.animate()
                .translationX(endOffset)
                .alpha(0f)
                .setDuration(220)
                .withEndAction(() -> {
                    currentView.setVisibility(View.GONE);
                    currentView.setTranslationX(0f);
                    currentView.setAlpha(1f);
                })
                .start();

        nextView.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(220)
                .start();

        currentStep = nextStep;
        updateStepUi();
        registerScroll.post(() -> registerScroll.smoothScrollTo(0, 0));
    }

    private void updateStepUi() {
        String[] titles = {"Bạn là?", "Thông tin tài khoản", "Thông tin đăng ký", "Bảo mật tài khoản"};
        String[] subtitles = {
                "Chọn vai trò để UniEats chuẩn bị đúng thông tin cần đăng ký.",
                "Nhập thông tin liên hệ để nhận mã xác thực OTP.",
                rbStudent.isChecked()
                        ? "Chọn tòa nhà bạn thường nhận món."
                        : "Thêm thông tin cơ bản cho quán của bạn.",
                "Tạo mật khẩu đủ mạnh để bảo vệ tài khoản."
        };

        txtStepLabel.setText("Bước " + (currentStep + 1) + "/" + STEP_COUNT);
        txtStepTitle.setText(titles[currentStep]);
        txtStepSubtitle.setText(subtitles[currentStep]);
        btnStepBack.setVisibility(currentStep == STEP_ROLE ? View.GONE : View.VISIBLE);
        btnStepNext.setVisibility(currentStep == STEP_PASSWORD ? View.GONE : View.VISIBLE);
        btnRegister.setVisibility(currentStep == STEP_PASSWORD ? View.VISIBLE : View.GONE);

        for (int i = 0; i < dotViews.length; i++) {
            dotViews[i].setBackgroundResource(i <= currentStep
                    ? R.drawable.bg_tab_indicator
                    : R.drawable.bg_edittext);
        }
    }

    private boolean validateCurrentStep() {
        if (currentStep == STEP_ACCOUNT) {
            return validateAccountInput();
        }
        if (currentStep == STEP_DETAILS) {
            return validateRoleDetails();
        }
        if (currentStep == STEP_PASSWORD) {
            return validatePasswordInput();
        }
        return true;
    }

    private boolean validateAccountInput() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (fullName.isEmpty()) {
            edtFullName.setError("Nhập họ tên");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            return false;
        }
        if (!phonePattern.matcher(phone).matches()) {
            edtPhone.setError("Số điện thoại cần 10 số");
            return false;
        }
        return true;
    }

    private boolean validateRoleDetails() {
        if (rbStudent.isChecked()) {
            if (isBlank(resolveBuildingId())) {
                edtBuildingId.setError("Chọn tòa nhà có trong hệ thống");
                return false;
            }
            return true;
        }

        if (edtShopName.getText().toString().trim().isEmpty()) {
            edtShopName.setError("Nhập tên quán");
            return false;
        }
        return true;
    }

    private boolean validatePasswordInput() {
        String password = edtPassword.getText().toString().trim();
        String confirm = edtConfirmPassword.getText().toString().trim();

        if (!passwordPattern.matcher(password).matches()) {
            edtPassword.setError("Mật khẩu ít nhất 8 ký tự, gồm hoa, thường, số và ký tự đặc biệt");
            return false;
        }
        if (!password.equals(confirm)) {
            edtConfirmPassword.setError("Mật khẩu không khớp");
            return false;
        }
        return true;
    }

    private String resolveBuildingId() {
        String input = edtBuildingId.getText().toString().trim().toLowerCase();
        if (input.isEmpty()) {
            return defaultBuildingId;
        }
        return buildingMap.get(input);
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
                ToastUtils.info(RegisterActivity.this, "Không tải được danh sách tòa nhà");
            }
        });
    }

    private void handleRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (!validateAccountInput()) {
            showStep(STEP_ACCOUNT, false);
            return;
        }
        if (!validateRoleDetails()) {
            showStep(STEP_DETAILS, false);
            return;
        }
        if (!validatePasswordInput()) {
            showStep(STEP_PASSWORD, true);
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Đang xử lý...");

        String buildingId = rbStudent.isChecked() ? resolveBuildingId() : "";
        String shopName = rbStudent.isChecked() ? "" : edtShopName.getText().toString().trim();
        viewModel.register(fullName, phone, email, password, rbStudent.isChecked(), buildingId, shopName);
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
