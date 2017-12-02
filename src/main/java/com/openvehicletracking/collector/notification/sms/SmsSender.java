package com.openvehicletracking.collector.notification.sms;

import com.openvehicletracking.collector.notification.AbstractSender;
import com.openvehicletracking.collector.notification.NotificationMessage;
import com.openvehicletracking.collector.notification.SendResult;
import com.openvehicletracking.collector.verticle.SmsVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

/**
 * Created By oksuz 02/12/2017
 *
 * this class implements following api
 * https://www.iletimerkezi.com/sms-api
 *
 */
public class SmsSender extends AbstractSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsVerticle.class);
    private JsonObject config;

    public SmsSender(JsonObject config, WebClient client) {
        super(config, client);
        this.config = super.getConfig();
        createPrivateKey(this.config);
    }

    private void createPrivateKey(JsonObject config) {
        try {
            SecretKey secretKey = new SecretKeySpec(config.getString("apiSecret").getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(config.getString("apiKey").getBytes());
            config.put("privateKey", DatatypeConverter.printHexBinary(hash).toLowerCase());
        } catch (Exception e) {
            LOGGER.error("error on createRequestHash", e);
            throw new RuntimeException(e);
        }
    }

    private String getRequestXML(String gsm, String text) {
        return "<request>" +
                "    <authentication>" +
                "        <key>"+ getConfig().getString("apiKey") +"</key>" +
                "        <hash>"+ getConfig().getString("privateKey") +"</hash>" +
                "    </authentication>" +
                "    <order>" +
                "        <sender>"+ getConfig().getString("sender") +"</sender>" +
                "        <sendDateTime></sendDateTime>" +
                "        <message>" +
                "            <text><![CDATA["+ text +"]]></text>" +
                "            <receipents>" +
                "                <number>"+ gsm +"</number>" +
                "            </receipents>" +
                "        </message>" +
                "    </order>" +
                "</request>";
    }

    @Override
    public JsonObject getConfig() {
        return config;
    }

    @Override
    public void send(NotificationMessage message, Handler<SendResult> handler) {
        String requestXML = getRequestXML(message.getRecipient().getGsmNumber(), message.getBody());
        Buffer body = Buffer.buffer(requestXML);
        getClient().postAbs(getConfig().getString("apiUrl"))
                .followRedirects(true)
                .putHeader("Content-Type", "text/xml")
                .timeout(1000 * 120)
                .sendBuffer(body, response -> {
                    SmsSendResult result;
                    if (response.succeeded()) {
                        result = SmsSendResult.Succeeded(parseXMLResponse(response.result().bodyAsString()));
                    } else {
                        result = SmsSendResult.Failed(parseXMLResponse(response.result().bodyAsString()), response.cause());
                    }

                    handler.handle(result);
                });
    }

    public String parseXMLResponse(String xmlResponse) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        Document document;
        InputSource is = new InputSource(new StringReader(xmlResponse));
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(is);
        } catch (Exception e) {
            LOGGER.error("error when parsing xml", e);
            return escapeHtml(xmlResponse);
        }

        Node responseNode = null;
        if (document.getElementsByTagName("response").getLength() > 0) {
            responseNode = document.getElementsByTagName("response").item(0);
        }

        if (responseNode != null && responseNode.getChildNodes().getLength() > 0 && responseNode.getChildNodes().item(1).getChildNodes().getLength() > 0) {
            NodeList codeAndStatusNode = responseNode.getChildNodes().item(1).getChildNodes();
            return codeAndStatusNode.item(1).getTextContent() + " - " + codeAndStatusNode.item(3).getTextContent();
        }

        return escapeHtml(xmlResponse);
    }

    private String escapeHtml(String str) {
        return str.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\\n", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\"", "&quot;");
    }
}
