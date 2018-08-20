package br.thayllo.labdefisica.model;

import java.util.ArrayList;

public class User {

    private String id;
    //private String ra;
    private String name;
    private String email;
    private String photoUrl;
    //private ArrayList<String> friends;

    public User() {}

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String nome) {
        this.name = nome;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhotoUrl() {
        return photoUrl;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public String toString() {
        return "name=" + name +
                "\nid=" + id +
                "\nemail=" + email +
                "\nphotoUrl=" + photoUrl;
    }
}
