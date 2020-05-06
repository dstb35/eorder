package net.benoodle.eorder.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static net.benoodle.eorder.MainActivity.catalog;
import static net.benoodle.eorder.MainActivity.order;

public class Order {
    @SerializedName("type")
    private String type;
    @SerializedName("email")
    private String email;
    @SerializedName("store")
    private String store;
    @SerializedName("placed")
    private Boolean placed = TRUE;
    @SerializedName ("order_items")
    private ArrayList<OrderItem> orderItems = new ArrayList<>() ;
    @SerializedName("order_id")
    private String orderId;

    public Order() {
        this.type ="default";
        this.store = "1";
    }

    /*
    Añade un producto al pedido, primero comprueba que el producto no exista
    para actualizar la cantidad.
     */
    public void addOrderItem(String sku, int quantity, String title){
        for (int i=0; i<orderItems.size(); i++){
            if (orderItems.get(i).getSku().compareTo(sku) == 0){
                int oldQuantity = orderItems.get(i).getQuantity();
                orderItems.get(i).setQuantity(quantity+oldQuantity);
                if (orderItems.get(i).getQuantity() < 1){
                    orderItems.remove(i);
                }
                return;
            }
        }
        OrderItem orderItem = new OrderItem(sku, quantity, title);
        orderItems.add(orderItem);
    }

    public void addMenuItem(ArrayList<String> selecciones, String sku, int quantity, String title){
        /*
        Aunque el sku exista en algún OrderItem de la Order actual debemos crear uno nuevo porque
        al tener diferentes selecciones de menú conforma una línea de pedido nueva, con el mismo sku.
         */
        OrderItem orderItem = new OrderItem(sku, quantity, selecciones, title);
        orderItems.add(orderItem);
    }

    public OrderItem getOrderItem(int i) { return orderItems.get(i); }

    public void removeOrderItem (int i) {
        orderItems.remove(i);
    }

    public void removeOrderItem (OrderItem orderItem) {
        orderItems.remove(orderItem);
    }

    public void removeAllOrderItems (ArrayList<OrderItem> removeOrderItems){
        orderItems.removeAll(removeOrderItems);
    }

    public int getOrderItemsSize () { return orderItems.size(); }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public ArrayList<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(ArrayList<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /*
    Método que devuelve los productos asociados a un OrderItem pasado por parámetro.
    Útil para saber los productos asociados al pedido de un menú.
     */
    public String getSeleccionesByID (OrderItem orderItem) {
        String titulos = new String();
        for (String seleccion : orderItem.getSelecciones()){
            try {
                Node node = catalog.getNodeById(seleccion);
                titulos = titulos.concat(node.getTitle()+" ");
            } catch (Exception e){
                titulos.concat(e.getMessage());
            }
        }
        return titulos;
    }
    
    public Float getTotal() throws Exception{
        Float total = new Float(0.00 );
        for (OrderItem orderItem : orderItems){
            Node node = catalog.getNodeBySku(orderItem.getSku());
           // Pattern pattern = Pattern.compile("\\w");
            //String FormatPrice = node.getPrice().substring(0, 4);
            String formatPrice = node.getPrice();
            //quitar carácteres y comas, se quedan los puntos como separador de decimales
            formatPrice = formatPrice.replaceAll("[^0-9\\.]", ""); //quitar carácteres y comas
            Float subtotal = Float.parseFloat(formatPrice);
            total += subtotal * orderItem.getQuantity();
        }
        return total;
    }
}