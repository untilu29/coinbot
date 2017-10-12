package util;

import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    private static final String API_SEND_MESSAGE_URL = "https://graph.facebook.com/v2.6/me/messages?access_token=";
    private static final String ROOT_DOMAIN_WEB_HOOK = "https://www.vuoncaumotel.com";
    public static String WEB_TOKEN = "";

    public static void sendMessageToClient(String recipientId, String message, String accessToken) {
        Future<HttpResponse<JsonNode>> future = Unirest.post(API_SEND_MESSAGE_URL + accessToken)
                .header("Content-Type", "application/json")
                .body("{\"recipient\": {\n" +
                        "    \"id\": \"" + recipientId + "\"\n" +
                        "  },\n" +
                        "  \"message\": {\n" +
                        "    \"text\": \"" + message + "\"\n" +
                        "  }}")
                .asJsonAsync(new Callback<JsonNode>() {

                    public void failed(UnirestException e) {
                        System.out.println("The request has failed");
                    }

                    public void completed(HttpResponse<JsonNode> response) {
                        int code = response.getStatus();
                        Headers headers = response.getHeaders();
                        JsonNode body = response.getBody();
                        InputStream rawBody = response.getRawBody();

                        log.info("Respond: {}", body.toString());
                    }

                    public void cancelled() {
                        System.out.println("Send message has been cancelled");
                    }

                });
    }

    public static void getTokenFromFile() throws UnirestException {
        HttpResponse<String> tokenRes = Unirest.get(ROOT_DOMAIN_WEB_HOOK + "/token.txt")
                .asString();
        WEB_TOKEN = tokenRes.getBody();
    }

    public static List<String> getSubscriberList() throws UnirestException {
        HttpResponse<String> tokenRes = Unirest.get(ROOT_DOMAIN_WEB_HOOK + "/subscriber.txt")
                .asString();
        return Arrays.asList(tokenRes.getBody().split("\n"));
    }

    public static void main(String[] args) throws UnirestException {
        getTokenFromFile();
        for (String receiverId:HttpUtil.getSubscriberList()) {
            System.out.println(receiverId.trim());
            HttpUtil.sendMessageToClient(receiverId.trim(),"Giá trị Việt Nam: ",HttpUtil.WEB_TOKEN);
        }
    }
}
