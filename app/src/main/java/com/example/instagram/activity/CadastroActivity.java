package com.example.instagram.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.instagram.R;
import com.example.instagram.config.ConfiguracaoFirebase;
import com.example.instagram.helper.Base64Custom;
import com.example.instagram.helper.UsuarioFirebase;
import com.example.instagram.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private EditText txtNome, txtEmail, txtSenha;
    private ProgressBar progressBar;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        //Configuracoes iniciais
        inicializarComponentes();

    }

    public void inicializarComponentes(){
        txtNome = findViewById(R.id.txtNome);
        txtEmail = findViewById(R.id.txtEmailCadastro);
        txtSenha = findViewById(R.id.txtSenhaCadastro);
        progressBar = findViewById(R.id.progressBarCadastro);
    }

    public void validarCampos(View view){
        String nome = txtNome.getText().toString();
        String email = txtEmail.getText().toString();
        String senha = txtSenha.getText().toString();
        if(!nome.isEmpty() || !email.isEmpty() || !senha.isEmpty()){
            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setEmail(email);
            usuario.setSenha(senha);
            cadastrar(usuario);
        }else{
            Toast.makeText(
                    getApplicationContext(),
                    "Preencha os campos",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void cadastrar(final Usuario usuario){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    try{
                        String id = Base64Custom.codificarBase64(usuario.getEmail());
                        usuario.setId(id);
                        usuario.salvar();

                        //Salvar dados no profile do firebase
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                        Toast.makeText(
                                CadastroActivity.this,
                                "Sucesso ao cadastrar usuário",
                                Toast.LENGTH_SHORT
                        ).show();
                        progressBar.setVisibility(View.VISIBLE);

                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    String excecao = "";
                    try {
                        throw task.getException();
                    }catch ( FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!";
                    }catch ( FirebaseAuthInvalidCredentialsException e){
                        excecao= "Por favor, digite um e-mail válido";
                    }catch ( FirebaseAuthUserCollisionException e){
                        excecao = "Este conta já foi cadastrada";
                    }catch (Exception e){
                        excecao = "Erro ao cadastrar usuário: "  + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this,
                            excecao,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
