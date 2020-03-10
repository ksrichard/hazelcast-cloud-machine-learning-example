Hazelcast Cloud Machine Learning Data Collector
---
This application shows that how easily can multiple languages use the same Hazelcast Cloud cluster.
It is periodically gets the current price of BTC - USD, puts into an IMap (distributed map)
and listens on any events on a predictions map, which contains always the actual predictions of BTC - USD prices for the next day.

How to run
---
1. Install `jdk8` and `maven`
2. Replace the following values with your Hazelcast Cluster details:
    ```java
    public static final String HZ_CLOUD_CLUSTER_NAME = "TODO";
    public static final String HZ_CLOUD_CLUSTER_PASSWORD = "TODO";
    public static final String HZ_CLOUD_DISCOVERY_TOKEN = "TODO";
    ```
3. Run `$ mvn clean compile exec:java@application` in the root directory


