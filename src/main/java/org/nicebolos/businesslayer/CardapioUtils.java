package org.nicebolos.businesslayer;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

@Component
public class CardapioUtils {
    JSONObject cardapio;
    public CardapioUtils(){
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            FileReader reader = new FileReader(classloader.getResource("cardapio.json").getFile());
            cardapio = new JSONObject(reader.readAllAsString());
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    /**
     * Verifica se o pedido é um JSON contendo apenas itens do cardápio e suas respectivas quantidades
     * EX:{"Bolos":{"Bolo de chocolate":1},"Salgados":{"Coxinha",2}}
     * @param pedido JSON para validar
     * @return Se o pedido é valido ou não
     */
    public boolean validarPedido(String pedido){
        try {
            JSONObject pedidoJSON = new JSONObject(pedido);
            Set<String> tiposSet = pedidoJSON.keySet();
            JSONObject cardapioObject = cardapio.getJSONObject("Cardapio");
            for (String tipo : tiposSet) {
                JSONObject cardapioTipos = cardapioObject.getJSONObject(tipo);
                for (String key : pedidoJSON.getJSONObject(tipo).keySet()) {
                    if(!cardapioTipos.has(key)) return false;
                }
            }
            return true;
        }catch (JSONException e){
            return false;
        }
    }

    /**
     * Obtem uma mensagem pronta para enviar a partir de um JSON de pedido
     * (use o metodo {@link #validarPedido(String)} antes desse, ou pode dar erro.
     * @param pedido Json para gerar a mensagem
     * @return Mensagem
     */
    public String getMensagem(String pedido){
        JSONObject pedidoJSON = new JSONObject(pedido);
        StringBuilder msg = new StringBuilder("Pedido:\n");
        Iterator<String> tipos = pedidoJSON.keys();
        while(tipos.hasNext()) {
            String tipo = tipos.next();
            JSONObject tipoObject = pedidoJSON.getJSONObject(tipo);
            msg.append(tipo);msg.append("\n");
            Iterator<String> keys = pedidoJSON.getJSONObject(tipo).keys();
            for (int i = 1; keys.hasNext(); i++) {
                String key = keys.next();
                msg.append("[%d] %s:%d unidade(s)\n".formatted(i, key, tipoObject.getInt(key)));
            }
        }
        msg.append("*Mensagem gerada automaticamente.");
        return msg.toString();
    }
}
