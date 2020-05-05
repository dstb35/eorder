package net.benoodle.eorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import net.benoodle.eorder.model.Node;
import net.benoodle.eorder.model.OrderItem;

import static net.benoodle.eorder.MainActivity.MENU;
import static net.benoodle.eorder.MainActivity.catalog;
import static net.benoodle.eorder.MainActivity.order;
import static net.benoodle.eorder.retrofit.UtilsApi.BASE_URL_API;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private Context context;
    private EliminarListener eliminarListener;

    public CartAdapter(Context context, EliminarListener eliminarListener) {
        this.context = context;
        this.eliminarListener = eliminarListener;
    }

    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_node, parent, false);
        return new ViewHolder(v, eliminarListener);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView price, title, quantity, selecciones;
        public Button btEliminar, btMas, btMenos;
        //public NumberPicker qtPicker;
        LinearLayout parentLayout;
        EliminarListener eliminarListener;

        public ViewHolder(View itemView, EliminarListener eliminarListener) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.selecciones = itemView.findViewById(R.id.selecciones);
            this.price = itemView.findViewById(R.id.price);
            this.image = itemView.findViewById(R.id.image);
            this.quantity = itemView.findViewById(R.id.quantity);
            parentLayout = itemView.findViewById(R.id.cart_node);
            //this.qtPicker = itemView.findViewById(R.id.qtPicker);
            this.eliminarListener = eliminarListener;
            this.btEliminar = itemView.findViewById(R.id.btEliminar);
            this.btMas = itemView.findViewById(R.id.Btmas);
            this.btMenos = itemView.findViewById(R.id.Btmenos);
        }
    }

    public void onBindViewHolder(final CartAdapter.ViewHolder holder, final int i) {

        OrderItem orderItem = order.getOrderItems().get(i);
        try {
            final Node node = catalog.getNodeBySku(orderItem.getSku());
            Picasso.with(context).load(BASE_URL_API+node.getUrl()).into(holder.image);
            holder.title.setText(node.getTitle());
            if (node.getType().compareTo(MENU) == 0){
                holder.selecciones.setText(order.getSeleccionesByID(orderItem));
                holder.selecciones.setVisibility(View.VISIBLE);
               // holder.qtPicker.setVisibility(View.GONE);
            }
            holder.price.setText(node.getPrice().toString());
            holder.quantity.setText(String.valueOf(orderItem.getQuantity()));
            /*holder.qtPicker.setMinValue(1);
            holder.qtPicker.setMaxValue(10);
            holder.qtPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    //Arreglar esto, muestra mal el total
                    order.addOrderItem(node.getSku(), newVal);
                    holder.quantity.setText(String.valueOf(newVal));
                    eliminarListener.ActualizarTotal();
                }
            });*/
            holder.btEliminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Eliminamos por la posición
                    eliminarListener.Eliminar(i);
                    eliminarListener.ActualizarTotal();
                }
            });
            holder.btMas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    order.addOrderItem(node.getSku(), 1, node.getTitle());
                    eliminarListener.ActualizarTotal();
                }
            });
            holder.btMenos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    order.addOrderItem(node.getSku(), -1, node.getTitle());
                    eliminarListener.ActualizarTotal();
                }
            });

        } catch (Exception e) {
           holder.title.setText(e.getMessage());
        }
    }

    public int getItemCount() {
        return order.getOrderItems().size();
    }

    public interface EliminarListener{
        void Eliminar(int i);
        void ActualizarTotal();
    }
}