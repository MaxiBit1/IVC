package com.example.ivc;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class List_bt extends BaseAdapter {

    private static final int RESOURS_LAYOUT = R.layout.list_search;
    private ArrayList<BluetoothDevice> bt_List = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private int TypeIcon;

    public List_bt(Context context,ArrayList<BluetoothDevice> bt_List, int TypeIcon) {
        this.bt_List = bt_List;
        this.TypeIcon = TypeIcon;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return bt_List.size();
    }

    @Override
    public Object getItem(int position) {
        return getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layoutInflater.inflate(RESOURS_LAYOUT,parent,false);

        BluetoothDevice device = bt_List.get(position);
        if(device !=null){
            ((TextView) view.findViewById(R.id.Name_device)).setText(device.getName());
            ((TextView) view.findViewById(R.id.Mac_device)).setText(device.getAddress());
            ((ImageView) view.findViewById(R.id.image_Bt)).setImageResource(TypeIcon);
        }
        return view;
    }
}
