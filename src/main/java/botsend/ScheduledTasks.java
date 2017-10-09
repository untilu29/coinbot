package botsend;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dto.Coin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final String BLOCK_CHAIN_INFO_URL = "https://blockchain.info/markets/data/tickers.json";
    private static final String COIN_DESK_URL = "https://api.coindesk.com/v1/bpi/currentprice/VND.json";

    private static Coin localbitcoins = new Coin();
    private static Coin bitfinex = new Coin();
    private static Coin kraken = new Coin();
    private static Coin bitstamp = new Coin();
    private static Coin btce = new Coin();
    private static Coin coindesk = new Coin();

    private JsonObject getJsonObjectFromUrl(String url) throws IOException {
        InputStreamReader reader = new InputStreamReader(new URL(url).openStream());
        JsonElement jsonElement = new Gson().fromJson(reader,JsonElement.class);
        return jsonElement.getAsJsonObject();
    }

    private JsonObject getExchangeValueFromCoinSource(String url, String exchangeName) throws IOException {
        return getJsonObjectFromUrl(url).get(exchangeName).getAsJsonObject();
    }

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() throws IOException, CloneNotSupportedException {
        log.info("The time is now {}", dateFormat.format(new Date()));

        Coin localbitcoins = getCoinValueBlockChainInfo("localbitcoins");
        Coin bitfinex = getCoinValueBlockChainInfo("bitfinex");
        Coin kraken = getCoinValueBlockChainInfo("kraken");
        Coin bitstamp = getCoinValueBlockChainInfo("bitstamp");
        Coin btce = getCoinValueBlockChainInfo("btce");
        coindesk = getCoinValueCoinDesk("USD",coindesk);
    }

    private Coin getCoinValueBlockChainInfo(String exchangeName) throws IOException {
        Coin coin = new Coin();
        coin.setName(exchangeName);
        coin.setCurrency("BTC_USD");
        coin.setPrice(getExchangeValueFromCoinSource(BLOCK_CHAIN_INFO_URL,exchangeName).get(coin.getCurrency())
                .getAsJsonObject().get("p").getAsDouble());
        coin.setDiff(getExchangeValueFromCoinSource(BLOCK_CHAIN_INFO_URL,exchangeName).get(coin.getCurrency())
                .getAsJsonObject().get("d").getAsDouble());
        return coin;
    }

    private Coin getCoinValueCoinDesk(String currencyName, Coin compareCoin) throws IOException, CloneNotSupportedException {
        Coin coin = new Coin();
        coin.setCurrency(currencyName);
        coin.setPrice(getExchangeValueFromCoinSource(COIN_DESK_URL,"bpi").getAsJsonObject()
                .get(coin.getCurrency()).getAsJsonObject().get("rate_float").getAsDouble());

        if (coin.compareTo(compareCoin)!=0||compareCoin==null) {
            coin.setDiff(coin.getPrice()-(compareCoin.getPrice()!=null?compareCoin.getPrice():0));
            log.info("Data changed: {}", coin.getPrice()+", Diff: "+ coin.getDiff());
        }
        return coin;
    }

}
