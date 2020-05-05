package net.benoodle.eorder;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import net.benoodle.eorder.model.Node;
import static net.benoodle.eorder.MainActivity.catalog;
import static net.benoodle.eorder.MainActivity.order;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    private ArrayList<String> selecciones = new ArrayList<>();
    private Node node;
    private int numRepeticiones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String sku = getIntent().getStringExtra("sku");
        try {
            node = catalog.getNodeBySku(sku);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
        PedirSelecciones();
    }

    public void PedirSelecciones() {
        //productos[] son las opciones asociadas al menú.
        final String[] productos = node.getProductos().split(",", -2);
        numRepeticiones = productos.length;

        /*Quitar espacios en blanco y retornos de carro de las cadenas y poner a minúsculas*/
        for (int i = 0; i < productos.length; i++) {
            productos[i] = productos[i].replaceAll("\\s", "").toLowerCase();
        }

        for (String producto : productos) {
            final ArrayList<Node> opciones = catalog.OpcionesMenu(producto);
            final String[] titulos = new String[opciones.size()];
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
                        order.addMenuItem(selecciones, node.getSku(), 1, node.getTitle());
                        Toast.makeText(getApplicationContext(), R.string.menu_added, Toast.LENGTH_SHORT).show();
                        finish();
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
}