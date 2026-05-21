package com.foodorderingapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.foodorderingapp.MainActivity;
import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.ResendOtpRequest;
import com.foodorderingapp.model.response.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LOGIN_DEBUG";
    private TextView txtSignUp, txtForgot;
    private EditText edtPhone, edtPassword;
    private ImageView imgEye;
    private Button btnLogin;
    private ProgressBar progressBar;
    private LoginViewModel loginViewModel;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        initViews();
        setupListeners();
        observeViewModel();

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
        // Lưu ý: Nếu activity_login.xml chưa có ProgressBar, bạn có thể thêm id="progressBar" vào XML
        progressBar = findViewById(R.id.progressBar); 
    }

    private void setupListeners() {
        txtSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        imgEye.setOnClickListener(v -> togglePasswordVisibility());

        txtForgot.setOnClickListener(v -> showForgotPasswordDialog());

        btnLogin.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            Log.d(TAG, "Attempting login for phone: " + phone);
            loginViewModel.login(phone, password);
        });
    }

    private void observeViewModel() {
        loginViewModel.getIsLoading().observe(this, isLoading -> {
            btnLogin.setEnabled(!isLoading);
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        loginViewModel.getErrorMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Login Error: " + message);
            }
        });

        loginViewModel.getLoginResponse().observe(this, response -> {
            if (response != null) {
                // Thêm Log theo checklist debug của bạn
                Log.d(TAG, "response=" + response.getMessage());
                Log.d(TAG, "role=" + response.getRole());
                Log.d(TAG, "token=" + (response.getAccessToken() != null ? "FOUND" : "MISSING"));

                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                String userRole = response.getRole();

                // Chuyển sang MainActivity và truyền Role
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("USER_ROLE", userRole);

                startActivity(intent);
                finish(); // Đóng LoginActivity sau khi chuyển màn
            }
        });
    }

    private void showForgotPasswordDialog() {
        EditText edtEmail = new EditText(this);
        edtEmail.setHint("Email");
        edtEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        edtEmail.setSingleLine(true);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        edtEmail.setPadding(padding, 0, padding, 0);

        new AlertDialog.Builder(this)
                .setTitle("Quen mat khau")
                .setMessage("Nhap email de nhan ma OTP")
                .setView(edtEmail)
                .setNegativeButton("Huy", null)
                .setPositiveButton("Gui ma", (dialog, which) -> {
                    String email = edtEmail.getText().toString().trim();
                    if (email.isEmpty()) {
                        Toast.makeText(this, "Vui long nhap email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    resendForgotPasswordOtp(email);
                })
                .show();
    }

    private void resendForgotPasswordOtp(String email) {
        setForgotPasswordLoading(true);

        ApiClient.getAuthApiService()
                .resendOtp(new ResendOtpRequest(email))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        setForgotPasswordLoading(false);
                        if (!response.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Khong the gui ma OTP", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(LoginActivity.this, "Da gui ma OTP", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        setForgotPasswordLoading(false);
                        Toast.makeText(LoginActivity.this, "Loi ket noi", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setForgotPasswordLoading(boolean isLoading) {
        txtForgot.setEnabled(!isLoading);
        btnLogin.setEnabled(!isLoading);
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
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
