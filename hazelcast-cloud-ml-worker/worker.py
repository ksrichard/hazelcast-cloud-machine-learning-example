import datetime
import json
import time
import configparser
import hazelcast
import pandas as pd
import requests
from sklearn.linear_model import LinearRegression

# Globals
BTC_USD_PRICES_MAP_NAME = "btc_usd_prices"
BTC_USD_PRICE_PREDICTION_MAP = "btc_usd_price_predictions"
N_ITEMS_PREDICTION_KEY_SUFFIX = "_items"
OVERALL_PREDICTIONS_TASK_NAME = "overall_predictions"
ITEMS_PREDICTIONS_TASK_NAME = "items_predictions"
PREDICTION_TASKS_QUEUE_NAME = "prediction_tasks"
HISTORICAL_DATA_IMPORT_LOCK_NAME = "historical_data_import"
ADD_TASKS = False
CURRENT_EVT_ID = None


def get_hz_client(groupName, groupPassword, discoveryToken):
    config = hazelcast.ClientConfig()
    config.group_config.name = groupName
    config.group_config.password = groupPassword
    config.network_config.cloud_config.enabled = True
    config.network_config.cloud_config.discovery_token = discoveryToken
    config.set_property("hazelcast.client.cloud.url", "https://coordinator.hazelcast.cloud")
    return hazelcast.HazelcastClient(config)


def import_historical_data(targetMap, startTime=None):
    print("Importing historical data...")
    # import historical BTC - USD price data from API if HZ map is empty
    startDateStr = "2010-07-17"
    if startTime is not None:
        startDateStr = startTime
    endDateStr = datetime.datetime.now().strftime("%Y-%m-%d")
    print("Importing historical data between", startDateStr, "and", endDateStr, "...")
    apiUrl = "https://api.coindesk.com/v1/bpi/historical/close.json?start=" + startDateStr + "&end=" + endDateStr
    resp = requests.get(url=apiUrl)
    btcUsdDetails = resp.json()['bpi']

    for currTime in btcUsdDetails:
        currentTime = time.mktime(datetime.datetime.strptime(currTime, "%Y-%m-%d").timetuple())
        currentPrice = btcUsdDetails[currTime]
        targetMap.put(currentTime, currentPrice)
        print("Number of historical data imported:", targetMap.size())


def load_all_historical_data(pricesMap):
    df = pd.DataFrame({'Time': [], 'USD Price': []})
    for currentTime, currentPrice in sorted(pricesMap.entry_set()):
        df = df.append({"Time": currentTime, 'USD Price': currentPrice}, ignore_index=True)
    x = df['Time'].values.reshape(-1, 1)
    y = df['USD Price'].values.reshape(-1, 1)
    return x, y


def predict_btc_usd_price(times, prices, lastNItems, predictionTime):
    startTime = time.time()
    regressor = LinearRegression()
    x = times
    y = prices
    if lastNItems > 0:
        x = times[-lastNItems:]
        y = prices[-lastNItems:]
    regressor.fit(x, y)
    prediction = regressor.predict([[predictionTime]])[0][0]
    stopTime = time.time()
    if lastNItems > 0:
        print("Last", lastNItems, "items prediction duration:", (stopTime - startTime), "seconds")
    else:
        print("All items prediction duration:", (stopTime-startTime), "seconds")
    return prediction


