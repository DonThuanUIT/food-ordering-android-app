package com.foodorderingapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.foodorderingapp.R;

public class RegisterActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private EditText edtFullName, edtPhone, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        btnBack = findViewById(R.id.btnBack);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        observeViewModel();

        // Xử lý nút Đăng ký
        btnRegister.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString();
            String phone = edtPhone.getText().toString();
            String password = edtPassword.getText().toString();
            String confirmPassword = edtConfirmPassword.getText().toString();

            viewModel.register(fullName, phone, password, confirmPassword);
        });

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void observeViewModel() {
        viewModel.getFullNameError().observe(this, error -> edtFullName.setError(error));
        viewModel.getPasswordError().observe(this, error -> edtPassword.setError(error));
        viewModel.getConfirmPasswordError().observe(this, error -> edtConfirmPassword.setError(error));
        viewModel.getPhoneError().observe(this, error -> edtPhone.setError(error));

        viewModel.getIsLoading().observe(this, loading -> {
            if (loading != null) {
                btnRegister.setEnabled(!loading);
            }
        });

        viewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsSuccess().observe(this, success -> {
            if (success != null && success) {
                clearForm();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void clearForm() {
        edtFullName.setText("");
        edtPhone.setText("");
        edtPassword.setText("");
        edtConfirmPassword.setText("");
    }
}