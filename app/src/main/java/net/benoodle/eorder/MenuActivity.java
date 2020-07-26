package net.benoodle.eorder;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import net.benoodle.eorder.model.Node;
import static net.benoodle.eorder.TypesActivity.catalog;
import static net.benoodle.eorder.MainActivity.order;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private ArrayList<String> selecciones = new ArrayList<>();
    private Node node;
    private int numRepeticiones;
    private String[] titulos;
    private ArrayList<String> productos;
    private List<String> extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String id = getIntent().getStringExtra("id");
        try {
            node = catalog.getNodeById(id);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
        if (!catalog.isStock(node.getProductID(), 1)){
            Toast.makeText(getApplicationContext(), R.string.no_sell, Toast.LENGTH_SHORT).show();
            finish();
        }
        PedirSelecciones();
    }

    public void PedirSelecciones() {
        //productos[] son las opciones asociadas al menú.
        productos = node.getProductos();
        numRepeticiones = productos.size();

        //extras son los productos asociados al menú, postres del menú be noodle
        //Array.asList devuelve un array inmodificable, hay que usar un wrapper
        //extras =  Arrays.asList(node.getExtras().split(",", -2));
        extras = node.getExtras();

        for (String producto : productos) {
            final ArrayList <Node> opciones = catalog.OpcionesMenu(producto);
            if (opciones.isEmpty()){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_stock)+producto, Toast.LENGTH_SHORT).show();
                finish();
            }
            titulos = new String[opciones.size()];
            for (int i = 0; i < opciones.size(); i++) {
                titulos[i] = opciones.get(i).getTitle();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
            builder.setTitle(getResources().getString(R.string.choose) + producto);
            builder.setCancelable(false);
            builder.setSingleChoiceItems(titulos, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int position) {
                    //La posición de productos[] debería coincidir con la posición de opciones.
                    selecciones.add(opciones.get(position).getProductID());
                    dialog.dismiss();
                    if (numRepeticiones == 1){
                        //En la última repetición se pide los extras, normalmente postres
                        if (!extras.isEmpty()){
                            pedirExtras();
                        }else{
                            //No hay comprobación de stock porque al menos una unidad habrá o se hubiese despublicado el producto.
                            try{
                                order.addMenuItem(node.getProductID(), selecciones, 1);
                            }catch (Exception e){
                                Toast.makeText(getApplicationContext(), R.string.no_sell, Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(getApplicationContext(), R.string.menu_added, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }else{
                        numRepeticiones--;
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), R.string.menu_canceled, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void pedirExtras(){

        try {
            /*Quitar espacios en blanco y retornos de carro de las cadenas y poner a minúsculas
            * Mirar stock en catalog.isStock()
            */
            ArrayList <String> found = new ArrayList<>();
            for (int i = 0; i < extras.size(); i++) {
                String id = extras.get(i).replaceAll("\\s", "");
                extras.set(i, id);
                if (!catalog.isStock(id, 1)) {
                    found.add(id);
                }
            }
            extras.removeAll(found);
            if (!extras.isEmpty()){
                final String [] extrasTitulos = new String[extras.size()];
                for (int i = 0; i < extras.size(); i++) {
                    try {
                        extrasTitulos[i] = catalog.getNodeById(extras.get(i)).getTitle();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                builder.setTitle(getResources().getString(R.string.choose));
                builder.setCancelable(false);
                builder.setSingleChoiceItems(extrasTitulos, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        selecciones.add(extras.get(position));
                        try{
                            order.addMenuItem(node.getProductID(), selecciones,1);
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), R.string.no_sell, Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getApplicationContext(), R.string.menu_added, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        setResult(0);
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), R.string.menu_canceled, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_stock_dessert), Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}