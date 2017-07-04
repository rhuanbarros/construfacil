package net.rhuanbarros.construfacilv3.adapters;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.rhuanbarros.construfacilv3.R;
import net.rhuanbarros.construfacilv3.models.ItemLista;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.rhuanbarros.construfacilv3.adapters.CatalogoRecyclerAdapter.mFirebaseDatabaseReference;

/**
 * Created by rhuanbarros on 01/07/2017.
 */

public class CriarListaRecyclerAdapter extends RecyclerView.Adapter<CriarListaRecyclerAdapter.ViewHolder > {
    private static final String TAG = "CriarListaRecAdap";
    public static List<ItemLista> lista;
    static final DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("listaNova");

    public CriarListaRecyclerAdapter(List<ItemLista> lista) {
        CriarListaRecyclerAdapter.lista = lista;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_criar_lista_recyclerview, parent, false);
        return new CriarListaRecyclerAdapter.ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.insereNoViewHolder( lista.get(position) );
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        static final String TAG = "CriarListaViewHolder";
        TextView tx_descricao_lista;
        TextView tx_quantidade_lista;
        public View mView;
        private ItemLista mItem;

        public ViewHolder(View view) {
            super(view);
            tx_descricao_lista = (TextView) view.findViewById(R.id.tx_descricao_lista);
            tx_quantidade_lista = (TextView) view.findViewById(R.id.tx_quantidade_lista);
            view.setOnClickListener(this);
            mView = view;
        }

        public void insereNoViewHolder(ItemLista item) {
            mItem = item;
            tx_descricao_lista.setText(item.getDescricao());
            tx_quantidade_lista.setText("Quantidade: "+item.getQuantidade().toString());

            Log.d(TAG, "insereNoViewHolder"+item.toString());
        }

        @Override
        public void onClick(View v) {
            removeDaLista(mItem, v);
        }

    }

    private static void removeDaLista(final ItemLista itemLista, final View v) {
        mFirebaseDatabaseReference.child(itemLista.getId().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ItemLista lancamentoAnterior = null;
                try {
                    lancamentoAnterior = dataSnapshot.getValue(ItemLista.class);
                    Log.d(TAG, "onDataChange Lancamento: " + lancamentoAnterior.toString());
                } catch (NullPointerException e) {
                    Log.d(TAG, "catch (NullPointerException e) -> Não existe registro no Firebase ainda desse produto lançado");
                    //por isso ele lança o NullPointer, nesse caso nao faz nada
                }
                //se lancamento ja existe, tem que fazer atualização
                boolean lancamentoJaExiste = lancamentoAnterior != null;
                if (lancamentoJaExiste) {
                    Log.d(TAG, "if( lancamentoJaExiste )");
                    Long quantidadeAnterior = lancamentoAnterior.getQuantidade();
                    Long quantidadeNova = quantidadeAnterior -1;

                    if(quantidadeNova> 0) {
                        Map<String, Object> atualizarQuantidade = new HashMap<String, Object>();
                        atualizarQuantidade.put("quantidade", quantidadeNova);

                        mFirebaseDatabaseReference.child(itemLista.getId().toString()).updateChildren(atualizarQuantidade);
                    } else { //se a quantidade chegar a Zero, tem q remover da lista
                        mFirebaseDatabaseReference.child(itemLista.getId().toString()).removeValue();
                        Snackbar.make(v, "Item removido: "+itemLista.getDescricao(), Snackbar.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled(DatabaseError databaseError)");
            }

        });
    }
}
