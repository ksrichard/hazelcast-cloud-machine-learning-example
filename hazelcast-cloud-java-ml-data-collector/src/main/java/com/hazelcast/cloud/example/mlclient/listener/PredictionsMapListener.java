package com.hazelcast.cloud.example.mlclient.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.cloud.example.mlclient.dto.BtcUsdPrediction;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;
import com.hazelcast.map.impl.MapListenerAdapter;

/**
 * Map listener class to react for events on predictions map
 */
public class PredictionsMapListener extends MapListenerAdapter<String, String> {

    private IMap<String, String> predictionsMap;

    public PredictionsMapListener(IMap<String, String> predictionsMap) {
        this.predictionsMap = predictionsMap;
    }

    @Override
    public void onEntryEvent(EntryEvent<String, String> event) {
        printPrediction("overall");
        printPrediction("30_items");
        System.out.println();
    }

    private void printPrediction(String key) {
        try {
            String predictionData = predictionsMap.get(key);
            if (predictionData != null) {
                ObjectMapper mapper = new ObjectMapper();
                BtcUsdPrediction prediction = mapper.readValue(predictionData, BtcUsdPrediction.class);
                System.out.println("Prediction (" + key + "): " + prediction.getPrice() + "$");
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
