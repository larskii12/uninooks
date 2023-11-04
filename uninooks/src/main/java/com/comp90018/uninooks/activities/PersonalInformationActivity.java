package com.comp90018.uninooks.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.comp90018.uninooks.R;
import com.comp90018.uninooks.models.user.User;
import com.comp90018.uninooks.service.mail.mailServiceImpl;
import com.comp90018.uninooks.service.user.UserServiceImpl;

public class PersonalInformationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinnerChangeFacultyList;

    private ArrayAdapter<CharSequence> facultyChangeAdapter;

    private Spinner spinnerChangeDegreeList;

    private ArrayAdapter<CharSequence> degreeChangeAdapter;

    private int userId;

    private String userName;

    private String userEmail;

    private String userFaculty;

    private String userDegree;

    private TextView userNameTextView;

    private TextView emailTextView;

    private TextView degreeTextView;

    private TextView facultyTextView;

    private EditText editUsernameEditText;

    private EditText editTextNewEmail;

    private Button buttonNewEmailGetOTP;

    private EditText editTextEmailOTP;

    private Button buttonNewEmailVerifyOTP;

    private final int OTP_TIMER = 20;

    private String otp;

    private String newUserEmail;



    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @SuppressLint({"SetTextI18n", "HandlerLeak"})
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case 0:
                    String info = (String) msg.obj;
                    Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
                    break;

                case 1:
                    userNameTextView.setText(userName);
                    emailTextView.setText(userEmail);
                    degreeTextView.setText(userDegree);
                    facultyTextView.setText(userFaculty);
                    break;

                case 2:
                    int time = (int) msg.obj;

                    if (time > 0) {
                        buttonNewEmailGetOTP.setEnabled(false);
                        buttonNewEmailGetOTP.setText(time + "s");
                        buttonNewEmailGetOTP.setTextColor(ContextCompat.getColor(PersonalInformationActivity.this, R.color.black));
                        buttonNewEmailGetOTP.setBackgroundColor(ContextCompat.getColor(PersonalInformationActivity.this, R.color.grey));

                        Message message = new Message();
                        message.what = 2;
                        message.obj = time - 1;
                        handler.sendMessageDelayed(message, 1000);
                    }

                    else {
                        buttonNewEmailGetOTP.setEnabled(true);
                        buttonNewEmailGetOTP.setText("Verify Email");
                        buttonNewEmailGetOTP.setTextColor(ContextCompat.getColor(PersonalInformationActivity.this, R.color.white));
                        buttonNewEmailGetOTP.setBackgroundColor(ContextCompat.getColor(PersonalInformationActivity.this, R.color.primary));
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_perfonal_info);

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", 6);

        ImageView backArrow = findViewById(R.id.Account_Back_to_Home_Arrow_Left);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        userNameTextView = findViewById(R.id.Account_Pi_Edit_Name);
        editUsernameEditText = findViewById(R.id.EditTextNewUsername);
        editTextNewEmail = findViewById(R.id.EditTextConfirmNewEmail);
        buttonNewEmailGetOTP = findViewById(R.id.Pi_ButtonConfirmNewEmail);
        editTextEmailOTP = findViewById(R.id.Pi_EditTextEmailOTP);
        buttonNewEmailVerifyOTP = findViewById(R.id.Pi_ButtonOTPVerify);

        otp = "";


        ImageView editNameIcon = findViewById(R.id.Account_Pi_Ic_Edit_Name);
        editNameIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editUsernameEditText.getVisibility() == View.GONE) {
                    editUsernameEditText.setVisibility(View.VISIBLE);
                    userNameTextView.setVisibility(View.GONE);
                } else {
                    editUsernameEditText.setVisibility(View.GONE);
                    userNameTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        emailTextView = findViewById(R.id.Account_Pi_Edit_Email);
//        editEmailEditText = findViewById(R.id.EditTextNewEmail);
        ImageView editEmailIcon = findViewById(R.id.Account_Pi_Ic_Edit_Email);

        editEmailIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextNewEmail.getVisibility() == View.GONE) {
//                    editEmailEditText.setVisibility(View.VISIBLE);

                    editTextNewEmail.setVisibility(View.VISIBLE);
                    buttonNewEmailGetOTP.setVisibility(View.VISIBLE);
                    editTextEmailOTP.setVisibility(View.VISIBLE);
                    buttonNewEmailVerifyOTP.setVisibility(View.VISIBLE);
                    emailTextView.setVisibility(View.GONE);

                } else {
                    editTextNewEmail.setVisibility(View.GONE);
                    buttonNewEmailGetOTP.setVisibility(View.GONE);
                    editTextEmailOTP.setVisibility(View.GONE);
                    buttonNewEmailVerifyOTP.setVisibility(View.GONE);
                    emailTextView.setVisibility(View.VISIBLE);
                }
            }
        });



        // Change Password
        buttonNewEmailGetOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    public void run() {
                        try {

                            // If email is empty
                            if (editTextNewEmail.getText().toString().trim().isEmpty()) {
                                showTextMessage("New Email cannot be empty");
                            }

                            else if (editTextNewEmail.getText().toString().equals(userEmail)){
                                showTextMessage("New email cannot same as the current email.");
                            }

                            else if (new UserServiceImpl().hasUser(editTextNewEmail.getText().toString())){
                                showTextMessage("This email has been registered with us, please try another one.");
                            }

                            else if (!editTextNewEmail.getText().toString().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")){
                                showTextMessage("This is not a valid email address format.");
                            }

                            else {
                                Message msg = new Message();
                                msg.what = 2;
                                msg.obj = OTP_TIMER;
                                handler.sendMessage(msg);

                                // Lock the new email
                                newUserEmail = editTextNewEmail.getText().toString();

                                otp = getOTP();
                            }

                        } catch (Exception e) {
                            throw new RuntimeException("Some error happens, please contact the IT administrator");
                        }
                    }
                }.start();
            }
        });


        buttonNewEmailVerifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(){
                    public void run(){
                        if (!otp.isEmpty() && otp.equals(editTextEmailOTP.getText().toString())){
                            try {
                                new UserServiceImpl().updateUserEmail(userId, newUserEmail);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                            showTextMessage("Your email has been changed successfully.");

                            reloadActivity();
                        }
                        else{
                            showTextMessage("OTP is not correct.");
                        }
                    }
                }.start();
            }
        });

        // Change Password
        ImageView editPasswordIcon = findViewById(R.id.Account_Pi_Ic_Edit_Password);
        editPasswordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        spinnerChangeDegreeList = findViewById(R.id.SpinnerPiDegreeList);

        // Set the degree drop down list
        degreeChangeAdapter = ArrayAdapter.createFromResource(this, R.array.degree_list, android.R.layout.simple_spinner_item);
        degreeChangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerChangeDegreeList.setAdapter(degreeChangeAdapter);
        spinnerChangeDegreeList.setOnItemSelectedListener(this);

        degreeTextView = findViewById(R.id.Account_Pi_Edit_Degree);
        ImageView editDegreeIcon = findViewById(R.id.Account_Pi_Ic_Edit_Degree);

        editDegreeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerChangeDegreeList.getVisibility() == View.GONE) {
                    spinnerChangeDegreeList.setVisibility(View.VISIBLE);
                    degreeTextView.setVisibility(View.GONE);
                } else {
                    spinnerChangeDegreeList.setVisibility(View.GONE);
                    degreeTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        facultyTextView = findViewById(R.id.Account_Pi_Edit_Faculty);
        ImageView editFacultyIcon = findViewById(R.id.Account_Pi_Ic_Edit_Faculty);

        spinnerChangeFacultyList = findViewById(R.id.SpinnerPiFacultyList);

        // Set the faculty drop down list
        facultyChangeAdapter = ArrayAdapter.createFromResource(this, R.array.faculty_list, android.R.layout.simple_spinner_item);
        facultyChangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerChangeFacultyList.setAdapter(facultyChangeAdapter);
        spinnerChangeFacultyList.setOnItemSelectedListener(this);

        editFacultyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerChangeFacultyList.getVisibility() == View.GONE) {
                    spinnerChangeFacultyList.setVisibility(View.VISIBLE);
                    facultyTextView.setVisibility(View.GONE);
                } else {
                    spinnerChangeFacultyList.setVisibility(View.GONE);
                    facultyTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        new Thread() {
            public void run(){
                initUserInfo();
            }
        }.start();
    }

    /**
     * Init the user info and show on the UI
     */
    private void initUserInfo() {

            try {

                User user = new UserServiceImpl().getUser(userId);

                userName = user.getUserName();
                userEmail = user.getUserEmail();
                userFaculty = user.getUserFaculty();

                switch (user.getUserAQFLevel()) {
                    case 1:
                        userDegree = "Certificate I";
                        break;
                    case 2:
                        userDegree = "Certificate II";
                        break;
                    case 3:
                        userDegree = "Certificate III";
                        break;
                    case 4:
                        userDegree = "Certificate IV";
                        break;
                    case 5:
                        userDegree = "Diploma";
                        break;
                    case 6:
                        userDegree = "Advanced Diploma, Associate Degree";
                        break;
                    case 7:
                        userDegree = "Bachelor Degree";
                        break;
                    case 8:
                        userDegree = "Bachelor Honours Degree";
                        break;
                    case 9:
                        userDegree = "Masters Degree";
                        break;
                    case 10:
                        userDegree = "Doctoral Degree";
                        break;
                    default:
                        userDegree = "Not Provided";
                }

                handler.sendEmptyMessage(1);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    /**
     * get OTP for registration
     *
     * @return OTP
     * @throws Exception if happens
     */
    private String getOTP() throws Exception {

        showTextMessage("The OTP has been sent, please check your mail box.");

        Message counter = new Message();
        counter.what = 2;
        counter.obj = OTP_TIMER;
        handler.sendMessage(counter);

        String newOTP = String.valueOf(new mailServiceImpl().sendOTP(editTextNewEmail.getText().toString()));

        return newOTP;
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

    /**
     * Reload current activity
     */
    private void reloadActivity(){
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}