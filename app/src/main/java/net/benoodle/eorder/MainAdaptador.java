package net.benoodle.eorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import net.benoodle.eorder.model.Node;
import java.util.ArrayList;
//import static net.benoodle.eorder.retrofit.UtilsApi.BASE_URL_API;

public class MainAdaptador extends RecyclerView.Adapter<MainAdaptador.ViewHolder> {

    private Context context;
    private ComprarListener comprarListener;
    private ArrayList<Node> catType = new ArrayList<>();
    //private String URL;

    public MainAdaptador(ArrayList<Node> catType, Context context, ComprarListener comprarListener) {
        this.context = context;
        this.comprarListener = comprarListener;
        this.catType = catType;
        //this.URL = URL;
    }

    @Override
    public MainAdaptador.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_node, parent, false);

        return new ViewHolder(v, comprarListener);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView body, price, title, stock;
        private Button btComprar;
        ComprarListener comprarListener;
        //LinearLayout parentLayout;

        public ViewHolder(View itemView, ComprarListener comprarListener) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.body = itemView.findViewById(R.id.body);
            this.price = itemView.findViewById(R.id.price);
            this.image = itemView.findViewById(R.id.image);
            this.stock = itemView.findViewById(R.id.stock);
            this.comprarListener = comprarListener;
            this.btComprar = itemView.findViewById(R.id.btComprar);
            //parentLayout = itemView.findViewById(R.id.node);
        }
    }

    public void onBindViewHolder(MainAdaptador.ViewHolder holder, int i) {
        final Node node = catType.get(i);
        //Modo sin setIndicatorsEnabled para ver si es de internet o de caché
        //Picasso.with(context).load(URL+node.getUrl()).into(holder.image); URLS absolutas ahora
        Picasso.with(context).load(node.getUrl()).into(holder.image);
        //Modo con SetIndicatorsEnabled ROJO Network, AZUL disk, VERDE memory
        //Picasso mPicasso = Picasso.with(context);
        //mPicasso.setIndicatorsEnabled(true);
        //mPicasso.load(BASE_URL_API+node.getUrl()).into(holder.image);
        holder.title.setText(node.getTitle());
        holder.body.setText(node.getBody());
        if (node.getStock() != -1){
            holder.stock.setText(context.getResources().getString(R.string.remain)+" "+node.getStock().toString()+" "+context.getResources().getString(R.string.unities));
        }
        holder.price.setText(node.getPrice() + " €");
        holder.btComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comprarListener.Añadir(node, 1);
            }
        });
    }

    public int getItemCount() {
        return catType.size();
    }

    public interface ComprarListener {
        void Añadir(Node node, int cantidad);
    }


}