package com.example.instagram.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagram.R;
import com.example.instagram.config.ConfiguracaoFirebase;
import com.example.instagram.helper.Base64Custom;
import com.example.instagram.helper.Permissao;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditarPerfilActivity extends AppCompatActivity {

    private TextInputEditText txtNome, txtEmail;
    private CircleImageView imgEditarPerfil;
    private Usuario usuarioLogado;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private String idUsuario;
    private FirebaseUser usuarioPerfil;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);
        configurarToolbar();

        //Validar permissoes
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        //Configuracoes Iniciais
        txtNome = findViewById(R.id.editNome);
        txtEmail = findViewById(R.id.editEmail);
        imgEditarPerfil = findViewById(R.id.imgFotoPerfil);

        //Firebase
        configuracaoFirebase();

        //Carregar dados do usuario
        txtNome.setText(usuarioPerfil.getDisplayName());
        txtEmail.setText(usuarioPerfil.getEmail());
        Uri url = usuarioPerfil.getPhotoUrl();
        if(url != null){
            Glide.with(EditarPerfilActivity.this).load(url).into(imgEditarPerfil);
        }else{
            imgEditarPerfil.setImageResource(R.drawable.avatar);
        }

    }

    private void configurarToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Editar Perfil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_fechar_preto);
    }

    private void configuracaoFirebase(){
        //Recuperar dados do usuario
        usuarioLogado = UsuarioFirebase.getUsuarioLogado();
        usuarioPerfil = UsuarioFirebase.getUsuarioAtual();
        storageReference = ConfiguracaoFirebase.getStorageReference();
        idUsuario = UsuarioFirebase.getIdentificadorUsuario();
        usuarioLogado.setId(idUsuario);
    }

    public void atualizarNome(View view){
        String nome = txtNome.getText().toString();
        if(!nome.isEmpty()){
            //Atualizar o nome no perfil
            UsuarioFirebase.atualizarNomeUsuario(nome);

            //Atualizar o nome no banco de dados
            usuarioLogado.setNome(nome);
            usuarioLogado.atualizar();

            Toast.makeText(
                    getApplicationContext(),
                    "Nome de usuário atualizado com sucesso!",
                    Toast.LENGTH_SHORT
            ).show();
        }else{
            Toast.makeText(
                    getApplicationContext(),
                    "Preencha os Campos",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public void alterarFoto(View view){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if( i.resolveActivity(getPackageManager()) != null){
            startActivityForResult(i, SELECAO_GALERIA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Bitmap imagem = null;
            try{
                //Selecao apenas da galeria
                switch (requestCode){
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }

                //Caso tenha sido escolhido uma imagem
                if(imagem != null){
                    //Configura imagem na tela
                    imgEditarPerfil.setImageBitmap(imagem);

                    //Recuperar dados da imagem para o Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Salvar imagem no firebase
                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("perfil")
                            .child(idUsuario + ".jpeg");
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Erro ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Sucesso ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT
                            ).show();
                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uri.isComplete());
                            Uri url = uri.getResult();
                            atualizaFotoUsuario(url);
                        }
                    });

                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void atualizaFotoUsuario(Uri url){
        boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);
        if(retorno){
            usuarioLogado.setFoto(url.toString());
            usuarioLogado.atualizar();
            Toast.makeText(
                    getApplicationContext(),
                    "Sua foto foi atualizada",
                    Toast.LENGTH_SHORT
            ).show();
        }

    }

}
