package com.foodorderingapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.foodorderingapp.R;

import java.util.Locale;

public class OtpActivity extends AppCompatActivity {
    private OtpViewModel viewModel;
    private String email;
    private TextView tvEmailHeader, tvTimer;
    private Button btnVerify, btnResend;
    private EditText edtOtp;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        initViews();

        email = getIntent().getStringExtra("email");
        if (email != null) {
            tvEmailHeader.setText("Mã đã được gửi tới: " + email);
        }

        viewModel = new ViewModelProvider(this).get(OtpViewModel.class);

        btnVerify.setOnClickListener(v -> {
            String otp = edtOtp.getText().toString();

            if (otp.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            btnVerify.setEnabled(false);
            viewModel.verifyOtp(email, otp);
        });

        btnResend.setOnClickListener(v -> {
            viewModel.resendOtp(email);
            startCountDown(); // Bắt đầu đếm ngược lại khi gửi mã mới
        });

        observeViewModel();
        startCountDown(); // Chạy đồng hồ ngay khi vào màn hình
    }

    private void initViews() {
        tvEmailHeader = findViewById(R.id.tvEmailHeader);
        tvTimer = findViewById(R.id.tvTimer);
        edtOtp = findViewById(R.id.edtOtp);
        btnVerify = findViewById(R.id.btnVerify);
        btnResend = findViewById(R.id.btnResend);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void startCountDown() {
        if (countDownTimer != null) countDownTimer.cancel();

        btnResend.setEnabled(false);
        viewModel.setOtpExpired(false);

        countDownTimer = new CountDownTimer(5 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = millisUntilFinished / 1000 % 60;

                tvTimer.setText(String.format(
                        Locale.getDefault(),
                        "Gửi lại sau %02d:%02d",
                        minutes,
                        seconds
                ));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Mã đã hết hạn");
                btnResend.setEnabled(true);
                viewModel.setOtpExpired(true); // Chặn verify trong ViewModel
            }
        }.start();
    }

    private void observeViewModel() {
        viewModel.getIsVerified().observe(this, verified -> {
            if (verified != null && verified) {
                Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            btnVerify.setEnabled(true);
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}