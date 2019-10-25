package com.mtsealove.github.iot.Database;

//로그인 요구정보 클래스
public class LoginData {
    String ID, Password;

    public LoginData(String ID, String password) {
        this.ID = ID;
        Password = password;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
