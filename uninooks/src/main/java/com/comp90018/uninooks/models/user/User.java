package com.comp90018.uninooks.models.user;


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