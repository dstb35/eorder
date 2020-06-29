package net.benoodle.eorder.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderItem {

    @SerializedName("quantity")
    private int quantity;
    @SerializedName("variation_id")
    private String productID;
    //Selecciones apunta al productID (variation ID) en el server, elecciones del menú.
    @SerializedName("field_selecciones")
    private ArrayList<String> selecciones = new ArrayList<>();



    public OrderItem(String productID, int quantity){
        this.productID = productID;
        this.quantity = quantity;
    }

    public OrderItem(String productID, int quantity, ArrayList<String> selecciones){
        this.productID = productID;
        this.quantity = quantity;
        this.selecciones = selecciones;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public ArrayList<String> getSelecciones() {
        return selecciones;
    }

    public void setSelecciones(ArrayList<String> selecciones) {
        this.selecciones = selecciones;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }
}