def do_predictions(pricesMap, predictionsMap, doItemsPrediction=True, doOverallPredictions=True):
    print("Start calculating predictions...")
    startTime = time.time()
    # load historical data once for predictions
    times, prices = load_all_historical_data(pricesMap)

    # predictions
    tomorrow = datetime.datetime.now() + datetime.timedelta(days=1)

    # up to last 31 days
    if doItemsPrediction:
        itemsBackStart = 7
        itemsBackStop = 31
        print("Calculating up to ", (itemsBackStop - itemsBackStart), " items back predictions...")
        for i in range(itemsBackStart, itemsBackStop):
            if i > 0:
                prediction = predict_btc_usd_price(times, prices, i, tomorrow.timestamp())
                predictionsMap.put(str(i) + N_ITEMS_PREDICTION_KEY_SUFFIX,
                                   json.dumps({'forDate': tomorrow.timestamp(), 'price': prediction}))
                print(tomorrow, str(i), "items -> BTC price in USD:", prediction, "$")

    # overall
    if doOverallPredictions:
        print("Calculating overall predictions...")
        prediction = predict_btc_usd_price(times, prices, 0, tomorrow.timestamp())
        predictionsMap.put("overall", json.dumps({'forDate': tomorrow.timestamp(), 'price': prediction}))

    stopTime = time.time()
    print("All predictions time:", (stopTime - startTime), "seconds")


def entry_listener(evt):
    global ADD_TASKS
    global CURRENT_EVT_ID
    ADD_TASKS = True
    CURRENT_EVT_ID = evt.uuid


if __name__ == "__main__":
    # connect to HZ Cloud cluster
    config = configparser.ConfigParser()
    config.read('config.ini')
    hz = get_hz_client(config['HAZELCAST_CLOUD']['HzCloudClusterName'],
                       config['HAZELCAST_CLOUD']['HzCloudClusterPassword'],
                       config['HAZELCAST_CLOUD']['HzCloudDiscoveryToken'])

    # getting distributed objects
    pricesMap = hz.get_map(BTC_USD_PRICES_MAP_NAME).blocking()
    predictionsMap = hz.get_map(BTC_USD_PRICE_PREDICTION_MAP).blocking()
    tasks = hz.get_queue(PREDICTION_TASKS_QUEUE_NAME).blocking()
    historicalDataImportLock = hz.get_lock(HISTORICAL_DATA_IMPORT_LOCK_NAME).blocking()

    try:
        # import historical data if needed
        daysToImport = 365
        if pricesMap.size() < daysToImport:
            if historicalDataImportLock.try_lock():
                try:
                    startTime = time.time()
                    oneYearAgo = (datetime.datetime.now() - datetime.timedelta(days=daysToImport)).strftime("%Y-%m-%d")
                    import_historical_data(pricesMap, oneYearAgo)
                    stopTime = time.time()
                    print("Historical data import took", (stopTime - startTime), "seconds")
                finally:
                    historicalDataImportLock.unlock()

        # wait for historical data import to be done if in progress
        while historicalDataImportLock.is_locked():
            print("Waiting for Historical data import to be finished...")

        print("Started waiting for events...")
        # add entry listener to trigger predictions
        pricesMap.add_entry_listener(True,
                                     added_func=entry_listener,
                                     removed_func=entry_listener,
                                     updated_func=entry_listener)

        while True:
            # if the prices map has modifications, add new calculation tasks to queue
            if ADD_TASKS:
                ADD_TASKS = False
                if CURRENT_EVT_ID is not None:
                    currentLock = hz.get_lock(CURRENT_EVT_ID).blocking()
                    if currentLock.try_lock():
                        try:
                            CURRENT_EVT_ID = None
                            tasks.add(OVERALL_PREDICTIONS_TASK_NAME)
                            tasks.add(ITEMS_PREDICTIONS_TASK_NAME)
                        finally:
                            currentLock.unlock()

            # polling a new task from queue
            currentTask = tasks.poll()
            if currentTask is not None:
                if currentTask == OVERALL_PREDICTIONS_TASK_NAME:
                    do_predictions(pricesMap, predictionsMap, False, True)

                if currentTask == ITEMS_PREDICTIONS_TASK_NAME:
                    do_predictions(pricesMap, predictionsMap, True, False)

    except KeyboardInterrupt:
        if historicalDataImportLock.is_locked():
            historicalDataImportLock.unlock()
        print('interrupted!')
