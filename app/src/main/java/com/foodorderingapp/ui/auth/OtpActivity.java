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

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.format(Locale.getDefault(), "Gửi lại sau %ds", seconds));
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
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}