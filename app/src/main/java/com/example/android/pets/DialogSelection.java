package com.example.android.pets;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
//import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.app.AlertDialog;

public class DialogSelection extends DialogFragment implements View.OnClickListener {
    private final String[] items = {"On Duty", "Off Duty", "Sleeper", "Driving"};//your data
    private ItemAdapter itemAdapter;//adapter
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (itemAdapter.getSelectPosition() == i) {
                return;
            }
//            alertDialog.dismiss();
            itemAdapter.select(i);
            //your code
            Log.i("Dialogos", "Option Selected: " + items[i]);
            switch (items[i]) {
                case "On Duty":
                    break;
                case "Off Duty":
                    break;
                case "Sleeper":
                    break;
                case "Driving":
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (itemAdapter == null) {
            itemAdapter = new ItemAdapter(getActivity(), items, -1);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Selection");
        final AlertDialog alertDialog = builder.create();
        ListView listView = new ListView(getActivity());
        alertDialog.setView(listView);//use custom ListView
        listView.setAdapter(itemAdapter);//user custom Adapter
        listView.setOnItemClickListener(onItemClickListener);
        return alertDialog;
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * Custom Adapter
     */
    private class ItemAdapter extends BaseAdapter {
        private String[] data = {"hello", "my", "friend"};
        private LayoutInflater layoutInflater;
        private int selectPosition = -1;//disabled item position

        public void select(int selectPosition) {
            this.selectPosition = selectPosition;
        }

        public int getSelectPosition() {
            return selectPosition;
        }

        public ItemAdapter(Context contexts, String[] data, int selectPosition) {
            layoutInflater = LayoutInflater.from(getActivity());
            this.data = data;
            this.selectPosition = selectPosition;
        }

        @Override
        public int getCount() {
            return data.length;
        }

        @Override
        public Object getItem(int i) {
            return data[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView textView = (TextView) layoutInflater.inflate(android.R.layout.simple_expandable_list_item_1, null);//use system default layout
            textView.setPadding(20, 20, 20, 20);
            textView.setText(data[i]);//set data
            textView.setEnabled(this.selectPosition != i);//disable or enable
            return textView;
        }
    }
}
