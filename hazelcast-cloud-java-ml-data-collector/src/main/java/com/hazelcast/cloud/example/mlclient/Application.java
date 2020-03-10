package com.hazelcast.cloud.example.mlclient;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.spi.impl.discovery.HazelcastCloudDiscovery;
import com.hazelcast.client.spi.properties.ClientProperty;
import com.hazelcast.cloud.example.mlclient.dto.BtcCurrentPrice;
import com.hazelcast.cloud.example.mlclient.listener.PredictionsMapListener;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Application {

    /**
     * Hazelcast Cloud cluster credentials
     */
    public static final String HZ_CLOUD_CLUSTER_NAME = "TODO";
    public static final String HZ_CLOUD_CLUSTER_PASSWORD = "TODO";
    public static final String HZ_CLOUD_DISCOVERY_TOKEN = "TODO";

    /**
     * Distributed object names
     */
    public static final String PRICES_MAP_NAME = "btc_usd_prices";
    public static final String PREDICTIONS_MAP_NAME = "btc_usd_price_predictions";

    public static void main(String[] args) {
        // rest template
        RestTemplate template = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        messageConverters.add(converter);
        template.setMessageConverters(messageConverters);

        // hazelcast client
        ClientConfig config = new ClientConfig();
        config.setGroupConfig(new GroupConfig(HZ_CLOUD_CLUSTER_NAME, HZ_CLOUD_CLUSTER_PASSWORD));
        config.setProperty("hazelcast.client.statistics.enabled", "true");
        config.setProperty(ClientProperty.HAZELCAST_CLOUD_DISCOVERY_TOKEN.getName(), HZ_CLOUD_DISCOVERY_TOKEN);
        config.setProperty(HazelcastCloudDiscovery.CLOUD_URL_BASE_PROPERTY.getName(), "https://coordinator.hazelcast.cloud");
        HazelcastInstance client = HazelcastClient.newHazelcastClient(config);

        IMap<Long, Double> pricesMap = client.getMap(PRICES_MAP_NAME);
        IMap<String, String> predictionsMap = client.getMap(PREDICTIONS_MAP_NAME);
        predictionsMap.addEntryListener(new PredictionsMapListener(predictionsMap), true);

        // polling current price periodically
        Thread pollingThread = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println("Checking current BTC -> USD price....");
                    BtcCurrentPrice result = template.getForObject("https://api.coindesk.com/v1/bpi/currentprice/USD.json", BtcCurrentPrice.class);
                    if (result != null) {
                        Long currentTime = result.getTime().getUpdatedIso().getTime();
                        Double currentPrice = result.getRates().get("USD").getRateFloat();
                        if(pricesMap.get(currentTime) == null) {
                            pricesMap.put(currentTime, currentPrice);
                            System.out.println("Added new price: " + currentPrice);
                        } else {
                            System.out.println("No new price has been found!");
                        }
                    }
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        pollingThread.start();

        // waiting until interruption
        while (!Thread.currentThread().isInterrupted()) { }

        // cleanup
        pollingThread.interrupt();
        client.shutdown();
    }
}
