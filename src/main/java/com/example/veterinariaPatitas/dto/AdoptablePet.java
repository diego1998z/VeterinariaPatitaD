package com.example.veterinariaPatitas.dto;

public class AdoptablePet {

    private final String id;
    private final String name;
    private final String imageUrl;

    public AdoptablePet(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
}
