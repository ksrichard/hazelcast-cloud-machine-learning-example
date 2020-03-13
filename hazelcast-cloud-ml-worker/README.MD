Hazelcast Cloud Machine Learning Worker
---
This is a simple python application, 
that aims to provide a simple, scalable worker that can calculate BTC -> USD price predictions
based on previous and current data.

Flow
---
1. At the beginning (when the distributed objects are empty), the application imports historical BTC - USD price data
from 365 days back to have a starting point for the prediction (if there are more instances, they are waiting for this process to be completed).
2. Starts listening on any events on the distributed map which stores BTC - USD prices
3. When any new entries are getting inserted to the prices map, the worker will put 2 items to a distributed queue: 
One is to calculate the overall price prediction for the next day and the other is to calculate from 7 to 30 items back predictions for the next day.
4. As the application instances are periodically try to poll new items from queue, then when a new item comes in, all instances will try to get it from queue and will execute the appropriate calculation based on queue item.

How to run
---
1. Install `python3` and `pip3`
2. `$ ./install.sh` to install all python dependencies
3. In `config.ini` set the Hazelcast Cloud configuration properties:
    ```ini
    [HAZELCAST_CLOUD]
    HzCloudClusterName = CLUSTER NAME
    HzCloudClusterPassword = CLUSTER PASSWORD
    HzCloudDiscoveryToken = CLUSTER DISCOVERY TOKEN
    ```

4. `$ ./run.sh` - to start a worker instance

You can start any amount of workers as the calculation and historical data import
is distributed across all workers using Hazelcast Cloud cluster.
