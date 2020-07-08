package com.example.instagram.model;

import com.example.instagram.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

public class Comentario {

    private String idComentario;
    private String idPostagem;
    private String idUsuario;
    private String foto;
    private String nomeUsuario;
    private String comentario;

    public Comentario() {
    }

    public boolean salvar(){

        DatabaseReference comentarioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("comentarios")
                .child(getIdPostagem());

        String chaveComentario = comentarioRef.push().getKey();
        setIdComentario(chaveComentario);
        comentarioRef.child(getIdComentario()).setValue(this);

        return true;
    }

    public String getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(String idComentario) {
        this.idComentario = idComentario;
    }

    public String getIdPostagem() {
        return idPostagem;
    }

    public void setIdPostagem(String idPostagem) {
        this.idPostagem = idPostagem;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
