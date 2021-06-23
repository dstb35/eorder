package net.benoodle.eorder.model;

import com.google.gson.annotations.SerializedName;

public class Cuppon {
    @SerializedName("type")
    private String type;
    @SerializedName("percentage")
    private Integer percentage;
    @SerializedName("product")
    private Integer product;
    @SerializedName("cuppon")
    private String cuppon;
    @SerializedName("op")
    private String op;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getCuppon() {
        return cuppon;
    }

    public void setCuppon(String cuppon) {
        this.cuppon = cuppon;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public void setProduct(Integer product) {
        this.product = product;
    }

    public String getType() {
        return type;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public Integer getProduct() {
        return product;
    }
}

