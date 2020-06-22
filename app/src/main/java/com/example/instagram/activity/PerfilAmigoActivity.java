package com.example.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.example.instagram.adapter.AdapterGrid;
import com.example.instagram.config.ConfiguracaoFirebase;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Postagem;
import com.example.instagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {

    private CircleImageView imgPerfilAmigo;
    private TextView txtPublicacoesAmigo, txtSeguidoresAmigo, txtSeguindoAmigo;
    private GridView gridViewPerfil;
    private AdapterGrid adapterGrid;
    private Button btnSeguir;
    private Usuario usuarioPerfil;
    private Usuario usuarioLogado;

    private DatabaseReference firebaseRef;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuariosLogadoRef;
    private DatabaseReference seguidoresRef;
    private DatabaseReference postagensUsuarioRef;

    private String idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);

        //Configuracoes iniciais
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        usuariosRef = firebaseRef.child("usuarios");
        seguidoresRef = firebaseRef.child("seguidores");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();

        //Inicializar Componentes
        inicializarComponentes();

        //Configurar toolbar
        configurarToolBar();

        //Abre a foto clicada
        gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Postagem postagem =

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarDadosUsuarioLogado();
        recuperarDadosUsuario();
    }

    private void inicializarComponentes(){
        //Link com a interface
        imgPerfilAmigo = findViewById(R.id.imgPerfilAmigo);
        txtPublicacoesAmigo = findViewById(R.id.txtPublicacoesAmigo);
        txtSeguidoresAmigo = findViewById(R.id.txtSeguidoresAmigo);
        txtSeguindoAmigo = findViewById(R.id.txtSeguindoAmigo);
        btnSeguir = findViewById(R.id.btnSeguir);
        btnSeguir.setText("Carregando...");
        gridViewPerfil = findViewById(R.id.gridViewPerfil);

        //Recuperar dados do usuário
        Bundle bundle = getIntent().getExtras();
        usuarioPerfil = (Usuario) bundle.getSerializable("usuarioPerfil");

        //Configurar referencia postagens usuario
        postagensUsuarioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("postagens")
                .child(usuarioPerfil.getId());

        inicializarImageLoader();

        //Carregar as fotos de postagem
        carregarFotosPostagem();

    }

    private void configurarToolBar(){
        //Configuracoes da Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(usuarioPerfil.getNome());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void inicializarImageLoader(){
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(config);
    }

    public void carregarFotosPostagem(){
        //Configurar o tamanho do grid
        int tamanhoGrid = getResources().getDisplayMetrics().widthPixels;
        int tamanhoImagem = tamanhoGrid / 3;
        gridViewPerfil.setColumnWidth(tamanhoImagem);

        //Recuperar as fotos postadas pelo usuário
        postagensUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> urlFotos = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Postagem postagem = ds.getValue(Postagem.class);
                    urlFotos.add(postagem.getCaminhoFoto());
                }

                //Configurar adapter
                adapterGrid = new AdapterGrid(getApplicationContext(), R.layout.grid_postagem, urlFotos);
                gridViewPerfil.setAdapter(adapterGrid);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void recuperarDadosUsuarioLogado(){
        usuariosLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuariosLogadoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usuarioLogado = dataSnapshot.getValue(Usuario.class);
                //Ver se o usuario está seguindo
                verificaSegueUsuarioAmigo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void recuperarDadosUsuario(){
        usuariosRef.child(usuarioPerfil.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);

                //Recuperar dados
                String postagens = String.valueOf(usuario.getPostagens());
                String seguindo = String.valueOf(usuario.getSeguindo());
                String seguidores = String.valueOf(usuario.getSeguidores());

                //Passar dados
                txtPublicacoesAmigo.setText(postagens);
                txtSeguindoAmigo.setText(seguindo);
                txtSeguidoresAmigo.setText(seguidores);

                //Foto
                String foto = usuario.getFoto();
                if(foto != null){
                    Glide.with(PerfilAmigoActivity.this).load(foto).into(imgPerfilAmigo);
                }else{
                    imgPerfilAmigo.setImageResource(R.drawable.avatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void verificaSegueUsuarioAmigo(){
        DatabaseReference seguidorRef = seguidoresRef.child(idUsuarioLogado).child(usuarioPerfil.getId());
        seguidorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //Já está sendo seguindo
                    habilitarBotaoSeguir(true);
                }else{
                    //Ainda não está seguindo
                    habilitarBotaoSeguir(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void habilitarBotaoSeguir(Boolean segueUsuario){
        if(segueUsuario){
            btnSeguir.setText("Seguindo");
            btnSeguir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deixarDeSeguir(usuarioLogado, usuarioPerfil);
                }
            });
        }else{
            btnSeguir.setText("Seguir");
            btnSeguir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Salvar seguidor
                    salvarSeguidor(usuarioLogado, usuarioPerfil);
                }
            });
        }
    }

    private void salvarSeguidor(Usuario uLogado, Usuario uAmigo){
        HashMap<String, Object> dadosAmigo = new HashMap<>();
        dadosAmigo.put("nome", uAmigo.getNome());
        dadosAmigo.put("foto", uAmigo.getFoto());

        DatabaseReference seguidorRef = seguidoresRef
                .child(uLogado.getId())
                .child(uAmigo.getId());
        seguidorRef.setValue(dadosAmigo);

        //Alterar bota ocao para seguindo
        btnSeguir.setText("Seguindo");
        btnSeguir.setOnClickListener(null);

        //Incrementar seguindo do usuário logado
        int seguindo = uLogado.getSeguindo() + 1;
        HashMap<String, Object> dadosSeguindo = new HashMap<>();
        dadosSeguindo.put("seguindo", seguindo);
        DatabaseReference usuarioSeguindo = usuariosRef.child(uLogado.getId());
        usuarioSeguindo.updateChildren(dadosSeguindo);

        //Inccrementar seguidores do amigo
        int seguidores = uAmigo.getSeguidores() + 1;
        HashMap<String, Object> dadosSeguidores = new HashMap<>();
        dadosSeguidores.put("seguidores", seguidores);
        DatabaseReference usuarioSeguidores = usuariosRef.child(uAmigo.getId());
        usuarioSeguidores.updateChildren(dadosSeguidores);
    }

    private void deixarDeSeguir(Usuario uLogado, Usuario uAmigo){
        finish();
    }

}