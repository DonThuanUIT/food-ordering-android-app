package com.foodorderingapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.foodorderingapp.MainActivity;
import com.foodorderingapp.R;
import com.foodorderingapp.utils.ToastUtils;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LOGIN_DEBUG";
    private TextView txtSignUp;
    private EditText edtPhone, edtPassword;
    private ImageView imgEye;
    private Button btnLogin;
    private ImageButton btnBiometric;
    private CheckBox cbRemember;
    private ProgressBar progressBar;
    private LoginViewModel loginViewModel;
    private boolean isPasswordVisible = false;
    private String tempPasswordToSave = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("app_dark_mode", prefs.getBoolean("vendor_dark_mode", false));
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        initViews();
        setupListeners();
        observeViewModel();
        checkBiometricSupport();

        // Pre-fill phone if saved
        String savedPhone = com.foodorderingapp.utils.TokenManager.getInstance().getPhone();
        if (savedPhone != null) {
            edtPhone.setText(savedPhone);
            cbRemember.setChecked(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        txtSignUp = findViewById(R.id.txtSignUp);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        imgEye = findViewById(R.id.imgEye);
        btnLogin = findViewById(R.id.btnLogin);
        btnBiometric = findViewById(R.id.btnBiometric);
        cbRemember = findViewById(R.id.cbRemember);
        progressBar = findViewById(R.id.progressBar); 
    }

    private void checkBiometricSupport() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
            btnBiometric.setVisibility(View.VISIBLE);
        } else {
            btnBiometric.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        txtSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        imgEye.setOnClickListener(v -> togglePasswordVisibility());

        btnLogin.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            Log.d(TAG, "Attempting login for phone: " + phone);

            if (cbRemember.isChecked()) {
                tempPasswordToSave = password;
            } else {
                tempPasswordToSave = null;
                com.foodorderingapp.utils.TokenManager.getInstance().savePassword(null);
            }

            loginViewModel.login(phone, password);
        });

        btnBiometric.setOnClickListener(v -> {
            String savedPhone = com.foodorderingapp.utils.TokenManager.getInstance().getPhone();
            String savedPassword = com.foodorderingapp.utils.TokenManager.getInstance().getPassword();
            if (savedPhone == null || savedPassword == null) {
                showLinkBiometricDialog();
            } else {
                showBiometricPrompt(savedPhone, savedPassword);
            }
        });
    }

    private void showLinkBiometricDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_link_biometric, null);

        EditText dialogEdtPhone = dialogView.findViewById(R.id.dialogEdtPhone);
        EditText dialogEdtPassword = dialogView.findViewById(R.id.dialogEdtPassword);

        String phoneInput = edtPhone.getText().toString().trim();
        if (!phoneInput.isEmpty()) {
            dialogEdtPhone.setText(phoneInput);
        } else {
            String savedPhone = com.foodorderingapp.utils.TokenManager.getInstance().getPhone();
            if (savedPhone != null) {
                dialogEdtPhone.setText(savedPhone);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Xác nhận", null)
                .setNegativeButton("Hủy", (dialogInterface, i) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String phone = dialogEdtPhone.getText().toString().trim();
                String password = dialogEdtPassword.getText().toString().trim();

                if (phone.isEmpty()) {
                    ToastUtils.error(LoginActivity.this, "Số điện thoại không được để trống");
                    return;
                }
                if (password.isEmpty()) {
                    ToastUtils.error(LoginActivity.this, "Mật khẩu không được để trống");
                    return;
                }

                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                button.setEnabled(false);

                com.foodorderingapp.model.request.LoginRequest request = new com.foodorderingapp.model.request.LoginRequest(phone, password);
                com.foodorderingapp.data.remote.api.ApiClient.getApiService().login(request)
                        .enqueue(new retrofit2.Callback<com.foodorderingapp.model.response.AuthResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<com.foodorderingapp.model.response.AuthResponse> call, retrofit2.Response<com.foodorderingapp.model.response.AuthResponse> response) {
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                button.setEnabled(true);

                                if (response.isSuccessful() && response.body() != null) {
                                    dialog.dismiss();
                                    promptBiometricToLink(phone, password);
                                } else {
                                    ToastUtils.error(LoginActivity.this, "Tài khoản hoặc mật khẩu không chính xác");
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<com.foodorderingapp.model.response.AuthResponse> call, Throwable t) {
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                button.setEnabled(true);
                                ToastUtils.error(LoginActivity.this, "Lỗi kết nối: " + t.getMessage());
                            }
                        });
            });
        });

        dialog.show();
    }

    private void promptBiometricToLink(String phone, String password) {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                ToastUtils.error(LoginActivity.this, "Liên kết thất bại: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                com.foodorderingapp.utils.TokenManager.getInstance().savePhone(phone);
                com.foodorderingapp.utils.TokenManager.getInstance().savePassword(password);
                ToastUtils.success(LoginActivity.this, "Liên kết vân tay thành công! Nhấn lại nút vân tay để đăng nhập.");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                ToastUtils.error(LoginActivity.this, "Xác thực vân tay thất bại, vui lòng thử lại.");
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Liên kết vân tay")
                .setSubtitle("Quét vân tay của bạn để xác nhận liên kết")
                .setNegativeButtonText("Hủy")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void showBiometricPrompt(String phone, String password) {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                ToastUtils.error(LoginActivity.this, "Lỗi xác thực: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                ToastUtils.success(LoginActivity.this, "Xác thực thành công!");
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                loginViewModel.login(phone, password);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                ToastUtils.error(LoginActivity.this, "Xác thực vân tay thất bại");
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Đăng nhập vân tay")
                .setSubtitle("Đặt vân tay lên cảm biến để đăng nhập nhanh")
                .setNegativeButtonText("Hủy")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void observeViewModel() {
        loginViewModel.getIsLoading().observe(this, isLoading -> {
            btnLogin.setEnabled(!isLoading);
            btnBiometric.setEnabled(!isLoading);
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        loginViewModel.getErrorMessage().observe(this, message -> {
            if (message != null) {
                ToastUtils.error(this, message);
                Log.e(TAG, "Login Error: " + message);
            }
        });

        loginViewModel.getLoginResponse().observe(this, response -> {
            if (response != null) {
                Log.d(TAG, "response=" + response.getMessage());
                Log.d(TAG, "role=" + response.getRole());
                Log.d(TAG, "token=" + (response.getAccessToken() != null ? "FOUND" : "MISSING"));

                ToastUtils.success(this, "Đăng nhập thành công!");

                if (tempPasswordToSave != null) {
                    com.foodorderingapp.utils.TokenManager.getInstance().savePassword(tempPasswordToSave);
                }

                String userRole = response.getRole();

                // Chuyển sang MainActivity và truyền Role
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("USER_ROLE", userRole);

                startActivity(intent);
                finish(); // Đóng LoginActivity sau khi chuyển màn
            }
        });
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
}
