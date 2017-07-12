package net.rhuanbarros.construfacilv3;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


import net.rhuanbarros.construfacilv3.adapters.CriarListaRecyclerAdapter;
import net.rhuanbarros.construfacilv3.email.GMailSender;
import net.rhuanbarros.construfacilv3.email.GMailSenderConstruFacil;
import net.rhuanbarros.construfacilv3.email.Utils;
import net.rhuanbarros.construfacilv3.fragmentos.CriarListaFragment;
import net.rhuanbarros.construfacilv3.fragmentos.VerListasFragment;
import net.rhuanbarros.construfacilv3.login.LoginActivity;
import net.rhuanbarros.construfacilv3.models.ItemLista;
import net.rhuanbarros.construfacilv3.models.Lista;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";

    static DatabaseReference mFirebaseDatabaseReference;
    static final DatabaseReference mFirebaseDatabaseReferenceListaNova = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String nomeDoUsuario;
    public static final String ANONYMOUS = "anonymous";
    private GoogleApiClient mGoogleApiClient;

    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        //variaveis para login
        nomeDoUsuario = ANONYMOUS;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        boolean usuarioLogado = mFirebaseUser != null;
        if (usuarioLogado) {
            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(mFirebaseUser.getUid()).child("listasSalvas");
            montaDrawer();
            montaBotaoFlutuante();
            colocaDadosUsarioNaView();
        } else {
            //usuario nao logado, manda para a tela de login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
    }

    private void colocaDadosUsarioNaView() {
        nomeDoUsuario = mFirebaseUser.getDisplayName();
        TextView txtViewNomeUsuario = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtViewNomeUsuario);
        txtViewNomeUsuario.setText(nomeDoUsuario);

        String email = mFirebaseUser.getEmail();
        TextView txtViewEmailUsuario = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtViewEmailUsuario);
        txtViewEmailUsuario.setText(email);

        ImageView imageViewUsuario = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageViewUsuario);
        String urlFotoUsuario;
        if (mFirebaseUser.getPhotoUrl() != null) {
            urlFotoUsuario = mFirebaseUser.getPhotoUrl().toString();
            Picasso.with(this).load(urlFotoUsuario).into(imageViewUsuario);
        }
    }

    private void montaDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        displaySelectedScreen(R.id.nav_cria_lista);
    }

    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    private void montaBotaoFlutuante() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Enviar solicitação de orçamento")
                        .setMessage("Você realmente deseja enviar esta solicitação de orçamento?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                enviaEmail(view);
                            }})
                        .setNegativeButton("Não", null).show();
            }

            private void enviaEmail(final View view) {
                final List<ItemLista> lista;
                lista = CriarListaRecyclerAdapter.lista;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            GMailSenderConstruFacil sender = new GMailSenderConstruFacil(lista, MainActivity.this);
                            sender.sendMail();
                            Snackbar.make(view, "Pedido enviado às lojas!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();

                            //ARRUMAR O TIMESTAMP DEPOIS
                            Lista listaNova = new Lista(getCurrentTimeStamp(), lista);
                            mFirebaseDatabaseReference.push().setValue(listaNova);

                            //LIMPA O CRIAR LISTA
                            mFirebaseDatabaseReferenceListaNova.child(mFirebaseUser.getUid()).child("listaNova").removeValue();

                        } catch (Exception e) {
                            Log.e("SendMail", e.getMessage(), e);
                        }
                    }
                }).start();
            }
        });
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //calling the method displayselectedscreen and passing the id of selected menu
        displaySelectedScreen(item.getItemId());
        //make this method blank
        return true;
    }

    private void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_logout:
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Você realmente deseja realizar logout?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                deslogar();
                            }})
                        .setNegativeButton("Não", null).show();
                break;
            case R.id.nav_cria_lista:
                findViewById(R.id.fab).setVisibility(View.VISIBLE);
                fragment = new CriarListaFragment();
                break;
            case R.id.nav_listas:
                findViewById(R.id.fab).setVisibility(View.INVISIBLE);
                fragment = new VerListasFragment();
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void deslogar() {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        mFirebaseUser = null;
        nomeDoUsuario = ANONYMOUS;
        //mPhotoUrl = null;
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

}
