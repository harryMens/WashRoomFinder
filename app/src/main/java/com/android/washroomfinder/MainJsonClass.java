package com.android.washroomfinder;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity
public class MainJsonClass {

    @PrimaryKey(autoGenerate = true)
    int id;
    @SerializedName("Column2")
    String description;
    @SerializedName("Column3")
    String city;
    @SerializedName("Column4")
    String street;
    @SerializedName("Column5")
    String number;
    @SerializedName("Column6")
    String postalCode;
    @SerializedName("Column11")
    String handicappedAccessible;
    @SerializedName("Column12")
    String price;
    @SerializedName("Column13")
    String canBePayedWithCoins;
    @SerializedName("Column14")
    String canBePayedInApp;
    @SerializedName("Column15")
    String canBePayedWithNFC;
    @SerializedName("Column16")
    String hasChangingTable;
    @SerializedName("Column9")
    String latitude;
    @SerializedName("Column8")
    String longitude;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getHandicappedAccessible() {
        return handicappedAccessible;
    }

    public void setHandicappedAccessible(String handicappedAccessible) {
        this.handicappedAccessible = handicappedAccessible;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCanBePayedWithCoins() {
        return canBePayedWithCoins;
    }

    public void setCanBePayedWithCoins(String canBePayedWithCoins) {
        this.canBePayedWithCoins = canBePayedWithCoins;
    }

    public String getCanBePayedInApp() {
        return canBePayedInApp;
    }

    public void setCanBePayedInApp(String canBePayedInApp) {
        this.canBePayedInApp = canBePayedInApp;
    }

    public String getCanBePayedWithNFC() {
        return canBePayedWithNFC;
    }

    public void setCanBePayedWithNFC(String canBePayedWithNFC) {
        this.canBePayedWithNFC = canBePayedWithNFC;
    }

    public String getHasChangingTable() {
        return hasChangingTable;
    }

    public void setHasChangingTable(String hasChangingTable) {
        this.hasChangingTable = hasChangingTable;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
