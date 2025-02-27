package com.comp90018.uninooks.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.comp90018.uninooks.R;
import com.comp90018.uninooks.service.mail.MailServiceImpl;
import com.comp90018.uninooks.service.user.UserServiceImpl;

public class ResetPasswordActivity extends AppCompatActivity {

    private final int OTP_TIMER = 60;
    private String otp;
    private Button buttonGetResetPasswordOTP;
    private Button buttonResetPasswordOTPVerify;
    private EditText editTextResetPasswordEmail;
    private EditText editTextResetPasswordOTP;
    private EditText editTextResetPasswordNewPassword;
    private EditText editTextResetPasswordNewPasswordConfirm;
    private Button buttonResetPasswordConfirm;

    private String userEmail;

    /**
     * on create method
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Intent intent = getIntent();
        userEmail = intent.getStringExtra("USER_EMAIL_EXTRA");
        handler.sendEmptyMessage(3);

        editTextResetPasswordEmail = findViewById(R.id.EditTextResetPasswordEmail);
        editTextResetPasswordOTP = findViewById(R.id.EditTextResetPasswordOTP);
        buttonGetResetPasswordOTP = findViewById(R.id.ButtonGetResetPasswordOTP);
        buttonResetPasswordOTPVerify = findViewById(R.id.ButtonResetPasswordOTPVerify);

        editTextResetPasswordNewPassword = findViewById(R.id.EditTextResetPasswordNewPassword);
        editTextResetPasswordNewPasswordConfirm = findViewById(R.id.EditTextResetPasswordNewPasswordConfirm);
        buttonResetPasswordConfirm = findViewById(R.id.ButtonResetPasswordConfirm);

        editTextResetPasswordNewPassword.setVisibility(View.GONE);
        editTextResetPasswordNewPasswordConfirm.setVisibility(View.GONE);
        buttonResetPasswordConfirm.setVisibility(View.GONE);

        buttonGetResetPasswordOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    public void run() {
                        try {

                            // If email is empty
                            if (editTextResetPasswordEmail.getText().toString().trim().isEmpty()) {
                                showTextMessage("Email cannot be empty");
                            } else {
                                Message msg = new Message();
                                msg.what = 1;
                                msg.obj = OTP_TIMER;
                                handler.sendMessage(msg);

                                showTextMessage("If your user name or email matches our record, an email has sent to you email address.");

                                otp = getOTP();
                            }

                        } catch (Exception e) {
                            throw new RuntimeException("Some error happens, please contact the IT administrator");
                        }
                    }
                }.start();
            }
        });

        buttonResetPasswordOTPVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread() {
                    public void run() {
                        try {
                            if (otp.equals("") || !otp.equals(editTextResetPasswordOTP.getText().toString())) {
                                showTextMessage("Your OTP code is incorrect.");
                            } else {
                                handler.sendEmptyMessage(2);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }
                }.start();
            }
        });

        buttonResetPasswordConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread() {
                    public void run() {
                        try {
                            if (editTextResetPasswordNewPassword.getText().toString().equals("") || editTextResetPasswordNewPasswordConfirm.getText().toString().equals("")) {
                                showTextMessage("New password cannot be empty.");
                            } else if (!editTextResetPasswordNewPassword.getText().toString().equals(editTextResetPasswordNewPasswordConfirm.getText().toString())) {
                                showTextMessage("Your new password are not identical.");
                            } else if (editTextResetPasswordNewPassword.getText().toString().length() < 8 || editTextResetPasswordNewPasswordConfirm.getText().toString().length() < 8) {
                                showTextMessage("Password length at least 8 characters.");
                            } else if (editTextResetPasswordNewPassword.getText().toString().equals(editTextResetPasswordNewPasswordConfirm.getText().toString())) {
                                resetPassWord();
                            } else {
                                throw new Exception("An error occurring when reset your password, please contact the IT administrator.");
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }.start();
            }
        });

    }

    public void onStart() {
        super.onStart();
        this.otp = "";
    }

    /**
     * Drive to Reset password page
     *
     * @throws Exception
     */
    private void resetPassWord() throws Exception {

        new UserServiceImpl().resetUserPassword(editTextResetPasswordEmail.getText().toString(), editTextResetPasswordNewPassword.getText().toString());
        showTextMessage("Your password has been reset successfully.");
        // Jump to login page
        new Thread() {
            public void run() {
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }.start();
    }

    /**
     * get OTP for registration
     *
     * @return OTP
     * @throws Exception if happens
     */
    private String getOTP() throws Exception {

//             Disable get OTP button
//        handler.sendEmptyMessage(3);

        // // Verify user old password and change password
        if (new UserServiceImpl().hasUser(editTextResetPasswordEmail.getText().toString())) {

            return String.valueOf(new MailServiceImpl().sendOTP(editTextResetPasswordEmail.getText().toString().trim()));
        }

        return "";
    }

    /**
     * Show message text
     *
     * @param text as the showing message
     */
    private void showTextMessage(String text) {
        Message msg = new Message();
        msg.what = 0;
        msg.obj = text;
        handler.sendMessage(msg);
    }

    @SuppressLint("HandlerLeak")

    private final Handler handler = new Handler() {

        @SuppressLint("SetTextI18n")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String info = (String) msg.obj;
                    Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
                    break;

                case 1:

                    int time = (int) msg.obj;

                    if (time > 0) {
                        buttonGetResetPasswordOTP.setEnabled(false);
                        buttonGetResetPasswordOTP.setText(time + "s");
                        buttonGetResetPasswordOTP.setTextColor(ContextCompat.getColor(ResetPasswordActivity.this, R.color.black));
                        buttonGetResetPasswordOTP.setBackgroundColor(ContextCompat.getColor(ResetPasswordActivity.this, R.color.grey));

                        Message message = new Message();
                        message.what = 1;
                        message.obj = time - 1;
                        handler.sendMessageDelayed(message, 1000);
                    } else {
                        editTextResetPasswordOTP.setEnabled(true);
                        buttonGetResetPasswordOTP.setEnabled(true);
                        buttonGetResetPasswordOTP.setText("Get OTP");
                        buttonGetResetPasswordOTP.setTextColor(ContextCompat.getColor(ResetPasswordActivity.this, R.color.white));
                        buttonGetResetPasswordOTP.setBackgroundColor(ContextCompat.getColor(ResetPasswordActivity.this, R.color.primary));

                    }
                    break;

                case 2:
                    editTextResetPasswordEmail.setVisibility(View.GONE);
                    editTextResetPasswordOTP.setVisibility(View.GONE);
                    buttonGetResetPasswordOTP.setVisibility(View.GONE);
                    buttonResetPasswordOTPVerify.setVisibility(View.GONE);

                    editTextResetPasswordNewPassword.setVisibility(View.VISIBLE);
                    editTextResetPasswordNewPasswordConfirm.setVisibility(View.VISIBLE);
                    buttonResetPasswordConfirm.setVisibility(View.VISIBLE);

                    break;

                case 3:
                    editTextResetPasswordEmail.setText(userEmail);
                    break;
            }
        }
    };
}