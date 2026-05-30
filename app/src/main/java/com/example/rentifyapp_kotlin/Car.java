package com.example.rentifyapp_kotlin;

public class Car {
    private String id;
    private String make;
    private String model;
    private String year;
    private double price;
    private String purpose; // "rent" or "sale"
    private String description;
    private String imageUrl;
    private String location;
    private String ownerEmail;

    public Car() {}

    public Car(String id, String make, String model, String year,
               double price, String purpose, String description,
               String imageUrl, String location, String ownerEmail) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.year = year;
        this.price = price;
        this.purpose = purpose;
        this.description = description;
        this.imageUrl = imageUrl;
        this.location = location;
        this.ownerEmail = ownerEmail;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getTitle() {
        return (year != null && !year.isEmpty() ? year + " " : "") + make + " " + model;
    }

    public String getPriceLabel() {
        if ("rent".equals(purpose)) {
            return String.format("$%.0f / day", price);
        } else {
            return String.format("$%.0f", price);
        }
    }
}