package com.example.instagram.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.example.instagram.model.Postagem;
import com.example.instagram.model.Usuario;

import de.hdodenhof.circleimageview.CircleImageView;

public class VisualizarPostagemActivity extends AppCompatActivity {

    private CircleImageView imgPerfil;
    private ImageView imgPostagem;
    private TextView txtNomePerfil, txtCurtidas, txtDescricao;

    private Usuario usuario;
    private Postagem postagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_postagem);

        //Configurar a toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Vizualizar Postagem");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_fechar_preto);

        //Inicializar Componentes
        imgPerfil = findViewById(R.id.imgPerfil);
        txtNomePerfil = findViewById(R.id.txtNomePerfil);
        txtCurtidas = findViewById(R.id.txtCurtidas);
        txtDescricao = findViewById(R.id.txtDescricao);
        imgPostagem = findViewById(R.id.imgPostagem);

        //Recuperar dados do usuário e postagem
        Bundle bundle = getIntent().getExtras();
        usuario = (Usuario) bundle.getSerializable("usuarioSelecionado");
        postagem = (Postagem) bundle.getSerializable("postagem");

        //Recuperar dados usuario
        String caminhoFoto = usuario.getFoto();
        if(caminhoFoto != null){
            Uri url = Uri.parse(caminhoFoto);
            Glide.with(VisualizarPostagemActivity.this).load(url).into(imgPerfil);
        }
        txtNomePerfil.setText(usuario.getNome());

        //Recuperar dados postagem
        String caminhoFotoPostagem = postagem.getCaminhoFoto();
        if(caminhoFotoPostagem != null){
            Uri url = Uri.parse(caminhoFotoPostagem);
            Glide.with(VisualizarPostagemActivity.this).load(url).into(imgPostagem);
        }
        txtDescricao.setText(postagem.getDescricao());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}