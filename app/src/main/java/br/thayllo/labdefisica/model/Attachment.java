package br.thayllo.labdefisica.model;

public class Attachment {

    private String id;
    private String text;
    private String name;
    private String photoUrl;
    private String attachedAt;

    public Attachment() {    }

    public Attachment(String id, String text, String name, String photoUrl, String attachedAt) {
        this.id = id;
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.attachedAt = attachedAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAttachedAt() {
        return attachedAt;
    }

    public void setAttachedAt(String attachedAt) {
        this.attachedAt = attachedAt;
    }

    @Override
    public String toString() {
        return "id='" + id +
                "\ntext='" + text +
                "\nname='" + name +
                "\nphotoUrl='" + photoUrl ;
    }
}
