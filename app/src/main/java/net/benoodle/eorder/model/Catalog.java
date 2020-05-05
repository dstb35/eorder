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

    public Node getNodeBySku (String sku) throws Exception {
        for (Node node : catalog){
            if (node.getSku().compareTo(sku) == 0){
                return node;
            }
        }
        throw new Exception(R.string.product_not_sku + sku);
    }

    /*Conmprueba si el sku pasado sigue en el catálogo
    Útil para comprobar si se han despublicado productos mientras se hacía la compra
    pero se han quedado en la cesta. Devuelve false si no existe.
     */
    public Boolean CheckNodebySku (String sku){
        for (Node node : catalog){
            if (node.getSku().compareTo(sku) == 0){
                return true;
            }
        }
        return false;
    }

    /*Conmprueba si el titulo pasado sigue en el catálogo
    Útil para comprobar si se han despublicado productos mientras se hacía la compra
    pero se han quedado en la cesta. Devuelve false si no existe.
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
    Ej. Si el tipo es bebidas devolverá todas las bebidas del catalog: coca-cola, fanta, etc...
     */

    public ArrayList<Node> OpcionesMenu (String tipo){
        ArrayList<Node> opciones = new ArrayList<>();
        for (Node node : catalog){
            String type = node.getType().toLowerCase();
            if  (node.getType().toLowerCase().compareTo(tipo) == 0){
                opciones.add(node);
            }
        }
        return opciones;
    }

    /*
    Devuelve el sku del título de producto pasado por parámetro
     */

    public String getNodeByTitle (String title){
        for (Node node : catalog){
            /*if (node.getTitle().toLowerCase().equals(title.toLowerCase())){
                return node.getSku();
            }*/
            if (node.getTitle().toLowerCase().compareTo(title.toLowerCase()) == 0){
                return node.getSku();
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

    public ArrayList<String> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }
}
