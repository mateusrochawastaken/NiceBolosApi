package org.nicebolos;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nicebolos.businesslayer.CardapioUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;

@RestController
public class ApiController {

    @Autowired
    CardapioUtils cardapioUtils;

    Logger logger;
    Document nicexml;

    public ApiController(){
        logger = LogManager.getLogger();
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = df.newDocumentBuilder();
            nicexml = db.parse(classloader.getResource("nicebolos.xml").toString());
        }catch (ParserConfigurationException | IOException | SAXException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Endpoint que envia o pedido para um whatsapp, por enquanto ele valida o pedido e
     * o telefone antes de mandar a mensagem, porém essa segunda validação vai ser trocada
     * pelo futuro sistema de verificação por códigos, que provavelmente vai dar uma chave
     * de autenticação que permite ao usuário usar esse endpoint.
     * @param telefone telefone para enviar
     * @param body JSON com o pedido
     * @return ResponseEntity com o resultado da operação, por enquanto
     * os únicos erros retornados são se o telefone for inválido se o pedido for inválido,
     * ambos HTTP 400.
     */
    @PostMapping("/enviar-comanda")
    public ResponseEntity<String> enviarComanda(@RequestParam(name = "telefone") String telefone, @RequestBody String body){
        String key = nicexml.getElementsByTagName("WahaKey").item(0).getTextContent();
        String porta = nicexml.getElementsByTagName("WahaPorta").item(0).getTextContent();
        String session = nicexml.getElementsByTagName("WahaSession").item(0).getTextContent();
        if (cardapioUtils.validarPedido(body)) {
            String msg = cardapioUtils.getMensagem(body);
            logger.trace(msg);
            JSONObject responseBody = new JSONObject();
            HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
            RestClient client = RestClient.builder().requestFactory(new JdkClientHttpRequestFactory(httpClient)).build();
            String existsBody;
            try {
                System.out.printf("http://localhost:%s/api/contacts/check-exists?phone=%s&session=%s%n", porta,telefone,session);
                existsBody = client.get().uri("http://localhost:%s/api/contacts/check-exists?phone=%s&session=%s".formatted(porta,telefone,session))
                        .header("X-Api-Key", key)
                        .retrieve().body(String.class);
            }catch (RestClientResponseException e){
                existsBody = "{\"numberExists\":false}";
            }
            JSONObject existsJSON = new JSONObject(existsBody);
            if(existsJSON.getBoolean("numberExists")) { //Essa parte é temporária, vai ser substituida pelo sistema de verificação
                RestClient.RequestBodySpec wahaBodySpec = client.post().uri("http://localhost:" + porta + "/api/sendText")
                        .header("X-Api-Key", key).contentType(MediaType.APPLICATION_JSON);
                responseBody.put("session", session);
                responseBody.put("chatId", existsJSON.getString("chatId"));
                responseBody.put("text", msg);
                RestClient.ResponseSpec responseSpec = wahaBodySpec.body(responseBody.toString()).retrieve();
                logger.trace("Status code da coneção com a WAHA:" + responseSpec.toBodilessEntity().getStatusCode());
                return ResponseEntity.ok().header("Access-Control-Allow-Origin","*").build();
            }else{
                logger.trace("O Número \"" + telefone + "\" não existe.");
                return ResponseEntity.badRequest().header("Access-Control-Allow-Origin","*")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"codigoErro\":1,\"mensagem\":\"O Número " + telefone + " não existe no WhatsApp.\"}");
            }
        } else {
            logger.trace("Pedido inválido, body:\n" + body);
            return ResponseEntity.status(400).header("Access-Control-Allow-Origin","*")
                    .body("{\"codigoErro\":2,\"mensagem\":\"Erro!Pedido inválido!\"}");
        }
    }

    /**
     * Lê e retorna o conteúdo do arquivo cardapio.json, que deve ser um resource acessível pelo classloader.
     */
    @GetMapping("/obter-cardapio")
    public ResponseEntity<String> obterCardapio(){
        try {
            String cardapio;
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            FileReader reader = new FileReader(classloader.getResource("cardapio.json").getFile());
            cardapio = reader.readAllAsString();
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin","*").body(cardapio);
        }catch (Exception e){
            logger.error(e);
            return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON)
                    .body("{\"mensagem\":\"Erro ao obter cardápio, tente mais tarde ou contate a administração do servidor\"");
        }
    }
}
