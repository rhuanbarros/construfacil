package net.rhuanbarros.construfacilv3.fragmentos;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.rhuanbarros.construfacilv3.R;
import net.rhuanbarros.construfacilv3.adapters.CatalogoRecyclerAdapter;
import net.rhuanbarros.construfacilv3.adapters.CriarListaRecyclerAdapter;
import net.rhuanbarros.construfacilv3.dao.DBAdapter;
import net.rhuanbarros.construfacilv3.dao.DatabaseHelper;
import net.rhuanbarros.construfacilv3.models.ItemLista;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.rhuanbarros.construfacilv3.R.id.txtPesquisa;

/**
 * Created by Belal on 18/09/16.
 */


public class CriarListaFragment extends Fragment {
    private static final String TAG = "CriarListaFragment";

    private CatalogoRecyclerAdapter catalogoAdapter;
    private RecyclerView catalogoRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_criar_lista, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Criar lista de materiais");

        getListaSalvaNoFirebase();
        inicilizaBancoDeDadosNoSQL();
        inicializaEventos();
        inicializaCatalogo(savedInstanceState);
    }

    public void getListaSalvaNoFirebase() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Carregando dados do servidor");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        FirebaseUser  mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(mFirebaseUser.getUid()).child("listaNova");
        //CRIAR LISTA DA DIREITA
        final RecyclerView criarListaRecyclerView = (RecyclerView) getView().findViewById(R.id.criar_lista_recyclerview);
        criarListaRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        final List<ItemLista> listaSalvaNoFirebase = new ArrayList<>();

        mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                listaSalvaNoFirebase.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    ItemLista itemLista = postSnapshot.getValue(ItemLista.class);
                    Log.d(TAG, "listaSalvaNoFirebase.add -> "+itemLista.toString() );
                    listaSalvaNoFirebase.add(itemLista);
                }
                CriarListaRecyclerAdapter criarListaAdapter = new CriarListaRecyclerAdapter(listaSalvaNoFirebase);
                criarListaRecyclerView.setAdapter(criarListaAdapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }

    private void inicilizaBancoDeDadosNoSQL() {
        DatabaseHelper myDbHelper = new DatabaseHelper(getActivity());
        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            myDbHelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }
        //Toast.makeText(getActivity(), "Successfully Imported", Toast.LENGTH_SHORT).show();
    }

    private void inicializaEventos() {
        //evento na caixa de pesquisa
        EditText txtPesquisa = (EditText) getView().findViewById(R.id.txtPesquisa );
        txtPesquisa.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                atualizaCatalogo(s.toString());
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void inicializaCatalogo(Bundle savedInstanceState) {
        //CATALOGO DA ESQUERDA
        catalogoRecyclerView = (RecyclerView) getView().findViewById(R.id.catalogo_recyclerview);
        catalogoRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        DBAdapter dbAdapter = new DBAdapter(getActivity());
        dbAdapter.openDB();

        Cursor cursor = dbAdapter.retrieve("joelho");
        List<ItemLista> lista = new ArrayList<>();

        if (cursor != null) {
            while(cursor.moveToNext()) {
                Integer id = cursor.getInt(0);
                String descricao = cursor.getString(1);
                ItemLista itemLista = new ItemLista(new Long(id), descricao);
                lista.add(itemLista);
            }
            cursor.close();
        }
        dbAdapter.closeDB();

        catalogoAdapter = new CatalogoRecyclerAdapter(lista);
        catalogoRecyclerView.setAdapter(catalogoAdapter);
    }

    private void atualizaCatalogo(String produto) {
        DBAdapter dbAdapter = new DBAdapter(getActivity());
        dbAdapter.openDB();

        Cursor cursor = dbAdapter.retrieve(produto);
        ArrayList<ItemLista> lista = new ArrayList<ItemLista>();

        if (cursor != null) {
            while(cursor.moveToNext()) {
                Integer id = cursor.getInt(0);
                String descricao = cursor.getString(1);
                ItemLista itemLista = new ItemLista(new Long(id), descricao);
                lista.add(itemLista);
            }
            cursor.close();
        }
        dbAdapter.closeDB();

        catalogoAdapter = new CatalogoRecyclerAdapter(lista);
        catalogoRecyclerView.setAdapter(catalogoAdapter);
    }
}