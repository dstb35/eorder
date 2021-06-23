package net.benoodle.eorder.model;

import com.google.gson.annotations.SerializedName;

public class Store {
    @SerializedName("name")
    private String name;
    @SerializedName("store_id")
    private String store_id;
    @SerializedName("modus")
    private String modus;

    public Store(String name){
        this.name = name.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public String getStore_id() { return store_id;}

    public String getModus(){
        return modus;
    }
}

