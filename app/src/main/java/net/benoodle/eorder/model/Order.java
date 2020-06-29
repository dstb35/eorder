package net.benoodle.eorder.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Boolean.TRUE;
import static net.benoodle.eorder.MainActivity.catalog;

public class Order {
    @SerializedName("type")
    private String type;
    @SerializedName("email")
    private String email;
    @SerializedName("store_id")
    private String store_id;
    @SerializedName("placed")
    private Boolean placed = TRUE;
    @SerializedName ("order_items")
    private ArrayList<OrderItem> orderItems = new ArrayList<>() ;
    @SerializedName("order_id")
    private String orderId;

    public Order(String store_id) {
        this.type = "default";
        this.store_id = store_id;
    }

    /*
    Añade un producto al pedido, primero comprueba que el producto no exista
    para actualizar la cantidad. Lanza una excepción si no hay stock.
    Con stock -1 no hay control de stock.
     */
    public void addOrderItem(String productID, int quantity)
        throws Exception {
        int pos = catalog.getPosById(productID);
        Node node = catalog.getNode(pos);
        Integer stock = node.getStock();
        if (!catalog.isStock(productID, quantity)){
            throw new Exception();
        }
        for (int i=0; i<orderItems.size(); i++) {
            //Comprobar si está este producto en el carrito de una compra anterior
            if (orderItems.get(i).getProductID().compareTo(productID) == 0) {
                int newQuantity = quantity + orderItems.get(i).getQuantity();
                //Si la cantidad de orderItems llega a 0 eliminamos el orderItem
                if (newQuantity == 0){
                    orderItems.remove(i);
                }else{
                    orderItems.get(i).setQuantity(newQuantity);
                }
                //Actualizar el stock del product del catálogo
                if (stock != -1){
                    catalog.getNode(pos).updateStock(quantity);
                }
                return;
            }
        }
        //Si no se encontraba es una compra nueva, no puede ser una cantidad negativa
        if (quantity > 0){
            OrderItem orderItem = new OrderItem(productID, quantity);
            orderItems.add(orderItem);
            if (stock != -1){
                try{
                    catalog.getNode(pos).updateStock(quantity);
                }catch (Exception e){
                    e.getLocalizedMessage();
                }
            }
        }
    }

    public void addMenuItem(String productID, ArrayList<String> selecciones, int quantity)
        throws Exception{
        /*
        Aunque el sku exista en algún OrderItem de la Order actual debemos crear uno nuevo porque
        al tener diferentes selecciones de menú conforma una línea de pedido nueva, con el mismo sku.
         */
        if (!catalog.isStock(productID, quantity)){
            throw new Exception();
        }
        for(String id : selecciones){
            try{
                Integer i = catalog.getPosById(id);
                Node node = catalog.getNode(i);
                if (node.getStock() != -1){
                    //En teoría no permite hacer selecciones sin stock el menu activity
                    //por el método catalog.opcionesMenu
                    if (!catalog.isStock(id, 1)) {
                        throw new Exception();
                    }
                    catalog.getNode(i).updateStock(1);
                }
            }catch (Exception e){
                e.getLocalizedMessage();
            }
        }
        OrderItem orderItem = new OrderItem(productID, quantity, selecciones);
        //Actualizar el stock del menú en sí mismo
        try{
            int pos = catalog.getPosById(productID);
            Node node = catalog.getNode(pos);
            if (node.getStock() != -1){
                catalog.getNode(pos).updateStock(quantity);
            }
        }catch (Exception e){
            e.getLocalizedMessage();
        }
        orderItems.add(orderItem);
    }

    public OrderItem getOrderItem(int i) { return orderItems.get(i); }

    public void removeOrderItem (int i)
        throws  Exception{
        OrderItem orderItem = orderItems.get(i);
        String id = orderItem.getProductID();
        Integer quantity = orderItem.getQuantity();
        int pos = catalog.getPosById(id);
        Node node = catalog.getNode(pos);
        Integer stock = node.getStock();
        orderItems.remove(i);
        if (stock != -1) {
            catalog.getNode(pos).updateStock(-quantity);
        }
        /*Por si era un menú lo que se ha eliminado*/
        for(String seleccion : orderItem.getSelecciones()){
            try{
                pos = catalog.getPosById(seleccion);
                node = catalog.getNode(pos);
                stock = node.getStock();
                if (stock != -1) {
                    catalog.getNode(pos).updateStock(-1);
                }
            }catch (Exception e){
                e.getLocalizedMessage();
            }
        }
    }

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
            Node node = catalog.getNodeById(orderItem.getProductID());
            String formatPrice = node.getPrice();
            //quitar carácteres y comas, se quedan los puntos como separador de decimales
            formatPrice = formatPrice.replaceAll("[^0-9\\.]", "");
            Float subtotal = Float.parseFloat(formatPrice);
            total += subtotal * orderItem.getQuantity();
        }
        return total;
    }


    /*
    Método que calcula las cantidades de cada producto que se van a pedir,
    ya que en los menús se piden productos como elecciones que no reflejan las cantidades reales.
    Útil para verificar el stock en el server antes de procesarlo, se envía como Map adjunto.
    Se llama justo antes de hacer la compra cuando ya no va a haber más cambios
    Ej: Si se piden 3 colas y dos menús que llevan colas y ramens, en realidad son 5 colas y 2 ramens
     */
    public Map<String, Integer> calculateTotalsQuantity (){
        Map <String, Integer> totalQuantity = new HashMap<>();
        for(OrderItem orderItem : orderItems){
            String productID = orderItem.getProductID();
            Integer quantity = orderItem.getQuantity();
            if (!orderItem.getSelecciones().isEmpty()){
                for(String seleccion : orderItem.getSelecciones()){
                    if (totalQuantity.containsKey(seleccion)){
                        totalQuantity.put(seleccion, totalQuantity.get(seleccion)+1);
                    }else{
                        totalQuantity.put(seleccion, 1);
                    }
                }
            }
            if (totalQuantity.containsKey(productID)){
                totalQuantity.put(productID, totalQuantity.get(productID)+quantity);
            }else{
                totalQuantity.put(productID, quantity);
            }
        }
        return totalQuantity;
    }

    /*Método que elimina las líneas de pedidos para la id dado,
    mira también las selecciones de menú.
    Útil por si el servidor deniega la compra por falta de stock.
     */
    public void removeOrderItemByStock (String id){
        ArrayList<OrderItem> found = new ArrayList<>();
        for (int i=0; i<orderItems.size(); i++) {
            OrderItem orderItem = orderItems.get(i);
            if (orderItem.getProductID().compareTo(id) == 0){
                found.add(orderItem);
                continue;
            }
            for (String seleccion : orderItem.getSelecciones()) {
                if (seleccion.compareTo(id) == 0){
                    found.add(orderItem);
                    continue;
                }
            }
        }
        orderItems.removeAll(found);
    }
}