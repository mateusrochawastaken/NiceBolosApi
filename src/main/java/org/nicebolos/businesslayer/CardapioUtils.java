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
    JSONObject raiz;
    public CardapioUtils(){
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            FileReader reader = new FileReader(classloader.getResource("cardapio.json").getFile());
            raiz = new JSONObject(reader.readAllAsString());
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    /**
     * Verifica se o pedido é um JSON contendo apenas itens do cardápio e suas respectivas quantidades
     * EX:{"Bolo de chocolate":1,"Coxinha",2}
     * @param pedido JSON para validar
     * @return Se o pedido é valido ou não
     */
    public boolean validarPedido(String pedido){
        try {
            JSONObject pedidoJSON = new JSONObject(pedido);
            Set<String> keySet = pedidoJSON.keySet();
            for (String key : keySet) {
                if (!raiz.has(key)) return false;
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
        Iterator<String> keys = pedidoJSON.keys();
        for(int i = 1; keys.hasNext(); i++){
            String key = keys.next();
            msg.append("[%d] %s:%d unidade(s)\n".formatted(i,key,pedidoJSON.getInt(key)));
        }
        msg.append("*Mensagem gerada automaticamente.");
        return msg.toString();
    }
}
