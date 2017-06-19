package net.rhuanbarros.construfacilnovo2.dao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import net.rhuanbarros.construfacilnovo2.models.ItemLista;

import java.util.ArrayList;


/**
 * Created by Oclemmy on 5/2/2016 for ProgrammingWizards Channel and http://www.Camposha.com.
 */
public class CustomAdapter extends BaseAdapter {
    Context c;
    ArrayList<ItemLista> itens;
    LayoutInflater inflater;
    public CustomAdapter(Context c, ArrayList<ItemLista> planets) {
        this.c = c;
        this.itens = planets;
    }
    @Override
    public int getCount() {
        return itens.size();
    }
    @Override
    public Object getItem(int position) {
        return itens.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       /* if(inflater==null)
        {
            inflater= (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if(convertView==null)
        {
            convertView=inflater.inflate(R.layout.mo,parent,false);
        }

        //TextView nameTxt= (TextView) convertView.findViewById(R.id.nameTxt);
        //nameTxt.setText(itens.get(position).getDescricao());

        final int pos=position;

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(c,itens.get(pos).getDescricao(),Toast.LENGTH_SHORT).show();
            }
        });*/
        return convertView;
    }
}