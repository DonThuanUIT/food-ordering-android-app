package com.foodorderingapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.foodorderingapp.R;
import com.foodorderingapp.data.remote.api.ApiClient;
import com.foodorderingapp.model.request.RegisterRequest;
import com.foodorderingapp.model.response.ApiError;
import com.foodorderingapp.model.response.RegisterResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtPhone, edtPassword, edtFullName, edtEmail, edtBuildingId;
    private Button btnRegister;

    private final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    private final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtBuildingId = findViewById(R.id.edtBuildingId);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String buildingId = edtBuildingId.getText().toString().trim();

        if (!validateInput(phone, password, fullName, email, buildingId)) {
            return;
        }

        RegisterRequest request = new RegisterRequest(
                phone,
                password,
                fullName,
                email,
                buildingId
        );

        ApiClient.getApiService().registerStudent(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();

                    Toast.makeText(RegisterActivity.this,
                            registerResponse.getMessage(),
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(RegisterActivity.this, VerifyOtpActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("phone", registerResponse.getPhone());
                    startActivity(intent);
                } else {
                    showApiError(response);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInput(String phone, String password, String fullName, String email, String buildingId) {
        if (phone.isEmpty()) {
            edtPhone.setError("Phone is required");
            edtPhone.requestFocus();
            return false;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            edtPhone.setError("Phone must be exactly 10 digits");
            edtPhone.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Password is required");
            edtPassword.requestFocus();
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            edtPassword.setError("Password must contain uppercase, lowercase, number, special character and at least 8 characters");
            edtPassword.requestFocus();
            return false;
        }

        if (fullName.isEmpty()) {
            edtFullName.setError("Full name is required");
            edtFullName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            edtEmail.setError("Email is required");
            edtEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Invalid email format");
            edtEmail.requestFocus();
            return false;
        }

        if (buildingId.isEmpty()) {
            edtBuildingId.setError("Building ID is required");
            edtBuildingId.requestFocus();
            return false;
        }

        return true;
    }

    private void showApiError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                ApiError apiError = new Gson().fromJson(errorJson, ApiError.class);

                String message = apiError != null && apiError.getMessage() != null
                        ? apiError.getMessage()
                        : "Request failed";

                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Unknown error", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(RegisterActivity.this, "Error parsing response", Toast.LENGTH_LONG).show();
        }
    }
}