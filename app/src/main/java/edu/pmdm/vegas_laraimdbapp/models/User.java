package edu.pmdm.vegas_laraimdbapp.models;

public class User {
    private String id;
    private String name;
    private String email;
    private String lastLogin;
    private String lastLogout;

    public User(String id, String name, String email, String lastLogin, String lastLogout) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.lastLogin = lastLogin;
        this.lastLogout = lastLogout;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getLastLogin() {
        return lastLogin;
    }
    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }
    public String getLastLogout() {
        return lastLogout;
    }
    public void setLastLogout(String lastLogout) {
        this.lastLogout = lastLogout;
    }
}
