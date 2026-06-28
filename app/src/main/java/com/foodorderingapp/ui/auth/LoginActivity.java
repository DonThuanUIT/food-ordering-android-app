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
import android.widget.LinearLayout;
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
    private TextView txtSignUp, txtForgot;
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
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

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
        txtForgot = findViewById(R.id.txtForgot);
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

        txtForgot.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogSubtitle = dialogView.findViewById(R.id.dialogSubtitle);
        
        LinearLayout layoutEmailInput = dialogView.findViewById(R.id.layoutEmailInput);
        EditText dialogEdtEmail = dialogView.findViewById(R.id.dialogEdtEmail);
        Button dialogBtnSendOtp = dialogView.findViewById(R.id.dialogBtnSendOtp);

        LinearLayout layoutResetFields = dialogView.findViewById(R.id.layoutResetFields);
        EditText dialogEdtOtp = dialogView.findViewById(R.id.dialogEdtOtp);
        EditText dialogEdtNewPassword = dialogView.findViewById(R.id.dialogEdtNewPassword);
        EditText dialogEdtConfirmNewPassword = dialogView.findViewById(R.id.dialogEdtConfirmNewPassword);
        Button dialogBtnResetPassword = dialogView.findViewById(R.id.dialogBtnResetPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setNegativeButton("Hủy", (dialogInterface, i) -> dialogInterface.dismiss())
                .create();

        dialogBtnSendOtp.setOnClickListener(v -> {
            String email = dialogEdtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                dialogEdtEmail.setError("Vui lòng nhập email");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                dialogEdtEmail.setError("Email không hợp lệ");
                return;
            }

            dialogBtnSendOtp.setEnabled(false);
            dialogBtnSendOtp.setText("Đang gửi...");

            com.foodorderingapp.data.remote.api.ApiClient.getApiService().sendForgotPasswordOtp(email)
                    .enqueue(new retrofit2.Callback<com.foodorderingapp.model.response.AuthResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.foodorderingapp.model.response.AuthResponse> call, retrofit2.Response<com.foodorderingapp.model.response.AuthResponse> response) {
                            dialogBtnSendOtp.setEnabled(true);
                            dialogBtnSendOtp.setText("Gửi mã OTP");
                            
                            if (response.isSuccessful() && response.body() != null) {
                                ToastUtils.success(LoginActivity.this, "Mã OTP đã được gửi đến email của bạn!");
                                layoutEmailInput.setVisibility(View.GONE);
                                layoutResetFields.setVisibility(View.VISIBLE);
                                dialogSubtitle.setText("Vui lòng kiểm tra email để lấy mã OTP và đặt lại mật khẩu mới.");
                            } else {
                                String errorMsg = "Gửi OTP thất bại! Vui lòng kiểm tra lại email.";
                                try {
                                    if (response.errorBody() != null) {
                                        org.json.JSONObject jsonObject = new org.json.JSONObject(response.errorBody().string());
                                        if (jsonObject.has("message")) {
                                            errorMsg = jsonObject.getString("message");
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing errorBody", e);
                                }
                                ToastUtils.error(LoginActivity.this, errorMsg);
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.foodorderingapp.model.response.AuthResponse> call, Throwable t) {
                            dialogBtnSendOtp.setEnabled(true);
                            dialogBtnSendOtp.setText("Gửi mã OTP");
                            ToastUtils.error(LoginActivity.this, "Lỗi kết nối: " + t.getMessage());
                        }
                    });
        });

        dialogBtnResetPassword.setOnClickListener(v -> {
            String email = dialogEdtEmail.getText().toString().trim();
            String otpCode = dialogEdtOtp.getText().toString().trim();
            String newPassword = dialogEdtNewPassword.getText().toString().trim();
            String confirmNewPassword = dialogEdtConfirmNewPassword.getText().toString().trim();

            if (otpCode.isEmpty() || otpCode.length() < 6) {
                dialogEdtOtp.setError("Mã OTP gồm 6 số");
                return;
            }
            java.util.regex.Pattern passwordPattern = java.util.regex.Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");
            if (!passwordPattern.matcher(newPassword).matches()) {
                dialogEdtNewPassword.setError("Mật khẩu ít nhất 8 ký tự, gồm hoa, thường, số và ký tự đặc biệt");
                return;
            }
            if (!newPassword.equals(confirmNewPassword)) {
                dialogEdtConfirmNewPassword.setError("Mật khẩu không khớp");
                return;
            }

            dialogBtnResetPassword.setEnabled(false);
            dialogBtnResetPassword.setText("Đang thực hiện...");

            com.foodorderingapp.model.request.ResetPasswordRequest resetRequest = new com.foodorderingapp.model.request.ResetPasswordRequest(email, otpCode, newPassword);
            com.foodorderingapp.data.remote.api.ApiClient.getApiService().resetPassword(resetRequest)
                    .enqueue(new retrofit2.Callback<com.foodorderingapp.model.response.AuthResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.foodorderingapp.model.response.AuthResponse> call, retrofit2.Response<com.foodorderingapp.model.response.AuthResponse> response) {
                            dialogBtnResetPassword.setEnabled(true);
                            dialogBtnResetPassword.setText("Đặt lại mật khẩu");

                            if (response.isSuccessful() && response.body() != null) {
                                ToastUtils.success(LoginActivity.this, "Đặt lại mật khẩu thành công!");
                                dialog.dismiss();
                            } else {
                                String errorMsg = "Đặt lại mật khẩu thất bại! Vui lòng thử lại.";
                                try {
                                    if (response.errorBody() != null) {
                                        org.json.JSONObject jsonObject = new org.json.JSONObject(response.errorBody().string());
                                        if (jsonObject.has("message")) {
                                            errorMsg = jsonObject.getString("message");
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing errorBody", e);
                                }
                                ToastUtils.error(LoginActivity.this, errorMsg);
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.foodorderingapp.model.response.AuthResponse> call, Throwable t) {
                            dialogBtnResetPassword.setEnabled(true);
                            dialogBtnResetPassword.setText("Đặt lại mật khẩu");
                            ToastUtils.error(LoginActivity.this, "Lỗi kết nối: " + t.getMessage());
                        }
                    });
        });

        dialog.show();
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
                if (message.toLowerCase().contains("khóa") || message.toLowerCase().contains("khoá")) {
                    showAccountLockedDialog(message);
                } else {
                    ToastUtils.error(this, message);
                }
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

    private void showAccountLockedDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Tài khoản bị khóa ⚠️")
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
