package net.rhuanbarros.construfacilnovo2;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.rhuanbarros.construfacilnovo2.CatalogoFragment.OnListFragmentInteractionListener;
import net.rhuanbarros.construfacilnovo2.models.ItemLista;

import java.util.List;

public class MyCatalogoRecyclerViewAdapter extends RecyclerView.Adapter<MyCatalogoRecyclerViewAdapter.ViewHolder> {

    private final List<ItemLista> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyCatalogoRecyclerViewAdapter(List<ItemLista> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_catalogo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.tx_descricao.setText(mValues.get(position).getDescricao());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView tx_descricao;
        public ItemLista mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tx_descricao = (TextView) view.findViewById(R.id.tx_descricao);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + tx_descricao.getText() + "'";
        }
    }
}
