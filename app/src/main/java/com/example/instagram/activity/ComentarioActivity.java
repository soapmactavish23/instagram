package com.example.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.instagram.R;
import com.example.instagram.adapter.AdapterComentario;
import com.example.instagram.config.ConfiguracaoFirebase;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Comentario;
import com.example.instagram.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ComentarioActivity extends AppCompatActivity {

    private EditText editComentario;
    private String idPostagem;
    private Usuario usuarioLogado;
    private RecyclerView recyclerComentarios;
    private AdapterComentario adapterComentario;
    private List<Comentario> listaComentarios = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private DatabaseReference comentariosRef;
    private ValueEventListener valueEventListenerComentarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentario);

        //Configurar a toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Comentários");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_fechar_preto);

        //Configuracoes Iniciais
        editComentario = findViewById(R.id.editComentario);
        usuarioLogado = UsuarioFirebase.getUsuarioLogado();
        recyclerComentarios = findViewById(R.id.recyclerComentarios);
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        //Configurar recyclerView
        adapterComentario = new AdapterComentario(listaComentarios, getApplicationContext());
        recyclerComentarios.setHasFixedSize(true);
        recyclerComentarios.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerComentarios.setAdapter(adapterComentario);

        //Recuperar id da postagem
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            idPostagem = bundle.getString("idPostagem");
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    public void salvarComentario(View view){
        String txtComentario = editComentario.getText().toString();
        if(txtComentario != null && !txtComentario.equals("")){

            Comentario comentario = new Comentario();
            comentario.setIdPostagem(idPostagem);
            comentario.setIdUsuario(usuarioLogado.getId());
            comentario.setNomeUsuario(usuarioLogado.getNome());
            comentario.setFoto(usuarioLogado.getFoto());
            comentario.setComentario(txtComentario);
            if(comentario.salvar()){
                Toast.makeText(
                        getApplicationContext(),
                        "Sucesso ao Salvar",
                        Toast.LENGTH_SHORT
                ).show();
                //Limpar o comentário digitado
                editComentario.setText("");
                adapterComentario.notifyDataSetChanged();
            }else{
                Toast.makeText(
                        getApplicationContext(),
                        "Erro ao Salvar",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }else{
            Toast.makeText(
                    getApplicationContext(),
                    "Preencha o campo do comentário",
                    Toast.LENGTH_SHORT
            ).show();

            //Limpar o comentário digitado
            editComentario.setText("");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        listarComentarios();
    }

    @Override
    protected void onStop() {
        super.onStop();
        comentariosRef.removeEventListener(valueEventListenerComentarios);
    }

    private void listarComentarios(){
        comentariosRef = firebaseRef.child("comentarios").child(idPostagem);
        valueEventListenerComentarios = comentariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaComentarios.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    listaComentarios.add(ds.getValue(Comentario.class));
                }
                adapterComentario.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
