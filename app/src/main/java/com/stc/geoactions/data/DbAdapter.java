package com.stc.geoactions.data;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stc.geoactions.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by artem on 7/17/17.
 */

public class DbAdapter extends RecyclerView.Adapter{
    public List<DbEntry> items;

    public DbAdapter() {
        this.items = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return  new DbViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DbEntry entry=items.get(position);
        DbViewHolder vh=(DbViewHolder)holder;
        vh.timestamp.setText(getDateCurrentTimeZone(entry.timestamp));
        vh.isInside.setText(""+entry.isInside);
    }
    public  String getDateCurrentTimeZone(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
    }
    @Override
    public int getItemCount() {
        return items.size();
    }
    class DbViewHolder extends RecyclerView.ViewHolder{
        public TextView timestamp;
        public TextView isInside;
        public View itemView;
        public DbViewHolder(View itemView) {
            super(itemView);
            this.itemView=itemView;
            this.isInside= (TextView) itemView.findViewById(R.id.text_position);
            this.timestamp= (TextView) itemView.findViewById(R.id.text_timestamp);
        }
    }

}
