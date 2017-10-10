package util;

import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    private static final String API_SEND_MESSAGE_URL = "https://graph.facebook.com/v2.6/me/messages?access_token=";

    public static void sendMessageToClient(String recipientId, String message, String accessToken){
        Future<HttpResponse<JsonNode>> future = Unirest.post(API_SEND_MESSAGE_URL+ accessToken)
                .header("Content-Type", "application/json")
                .body("{\"recipient\": {\n" +
                        "    \"id\": \""+recipientId+"\"\n" +
                        "  },\n" +
                        "  \"message\": {\n" +
                        "    \"text\": \""+message+"\"\n" +
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

                        log.info("Respond: {}",body.toString());
                    }

                    public void cancelled() {
                        System.out.println("The request has been cancelled");
                    }

                });
    }
}
