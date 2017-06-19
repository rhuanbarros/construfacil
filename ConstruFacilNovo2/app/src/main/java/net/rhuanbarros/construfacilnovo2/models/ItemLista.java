package net.rhuanbarros.construfacilnovo2.models;

/**
 * Created by rhuanbarros on 29/03/2017.
 */

public class ItemLista {
    private Long id;
    private String descricao;
    private Long quantidade;

    public ItemLista() {
    }

    public ItemLista(Long id, String descricao, Long quantidade) {
        this.id = id;
        this.descricao = descricao;
        this.quantidade = quantidade;
    }

    public ItemLista(Long id, String descricao) {
        this.id = id;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }
}
