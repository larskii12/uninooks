package com.example.mainactivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button);

        // Test button
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            DatabaseHelper db = new DatabaseHelper();
                            if (db.databaseConnectionTest()) {
                                System.out.println("Database online!");
                                try {
//                                    // Add an user
//                                    new User().addUser("djhasoidajns", "daweqdq", "userPaed13d1ss", "Chemistry", 5);
//                                    System.out.println("Add successfully.");
//
//
//
//
//                                    // Delete an user
//                                    new User().deleteUser(6);
//                                    System.out.println("delete successful.");
//
//
//
//
//                                    // Get an user information
//                                    User user = new User().getUser(15);
//                                    if (user == null) {
//                                        throw new Exception("Some error happened, please contact the IT administrator.");
//                                    }
//
//                                    else{
//                                        System.out.println("Query successful.");
//                                        System.out.println(user.getUserId());
//                                        System.out.println(user.getUserName());
//                                        System.out.println(user.getUserEmail());
//                                        System.out.println(user.getUserFaculty());
//                                        System.out.println(user.getUserAQFLevel());
//                                    }
//
//
//
//                                    // Update an user's user name information
//                                    new User().updateUserName(15, "Frank Martinez");
//                                    System.out.println("The user name has been updated successfully.");
//
//                                    // Update an user's user email information
//                                    new User().updateUserEmail(15,"useaaaaartest@tt.com");
//                                    System.out.println("The user email has been updated successfully.");
//
                                    // Update an user's user password information
//                                    new User().updateUserPassword(15, "xxxxxxxxxx");
//                                    System.out.println("The user password has been updated successfully.");
//
                                    // Update an user's user faculty information
//                                    new User().updateUserFaculty(15, "IT");
//                                    System.out.println("The user faculty has been updated successfully.");
//
                                    // Update an user's user AQF level information
//                                    new User().updateUserAQFLevel(15, 8);
//                                    System.out.println("The user AQF level has been updated successfully.");


                                }

                                // If exception when operating
                                catch (Exception e) {
                                    System.out.println(e.getMessage());
                                }
                            }

                            // If database is not reachable
                            else {
                                System.out.println("Database offline!");
                            }
                        }

                        // If exception when operating
                        catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                }.start();
            }
        });
    }
}