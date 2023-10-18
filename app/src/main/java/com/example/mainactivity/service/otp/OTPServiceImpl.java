package com.example.mainactivity.service.otp;

import com.example.mainactivity.R;
import com.example.mainactivity.activities.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * Class for sending OTP email
 */
public class OTPServiceImpl extends javax.mail.Authenticator implements OTPService {

    /**
     * Method to send email
     * @param userEmail as the user need to receive the email
     * @return OTP for success or 1 for fails
     */
    public int sendRegistrationOTP(String userEmail) {
        // Create a Random object
        Random random = new Random();

        // Generate a random number between 100,000 and 999,999
        int min = 100000;
        int max = 999999;
        int OTP = random.nextInt(max - min + 1) + min;

        // Set Gmail information
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        // Create a session
        Session session = Session.getInstance(prop, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {


                try {
                    InputStream inputStream = MainActivity.getAppContext().getResources().openRawResource(R.raw.config);
                    Properties properties = new Properties();
                    properties.load(inputStream);

                    return new PasswordAuthentication(properties.getProperty("SENDER_EMAIL"), properties.getProperty("SENDER_PASSWORD"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

        // Send email
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("uninooks.support@gmail.com", "UNINOOKS Customer Support"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
            message.setSubject("Your UNINOOKS OTP");
            message.setText("Your UNINOOKS OTP is " + OTP + ". Please do not tell others.");

            Transport.send(message);

            System.out.println("Mail send success");

            return OTP;
        }

        // If exception, return 1.
        catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return 1;
    }
}