package net.konyan.yangonbusonthemap.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cocoahero.android.geojson.Feature;

import net.konyan.yangonbusonthemap.R;

import org.json.JSONException;

import java.util.List;

/**
 * Created by zeta on 1/21/17.
 */

public class BusesAdapter extends RecyclerView.Adapter<BusesAdapter.BusHolder>{

    Context context;
    //List<Integer> buses;
    List<Feature> buses;
    BusItemClickListener listener;

    private static View selected = null;

    public BusesAdapter(Context context, List<Feature> buses, BusItemClickListener listener){
        this.context = context;
        this.buses = buses;
        this.listener = listener;
    }


    @Override
    public BusHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_buses, parent, false);
        return new BusHolder(view);
    }

    @Override
    public void onBindViewHolder(final BusHolder holder, final int position) {

        final CardView cardView = (CardView) holder.itemView;

        final Feature feature = buses.get(position);
        try {
            int busName = feature.getProperties().getInt("svc_name");
            String busColor = feature.getProperties().getString("color");

            if (busName != 0 )holder.tvBusName.setText(""+busName);
            if (busColor != null) cardView.setCardBackgroundColor(Color.parseColor(busColor));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(feature);
            }
        });
    }

    @Override
    public int getItemCount() {
        return buses == null ? 0 : buses.size();
    }

    class BusHolder extends RecyclerView.ViewHolder {
        TextView tvBusName;
        public BusHolder(View itemView) {
            super(itemView);
            tvBusName = (TextView) itemView.findViewById(R.id.tv_bus_name);
        }
    }

    public interface BusItemClickListener{
        void onItemClick(Feature bus);
    }
}
