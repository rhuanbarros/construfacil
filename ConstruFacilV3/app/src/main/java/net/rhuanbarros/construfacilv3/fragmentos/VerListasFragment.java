package net.rhuanbarros.construfacilv3.fragmentos;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.rhuanbarros.construfacilv3.R;
import net.rhuanbarros.construfacilv3.adapters.ExpandableListAdapter;
import net.rhuanbarros.construfacilv3.models.ItemLista;
import net.rhuanbarros.construfacilv3.models.Lista;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Belal on 18/09/16.
 */


public class VerListasFragment extends Fragment {
    private static final String TAG = "VerListasFragment";
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<ItemLista>> listDataChild;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ver_listas, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Listas de materiais já enviadas");

        expListView = (ExpandableListView) view.findViewById(R.id.lvExp);

        getListaSalvaNoFirebase();
    }

    public void getListaSalvaNoFirebase() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Carregando dados do servidor");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("listasSalvas");
        final List<Lista> listasSalvas = new ArrayList<>();

        mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                listasSalvas.clear();
                listDataHeader = new ArrayList<String>();
                listDataChild = new HashMap<String, List<ItemLista>>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Lista lista = postSnapshot.getValue(Lista.class);
                    Log.d(TAG, "listasSalvas.add -> "+lista.toString() );
                    listasSalvas.add(lista);
                    listDataHeader.add(lista.getTimestamp());
                    listDataChild.put(lista.getTimestamp(), lista.getLista());
                }
                listAdapter = new ExpandableListAdapter(getContext(), listDataHeader, listDataChild);
                expListView.setAdapter(listAdapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }
}