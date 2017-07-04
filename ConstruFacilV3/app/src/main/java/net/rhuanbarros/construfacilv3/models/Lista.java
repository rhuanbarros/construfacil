package net.rhuanbarros.construfacilv3.models;

import java.util.List;

/**
 * Created by rhuanbarros on 02/07/2017.
 */

public class Lista {
    private String timestamp;
    private List<ItemLista> lista;

    public Lista() {
    }

    public Lista(String timestamp, List<ItemLista> lista) {
        this.timestamp = timestamp;
        this.lista = lista;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<ItemLista> getLista() {
        return lista;
    }

    public void setLista(List<ItemLista> lista) {
        this.lista = lista;
    }
}
