package br.thayllo.labdefisica.model;

public class User {

    private String id;
    //private String ra;
    private String name;
    private String email;
    //private String senha;

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

    @Override
    public String toString() {
        return "id=" + id +
                "\nname=" + name +
                "\nemail=" + email;
    }
}
