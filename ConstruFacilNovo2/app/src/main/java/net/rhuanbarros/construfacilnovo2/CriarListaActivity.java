package net.rhuanbarros.construfacilnovo2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.design.widget.FloatingActionButton;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import net.rhuanbarros.construfacilnovo2.dao.DBAdapter;
import net.rhuanbarros.construfacilnovo2.dao.DatabaseHelper;
import net.rhuanbarros.construfacilnovo2.models.ItemLista;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CriarListaActivity extends AppCompatActivity  implements  CatalogoFragment.OnListFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener  {

    private static final String TAG = "CriarListaActivity";
    final DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

    @BindView(R.id.criarListaView)
    View criarListaView;

    @BindView(R.id.listaView)
    ListView listaView;

    @BindView(R.id.txtPesquisa)
    EditText txtPesquisa;

    private FirebaseListAdapter<ItemLista> mAdapterFireBase;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_lista);

        criaMenuLateral();

        ButterKnife.bind(this);
        getListaSalvaNoFirebase();
        inicilizaBancoDeDadosNoSQL();
        inicializaEventos();
        inicializaCatalogo(savedInstanceState);

        ajustesMenuLateral();
    }

    private void ajustesMenuLateral() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "================================>"+ auth.getCurrentUser().getDisplayName() +" "+ auth.getCurrentUser().getPhotoUrl());

            TextView txtViewNomeUsuario  = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtViewNomeUsuario);
            TextView txtViewEmailUsuario = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtViewEmailUsuario);
            ImageView fotoUsuario = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageViewUsuario);

            txtViewNomeUsuario.setText(auth.getCurrentUser().getDisplayName());
            txtViewEmailUsuario.setText(auth.getCurrentUser().getEmail());

            Picasso.with(this).load(auth.getCurrentUser().getPhotoUrl()).into(fotoUsuario);

        } else {
            Log.d(TAG, "auth.getCurrentUser() == null");
        }

//        (Item) navigationView.getHeaderView(0).findViewById(R.id.nav_logout);
    }

    private void criaMenuLateral() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Você realmente deseja realizar logout?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            signOutClicked();
                        }})
                    .setNegativeButton("Não", null).show();
        } else if (id == R.id.nav_deletar_conta) {
            new AlertDialog.Builder(this)
                    .setTitle("Apagar conta")
                    .setMessage("Você realmente deseja deletar sua conta?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            deleteAccount();
                        }})
                    .setNegativeButton("Não", null).show();

        } else if (id == R.id.nav_listas) {
            Toast.makeText(CriarListaActivity.this, "Implemetar esta funcionalidade", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_enviar) {
            Toast.makeText(CriarListaActivity.this, "Implemetar esta funcionalidade", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void inicializaCatalogo(Bundle savedInstanceState) {
        DBAdapter dbAdapter = new DBAdapter(this);
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

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.framelayout_catalogo, CatalogoFragment.newInstance(1, lista), "rageComicList")
                    .commit();
        }
    }

    private void atualizaCatalogo(String produto) {
        DBAdapter dbAdapter = new DBAdapter(this);
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

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.framelayout_catalogo, CatalogoFragment.newInstance(1, lista))
                .addToBackStack(null)
                //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private void inicializaEventos() {
        txtPesquisa.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                atualizaCatalogo(s.toString());
            }
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        listaView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ItemLista itemLista = (ItemLista) parent.getItemAtPosition(position);

                Log.d(TAG, "Evento: listaView.setOnItemClickListener");
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
                                Snackbar.make(criarListaView, "Item removido: "+itemLista.getDescricao(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled(DatabaseError databaseError)");
                    }

                });
            }
        });

    }

    private void getListaSalvaNoFirebase() {
        mAdapterFireBase = new FirebaseListAdapter<ItemLista>(this, ItemLista.class, android.R.layout.two_line_list_item, mFirebaseDatabaseReference) {
            @Override
            protected void populateView(View view, ItemLista itemLista, int position) {
                ((TextView)view.findViewById(android.R.id.text1)).setText(itemLista.getDescricao());
                ((TextView)view.findViewById(android.R.id.text2)).setText("Quantidade: "+itemLista.getQuantidade().toString());
            }
        };
        listaView.setAdapter(mAdapterFireBase);
    }

    /*private void getPesquisa(String pesquisa) {
        DBAdapter dbAdapter = new DBAdapter(this);
        dbAdapter.openDB();

        Cursor cursor = dbAdapter.retrieve(pesquisa);

        ListAdapter myAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
                cursor,  new String[] { "Nome" }, new int[] { android.R.id.text1 });

        //catalogoView.setAdapter(myAdapter);

        /*cursor.moveToNext();
        String id = cursor.getString(0);
        String material = cursor.getString(1);
        showMessage(id+" "+material);*/
    //}

    private void inicilizaBancoDeDadosNoSQL() {
        DatabaseHelper myDbHelper = new DatabaseHelper(CriarListaActivity.this);
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
        //Toast.makeText(CriarListaActivity.this, "Successfully Imported", Toast.LENGTH_SHORT).show();
    }

    public static Intent createIntent(Context context, IdpResponse idpResponse) {
        Intent in = IdpResponse.getIntent(idpResponse);
        in.setClass(context, CriarListaActivity.class);
        return in;
    }

    public void signOutClicked() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(MainActivity.createIntent(CriarListaActivity.this));
                            finish();
                        } else {
                            showSnackbar(R.string.sign_out_failed);
                        }
                    }
                });
    }

    public void deleteAccountClicked() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes, nuke it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAccount();
                    }
                })
                .setNegativeButton("No", null)
                .create();

        dialog.show();
    }

    private void deleteAccount() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(MainActivity.createIntent(CriarListaActivity.this));
                            finish();
                        } else {
                            showSnackbar(R.string.delete_account_failed);
                        }
                    }
                });
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(criarListaView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapterFireBase.cleanup();
    }

    @Override
    public void onListFragmentInteraction(ItemLista item) {
        adicionaNaLista(item);
    }

    private void adicionaNaLista(final ItemLista itemLista ) {
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
                    Long quantidadeNova = quantidadeAnterior + itemLista.getQuantidade();

                    Map<String, Object> atualizarQuantidade = new HashMap<String, Object>();
                    atualizarQuantidade.put("quantidade", quantidadeNova);

                    mFirebaseDatabaseReference.child(itemLista.getId().toString()).updateChildren(atualizarQuantidade);
                }
                Snackbar.make(criarListaView, "Lançamento efetuado com sucesso", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled(DatabaseError databaseError)");
            }

        });
    }
}
