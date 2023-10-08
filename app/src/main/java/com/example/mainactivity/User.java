package com.example.mainactivity;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class User {

    /**
     * User ID if acceptable
     */
    int userId;

    /**
     * User name
     */
    String userName;

    /**
     * User email
     */
    String userEmail;

    /**
     * User password, if acceptable
     */
    String userPassword;

    /**
     * User Faculty, if acceptable
     */
    String userFaculty;

    /**
     * user AQF level, if acceptable
     */
    int userAQFLevel;

    Connection connector = new DatabaseHelper().getConnector();

    /**
     * User log in with user name
     *
     * @param userNameOrEmail, as user name or user email address
     * @param userPassword,    as user password
     * @return true if login success
     * @throws Exception, if user log in fails
     */
    public boolean logIn(String userNameOrEmail, String userPassword) throws Exception {

        try {

            String query = "SELECT user_password FROM mobilecomputing.\"user\" WHERE \"user_name\" = ? OR \"user_email\" = ?";

            PreparedStatement preparedStatement = connector.prepareStatement(query);
            preparedStatement.setString(1, userNameOrEmail);
            preparedStatement.setString(2, userNameOrEmail);

            ResultSet resultSet = preparedStatement.executeQuery();

            // If user name, email, and password matches, return true
            if (resultSet.next() && BCrypt.checkpw(userPassword, resultSet.getString("user_password"))) {
                return true;
            }

            // If user not exist, password not correct or other exceptions
            else {
                throw new Exception();
            }
        }

        // If log in fails
        catch (Exception e) {
            throw new Exception("The information does not match our records, please try again.");
        }
    }


    /**
     * method to add a new user into the database (To register a new user)
     *
     * @param userName     as the user name
     * @param userEmail    as the user email
     * @param userPassword as the user password
     * @param userFaculty  as the user faculty
     * @param userAQFLevel as the user AQF level
     * @throws Exception if any exceptions happens
     */
    public boolean addUser(String userName, String userEmail, String userPassword, String userFaculty, int userAQFLevel) throws Exception {

        // Check all fields not empty, faculty can be 0, treat as a guest.
        if (userName.replaceAll("\\s", "").equals("") || userEmail.replaceAll("\\s", "").equals("") || userPassword.replaceAll("\\s", "").equals("")) {
            throw new Exception("All fields are required.");
        }

        // Check userAQF level is valid, 0 is guest.
        if (userAQFLevel < 0 || userAQFLevel > 10) {
            throw new Exception("AQF level invalid, AQF level should between 1 and 10.");
        }

        try {

            // Add new user to the database
            String query = "INSERT INTO mobilecomputing.\"user\" (\"user_name\", \"user_email\", \"user_password\", \"user_faculty\", \"user_AQF_level\") VALUES (?, ?, ?, ?, ?)";

            PreparedStatement preparedStatement = connector.prepareStatement(query);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, userEmail);
            preparedStatement.setString(3, BCrypt.hashpw(userPassword, BCrypt.gensalt()));
            preparedStatement.setString(4, userFaculty);
            preparedStatement.setInt(5, userAQFLevel);

            // Execute query
            preparedStatement.executeUpdate();

            return true;

            //connection.createStatement().executeQuery(query);
        }

        // If exception happens
        catch (Exception e) {

            // If user name has already been registered
            if (e.getMessage().contains("unique_user_username")) {
                throw new Exception("This username has been registered, please try another one.");
            }

            // If email has already been registered
            else if (e.getMessage().contains("unique_user_email")) {
                throw new Exception("This email has been registered, please try another one.");
            }

            // Unknown exceptions happens.
            throw new Exception("User added failed, please contact the IT administrator to report the issue.");
        }
    }


    /**
     * Delete an user with given user email
     *
     * @param userId, the user id who need to be deleted
     * @throws Exception, if delete fails, show the exception
     */
    public boolean deleteUser(int userId) throws Exception {
        try {
            String query = "DELETE FROM mobilecomputing.\"user\" WHERE \"user_id\" = ?";

            PreparedStatement preparedStatement = connector.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();

            return true;
        }

        // If exception happens when deleting an user.
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("User delete failed, please contact the IT administrator.");
        }
    }


    /**
     * Get a user with specified email address
     *
     * @param userId, as the user id
     * @return User
     */
    public User getUser(int userId) throws Exception {

        User user = new User();

        try {

            String query = "SELECT * FROM mobilecomputing.\"user\" WHERE \"user_id\" = ?";

            PreparedStatement preparedStatement = connector.prepareStatement(query);
            preparedStatement.setInt(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();

            // Set user information
            if (resultSet.next()) { // Ensure there's a row in the result set
                // Set user information
                user.setUserId(resultSet.getInt("user_id"));
                user.setUserName(resultSet.getString("user_name"));
                user.setUserEmail(resultSet.getString("user_email"));
                user.setUserFaculty(resultSet.getString("user_faculty"));
                user.setUserAQFLevel(resultSet.getInt("user_AQF_level"));

                return user;
            }

        }

        // If exception happens when querying user
        catch (Exception e) {
            throw new Exception("Some error happened, please contact the IT administrator.");
        }
        // Return user information
        return null;
    }

    /**
     * Update user name
     *
     * @param userId,      as user id
     * @param newUserName, as new user name
     * @throws Exception, if duplication or other exception happens
     */
    public boolean updateUserName(int userId, String newUserName) throws Exception {
        try {

            String query = "UPDATE mobilecomputing.\"user\" SET \"user_name\" = ? WHERE \"user_id\" = ?";
            PreparedStatement ps = connector.prepareStatement(query);
            ps.setString(1, newUserName);
            ps.setInt(2, userId);
            ps.executeUpdate();

            return true;
        }

        //
        catch (Exception e) {

            // If user name has been used.
            if (e.getMessage().contains("unique_user_username")) {
                throw new Exception("This user name has been used, please try another one.");
            }

            // unknown exception happens when update user name
            else {
                throw new Exception("Errors happened when update user information, please contact the IT administrator.");
            }
        }
    }


    /**
     * Update user email address
     *
     * @param userId,       as user id
     * @param newUserEmail, as new user email address
     * @throws Exception, if duplication or other exception happens
     */
    public boolean updateUserEmail(int userId, String newUserEmail) throws Exception {
        try {

            String query = "UPDATE mobilecomputing.\"user\" SET \"user_email\" = ? WHERE \"user_id\" = ?";
            PreparedStatement ps = connector.prepareStatement(query);
            ps.setString(1, newUserEmail);
            ps.setInt(2, userId);
            ps.executeUpdate();

            return true;
        }

        // unknown exception happens when update user email
        catch (Exception e) {

            // If user name has been used.
            if (e.getMessage().contains("unique_user_email")) {
                throw new Exception("This user email has been used, please try another one.");
            }

            // unknown exception happens
            else {
                throw new Exception("Errors happened when update user information, please contact the IT administrator.");
            }
        }
    }


    /**
     * Update user password
     *
     * @param userId,          as user id
     * @param newUserPassword, as new user password
     * @throws Exception, if exception happens
     */
    public boolean updateUserPassword(int userId, String oldUserPassword, String newUserPassword) throws Exception {
        try {
            // Get the user
            String query = "SELECT user_password FROM mobilecomputing.\"user\" WHERE \"user_id\" = ?";

            PreparedStatement preparedStatement = connector.prepareStatement(query);
            preparedStatement.setInt(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();

            // // Verify user old password and change password
            if (resultSet.next() && BCrypt.checkpw(oldUserPassword, resultSet.getString("user_password"))) {

                // Change password
                String queryChangePassword = "UPDATE mobilecomputing.\"user\" SET \"user_password\" = ? WHERE \"user_id\" = ?";
                PreparedStatement ps = connector.prepareStatement(queryChangePassword);
                ps.setString(1, BCrypt.hashpw(newUserPassword, BCrypt.gensalt()));
                ps.setInt(2, userId);
                ps.executeUpdate();

                return true;
            }

            // If old password is not correct
            else {
                throw new Exception("Your old password is not correct, please try again.");
            }

        }

        // If exception happens
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }


    public boolean resetUserPassword(String userNameOrEmail, String newUserPassword) throws Exception {
        // Change password
        try {

            String queryChangePassword = "UPDATE mobilecomputing.\"user\" SET \"user_password\" = ? WHERE \"user_name\" = ? OR \"user_email\" = ?";
            PreparedStatement ps = connector.prepareStatement(queryChangePassword);

            ps.setString(1, BCrypt.hashpw(newUserPassword, BCrypt.gensalt()));
            ps.setString(2, userNameOrEmail);
            ps.setString(3, userNameOrEmail);
            ps.executeUpdate();

            return true;
        }

        // If exception happens
        catch (Exception e) {
            throw new Exception("Error happened when changing password, please contact the IT administrator.");
        }
    }


    /**
     * Update user faculty
     *
     * @param userId,         as user id
     * @param newUserFaculty, as new user new faculty
     * @throws Exception, if exception happens
     */
    public boolean updateUserFaculty(int userId, String newUserFaculty) throws Exception {
        try {

            String query = "UPDATE mobilecomputing.\"user\" SET \"user_faculty\" = ? WHERE \"user_id\" = ?";
            PreparedStatement ps = connector.prepareStatement(query);
            ps.setString(1, newUserFaculty);
            ps.setInt(2, userId);
            ps.executeUpdate();

            return true;

        }

        // If exception happens
        catch (Exception e) {
            throw new Exception("Errors happened when update user information, please contact the IT administrator.");
        }

    }


    /**
     * Update user's AQF level
     *
     * @param userId, as user id
     * @throws Exception if exception happens
     */
    public boolean updateUserAQFLevel(int userId, int newAQFLevel) throws Exception {
        try {

            //  Update user AQF level
            String query = "UPDATE mobilecomputing.\"user\" SET \"user_AQF_level\" = ? WHERE \"user_id\" = ?";
            PreparedStatement ps = connector.prepareStatement(query);
            ps.setInt(1, newAQFLevel);
            ps.setInt(2, userId);
            ps.executeUpdate();

            return true;
        }

        // If exception happens
        catch (Exception e) {
            throw new Exception("Errors happened when update user information, please contact the IT administrator.");
        }
    }


    /**
     * Setters and Getters
     */


    /**
     * Setter, set user ID
     *
     * @param userId, as user id
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Setter, set user name
     *
     * @param userName, as user user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Setter, set user email
     *
     * @param userEmail as user email
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Setter, set user faculty
     *
     * @param userFaculty, as user faculty
     */
    public void setUserFaculty(String userFaculty) {
        this.userFaculty = userFaculty;
    }

    /**
     * Setter, set user AQF level
     *
     * @param userAQFLevel, as user AQF level
     */
    public void setUserAQFLevel(int userAQFLevel) {
        this.userAQFLevel = userAQFLevel;
    }

    /**
     * Getter, get user ID
     *
     * @return userID, as user id
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Getter, get user name
     *
     * @return userName, as user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Getter, get user email
     *
     * @return userEmail, as user email
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Getter, get user faculty
     *
     * @return userFaculty, as user faculty
     */
    public String getUserFaculty() {
        return userFaculty;
    }

    /**
     * Getter, get user AQF level
     *
     * @return userAQFLevel, as user AQF level
     */
    public int getUserAQFLevel() {
        return userAQFLevel;
    }
}