package com.roboticaircraftinspection.roboticinspection.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.roboticaircraftinspection.roboticinspection.R;

import java.util.ArrayList;
import java.util.List;

public class ItemArrayAdapter extends ArrayAdapter<String[]> {
    private List<String[]> waypoints = new ArrayList<>();

    static class ItemViewHolder {
        TextView XLatM;
        TextView YLonM;
        TextView alt;
        /*
        TextView latitude;
        TextView longitude;
        TextView zone;
        TextView camHeading;
        TextView gimbalPitchangle;
        TextView gimbalAngle;
        TextView actiontype1;
        TextView actionparam1;
        */
    }
    public ItemArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }
    @Override
    public void add(String[] object) {
        waypoints.add(object);
        super.add(object);
    }
    @Override
    public int getCount() {
        return waypoints.size();
    }

    @Override
    public String[] getItem(int index) {
        return waypoints.get(index);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemViewHolder viewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.item_layout, parent, false);
            viewHolder = new ItemViewHolder();
            viewHolder.XLatM = (TextView) row.findViewById(R.id.item_x);
            viewHolder.YLonM = (TextView) row.findViewById(R.id.item_y);
            viewHolder.alt = (TextView) row.findViewById(R.id.item_alt);
            /*
             viewHolder.latitude = (TextView)row.findViewById(R.id.item_latitude);
            viewHolder.longitude = (TextView)row.findViewById(R.id.item_longitude);
           viewHolder.zone = (TextView) row.findViewById(R.id.zone);
            viewHolder.camHeading = (TextView) row.findViewById(R.id.camHeading);
            viewHolder.gimbalPitchangle = (TextView) row.findViewById(R.id.gimbalPitchangle);
            viewHolder.gimbalAngle = (TextView) row.findViewById(R.id.gimbalAngle);
            viewHolder.actiontype1 = (TextView) row.findViewById(R.id.actiontype1);
            viewHolder.actionparam1 = (TextView) row.findViewById(R.id.actionparam1);
            */

            row.setTag(viewHolder);
        } else {
            viewHolder = (ItemViewHolder)row.getTag();
        }
        String[] stat = getItem(position);
        Log.d("NANCY","position: "+position+" stat: "+stat);
        viewHolder.XLatM.setText(stat[2]);
        viewHolder.YLonM.setText(stat[3]);
        viewHolder.alt.setText(stat[4]);
        /*
        viewHolder.latitude.setText(stat[0]);
        viewHolder.longitude.setText(stat[1]);
        viewHolder.zone.setText(stat[0]);
        viewHolder.camHeading.setText(stat[0]);
        viewHolder.gimbalPitchangle.setText(stat[0]);
        viewHolder.gimbalAngle.setText(stat[0]);
        viewHolder.actiontype1.setText(stat[0]);
        viewHolder.actionparam1.setText(stat[0]);
        */
        return row;
    }

}
