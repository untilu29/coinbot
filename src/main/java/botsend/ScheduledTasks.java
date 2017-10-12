package botsend;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.exceptions.UnirestException;
import dto.Coin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import util.HttpUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final SimpleDateFormat dateFormat = new  SimpleDateFormat("HH:mm:ss");
    private static final String BLOCK_CHAIN_INFO_URL = "https://blockchain.info/markets/data/tickers.json";
    private static final String COIN_DESK_URL = "https://api.coindesk.com/v1/bpi/currentprice/VND.json";
    private static final int TIME_REFRESH_VALUE = 5000;
    private static final int THRESHOLD_VALUE_USD = 1;
    private static boolean firstTime = true;

    private static Coin localbitcoins = new Coin();
    private static Coin bitfinex = new Coin();
    private static Coin kraken = new Coin();
    private static Coin bitstamp = new Coin();
    private static Coin btce = new Coin();
    private static Coin coindesk = new Coin();
    private static Coin vnd = new Coin();
    private static String messageReceived;

    private JsonObject getJsonObjectFromUrl(String url) throws IOException {
        InputStreamReader reader = new InputStreamReader(new URL(url).openStream());
        JsonElement jsonElement = new Gson().fromJson(reader,JsonElement.class);
        return jsonElement.getAsJsonObject();
    }

    private JsonObject getExchangeValueFromCoinSource(String url, String exchangeName) throws IOException {
        return getJsonObjectFromUrl(url).get(exchangeName).getAsJsonObject();
    }

    @Scheduled(fixedRate = TIME_REFRESH_VALUE)
    public void reportCurrentTime() throws IOException, CloneNotSupportedException, UnirestException {
//        log.info("The time is now {}", dateFormat.format(new Date()));
        messageReceived = "";
        localbitcoins = getCoinValueBlockChainInfo("localbitcoins",localbitcoins);
        bitfinex = getCoinValueBlockChainInfo("bitfinex",bitfinex);
        kraken = getCoinValueBlockChainInfo("kraken",kraken);
        bitstamp = getCoinValueBlockChainInfo("bitstamp",bitstamp);
        btce = getCoinValueBlockChainInfo("btce",btce);
        coindesk = getCoinValueCoinDesk("USD",coindesk);
        vnd = getCoinValueCoinDeskVND(vnd);

        if (!messageReceived.trim().isEmpty()&& !firstTime)
        {
            firstTime = false;
            for (String receiverId:HttpUtil.getSubscriberList()) {
                HttpUtil.sendMessageToClient(receiverId.trim(),messageReceived+"Giá trị Việt Nam: "+vnd.getRate() + " VNĐ",HttpUtil.WEB_TOKEN);
            }
        }
    }

    private Coin getCoinValueBlockChainInfo(String exchangeName, Coin compareCoin) throws IOException {
        Coin coin = new Coin();
        coin.setName(exchangeName);
        coin.setCurrency("BTC_USD");
        coin.setPrice(getExchangeValueFromCoinSource(BLOCK_CHAIN_INFO_URL,exchangeName).get(coin.getCurrency())
                .getAsJsonObject().get("p").getAsDouble());
        coin.setDiff(getExchangeValueFromCoinSource(BLOCK_CHAIN_INFO_URL,exchangeName).get(coin.getCurrency())
                .getAsJsonObject().get("d").getAsDouble());

        if (coin.compareTo(compareCoin)!=0||compareCoin==null) {
            coin.setDiff((compareCoin.getPrice()!=null?compareCoin.getPrice():0)-coin.getPrice());
            log.info(StringUtils.capitalize(exchangeName)+": {}", coin.getPrice()+", "+(coin.getDiff()>0 ? "GIẢM "+
                    String.format("%.2f", (coin.getDiff()))
                    : "TĂNG "+ String.format("%.2f", Math.abs(Math.abs(coin.getDiff())))));
            if (Math.abs(coin.getDiff())> THRESHOLD_VALUE_USD)
                messageReceived += StringUtils.capitalize(exchangeName)+": "+ coin.getPrice()+", "+(coin.getDiff()>0 ? "GIẢM "+
                        String.format("%.2f", (coin.getDiff()))
                        : "TĂNG "+ String.format("%.2f", Math.abs(Math.abs(coin.getDiff())))) + "\n";
        }



        return coin;
    }

    private Coin getCoinValueCoinDesk(String currencyName, Coin compareCoin) throws IOException{
        Coin coin = new Coin();
        coin.setCurrency(currencyName);
        coin.setPrice(getExchangeValueFromCoinSource(COIN_DESK_URL,"bpi").getAsJsonObject()
                .get(coin.getCurrency()).getAsJsonObject().get("rate_float").getAsDouble());

        if (coin.compareTo(compareCoin)!=0||compareCoin==null) {
            coin.setDiff((compareCoin.getPrice()!=null?compareCoin.getPrice():0)-coin.getPrice());
            log.info("Coindesk: {}", coin.getPrice()+", "+(coin.getDiff()>0 ? "GIẢM "+
                    String.format("%.2f", (coin.getDiff()))
                    : "TĂNG "+ String.format("%.2f", Math.abs(Math.abs(coin.getDiff())))));

//            log.info("Giá trị VNĐ: {}",vnd.getRate());
            if (Math.abs(coin.getDiff())>THRESHOLD_VALUE_USD)
                messageReceived += "Coindesk: "+ coin.getPrice()+", "+(coin.getDiff()>0 ? "GIẢM "+
                        String.format("%.2f", (coin.getDiff()))
                        : "TĂNG "+ String.format("%.2f", Math.abs(Math.abs(coin.getDiff())))) + "\n";
        }

        return coin;
    }

    private Coin getCoinValueCoinDeskVND(Coin compareCoin) throws IOException{
        Coin coin = new Coin();
        coin.setCurrency("VND");
        coin.setRate(getExchangeValueFromCoinSource(COIN_DESK_URL,"bpi").getAsJsonObject()
                .get(coin.getCurrency()).getAsJsonObject().get("rate").getAsString());
        return coin;
    }

}
