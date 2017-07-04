package net.rhuanbarros.construfacilv3.adapters;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.rhuanbarros.construfacilv3.R;
import net.rhuanbarros.construfacilv3.models.ItemLista;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rhuanbarros on 30/06/2017.
 */

public class CatalogoRecyclerAdapter extends RecyclerView.Adapter<CatalogoRecyclerAdapter.ViewHolder > {
    private static final String TAG = "CatalogoRecyclerAdapter";
    private final List<ItemLista> lista;
    static final DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("listaNova");

    public CatalogoRecyclerAdapter(List<ItemLista> listaRecebida) {
        lista = listaRecebida;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private static final String TAG = "CatalogoRec.ViewHolder";
        private View mView;
        private TextView tx_descricao;
        private ItemLista mItem;

        public ViewHolder(View view) {
            super(view);

            mView = view;
            tx_descricao = (TextView) view.findViewById(R.id.tx_descricao);
            view.setOnClickListener(this);
        }

        public void insereNoViewHolder(ItemLista item) {
            mItem = item;
            tx_descricao.setText( item.getDescricao() );
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "---->onClick");
            adicionaNaLista(mItem, v);
        }
    }

    private static void adicionaNaLista(final ItemLista itemLista, final View v) {
        Log.d(TAG, "Abrindo Firebase em busca de " + itemLista.getDescricao());

        mFirebaseDatabaseReference.child(itemLista.getId().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ItemLista lancamentoAnterior = null;
                try {
                    lancamentoAnterior = dataSnapshot.getValue(ItemLista.class);
                    Log.d(TAG, "onDataChange Lancamento: " + lancamentoAnterior.toString());
                } catch (NullPointerException e) {
                    Log.d(TAG, "catch (NullPointerException e) -> Não existe registro no Firebase ainda desse produto lançado");

                    //por isso ele lança o NullPointer, nesse caso é só colocar um novo registro

                    itemLista.setQuantidade(1L);//TO DO pegar quantidade do lugar certo

                    mFirebaseDatabaseReference.child(itemLista.getId().toString()).setValue(itemLista);
                    Log.d(TAG, "Lancamento novo registro: " + itemLista.toString());
                }
                //se lancamento ja existe, tem que fazer atualização
                boolean lancamentoJaExiste = lancamentoAnterior != null;
                if (lancamentoJaExiste) {
                    Log.d(TAG, "if( lancamentoJaExiste )");
                    Long quantidadeAnterior = lancamentoAnterior.getQuantidade();
                    //Long quantidadeNova = quantidadeAnterior + itemLista.getQuantidade();
                    Long quantidadeNova = quantidadeAnterior + 1;

                    Map<String, Object> atualizarQuantidade = new HashMap<String, Object>();
                    atualizarQuantidade.put("quantidade", quantidadeNova);

                    mFirebaseDatabaseReference.child(itemLista.getId().toString()).updateChildren(atualizarQuantidade);
                }
                Snackbar.make(v, R.string.lancamento_sucesso, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled(DatabaseError databaseError)");
            }

        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_catalogo_recyclerview, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.insereNoViewHolder(lista.get(position) );
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
