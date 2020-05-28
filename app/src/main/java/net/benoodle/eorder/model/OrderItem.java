package net.benoodle.eorder.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderItem {

    @SerializedName("quantity")
    private int quantity;
    @SerializedName("sku")
    private String sku;
    @SerializedName("variation_id")
    private String productID;

    //Selecciones apunta al productID (variation ID) en el server, elecciones del menú.
    @SerializedName("field_selecciones")
    private ArrayList<String> selecciones = new ArrayList<>();

    private String title;


    public OrderItem(String productID, String sku, int quantity, String title){
        this.productID = productID;
        this.sku = sku;
        this.quantity = quantity;
        this.title = title;
    }

    public OrderItem(String productID, String sku, int quantity, ArrayList<String> selecciones, String title){
        this.productID = productID;
        this.sku = sku;
        this.quantity = quantity;
        this.selecciones = selecciones;
        this.title = title;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public ArrayList<String> getSelecciones() {
        return selecciones;
    }

    public void setSelecciones(ArrayList<String> selecciones) {
        this.selecciones = selecciones;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }
}


