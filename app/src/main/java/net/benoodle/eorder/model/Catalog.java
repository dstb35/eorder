package net.benoodle.eorder.model;

import net.benoodle.eorder.R;
import java.util.ArrayList;

public class Catalog {
    private ArrayList<Node> catalog;
    private ArrayList<String> types = new ArrayList<>();
    public Catalog(ArrayList<Node> catalog) {
        this.catalog = catalog;
    }

    public Node getNodeById (String id) throws Exception{
        for (Node node : catalog){
            if (node.getProductID().compareTo(id) == 0){
                return node;
            }
        }
        throw new Exception(R.string.product_not_id + id);
    }

    /*public Node getNodeBySku (String sku) throws Exception {
        for (Node node : catalog){
            if (node.getSku().compareTo(sku) == 0){
                return node;
            }
        }
        throw new Exception(R.string.product_not_sku + sku);
    }*/

    public Node getNode (Integer i){
        return catalog.get(i);
    }


    /*Conmprueba si el sku pasado sigue en el catálogo
    Útil para comprobar si se han despublicado productos mientras se hacía la compra
    pero se han quedado en la cesta. Devuelve false si no existe.
     */
    /*public Boolean CheckNodebySku (String sku){
        for (Node node : catalog){
            if (node.getSku().compareTo(sku) == 0){
                return true;
            }
        }
        return false;
    }*/

    /*Conmprueba si el titulo pasado sigue en el catálogo
    Devuelve false si no existe.
     */
    public Boolean CheckNodebyTitle (String title){
        for (Node node : catalog){
            if (node.getProductID().compareTo(title) == 0){
                return true;
            }
        }
        return false;
    }

    /*
    Crea los tipos para la variable static types. Los tipos de productos son bebidas, ramen, etc...
     */
    public void CrearTypes (){
        for ( Node node : catalog){
            if (!types.contains(node.getType())){
                types.add(node.getType());
            }
        }
    }

    /*
    Devuelve los productos del catálogo que coinciden con el parámetro String dado.
    Comprueba que haya stock.
    Ej. Si el tipo es bebidas devolverá todas las bebidas del catalog: coca-cola, fanta, etc...
     */

    public ArrayList<Node> OpcionesMenu (String tipo){
        ArrayList<Node> opciones = new ArrayList<>();
        for (Node node : catalog){
            if  ((node.getType().toLowerCase().compareTo(tipo) == 0) && (this.isStock(node.getProductID(), 1))){
                opciones.add(node);
            }
        }
        return opciones;
    }

    /* Devuelve TRUE si hay stock para el id y cantidades pasados por parámetro */
    public Boolean isStock (String id, Integer quantity){
        try{
            Node node = this.getNodeById(id);
            if ( node.getStock() >= quantity || node.getStock() == -1){
                return Boolean.TRUE;
            }
        }catch (Exception e){
            e.getLocalizedMessage();
        }
        return Boolean.FALSE;
    }

    /*
    Devuelve el sku del título de producto pasado por parámetro
     */

    public Integer getPosById(String id){
        for (int i=0; i < catalog.size(); i++){
            Node node = catalog.get(i);
            if (node.getProductID().toLowerCase().compareTo(id.toLowerCase()) == 0){
                return i;
            }
        }
        return null;
    }

    /*
    Devuelve los productos por tipos.
    catType es el catálogo para un tipo de producto determinado.
    Ej. Si tipo es bebidas devolverá todas los Node de tipo bebidas
     */
    public ArrayList<Node> TypeCatalog (String tipo){
        ArrayList<Node> catType = new ArrayList<>();
        if (tipo == null){
            catType = catalog;
        }
        else {
            for (Node node : catalog){
                if (node.getType().compareTo(tipo) == 0){
                    catType.add(node);
                }
            }
        }
        return catType;
    }

    public void actualizarStock (String id, String stock){
        int i = this.getPosById(id);
        this.catalog.get(i).setStock(Integer.valueOf(stock));
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }

    /*Método que sincroniza el stock del catálogo con las línes de pedido del carrito.
    Útil por si se recarga el catalágo teniendo un carrito lleno
    Devuelve false si no hubo que modificar el carrito. True si se quitaron productos.
     */
    /*public Boolean sincronizarStock(Order order){
        Boolean result = Boolean.FALSE;
        for (int i=0; i<order.getOrderItemsSize(); i++){
            OrderItem orderItem = order.getOrderItem(i);
            String sku = orderItem.getSku();
            Integer quantity = orderItem.getQuantity();
            try {
                Node node = this.getNodeBySku(sku);
                Integer stock = Integer.valueOf(node.getStock());
                if (stock != -1) {
                    if (stock > quantity) {
                        node.setStock((stock - quantity));
                    } else if (stock < quantity) {
                        order.getOrderItem(i).setQuantity(stock);
                        node.setStock(0);
                    } else if (stock == quantity) {
                        node.setStock(0);
                    }
                    result = Boolean.TRUE;
                }

                //Mirar los menús, en teoría solo los menús tiene selecciones.
                for (String id : orderItem.getSelecciones()) {
                    Integer pos = this.getPosById(id);
                    Node seleccion = this.getNode(pos);
                    Integer seleccionStock = seleccion.getStock();
                    if (seleccionStock < 1 && seleccionStock != -1) {
                        throw new Exception();
                    }
                    node.setStock(seleccionStock - 1);
                    this.setNode(pos, seleccion);
                }
            }catch (Exception e){
                e.getLocalizedMessage();
            }
        }
        return result;
    }*/
}
