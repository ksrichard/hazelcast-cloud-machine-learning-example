Hazelcast Cloud Distributed Live Machine Learning Example
---
This is a sample project that shows how easily you can create distributed LIVE machine learning application by using Hazelcast Cloud.

This repository contains 2 applications:
- **Machine learning data collector**
    - Collects periodically fresh data to recalculate BTC - USD price predictions
    - Logs updates on predictions
- **Machine learning worker**
    - imports historical data regarding BTC - USD price
    - calculates predictions for next day based on all or partial data (last 7 to 30 entries or all entries)
    using Linear Regression algorithm

To start the example applications, please check both readme files.