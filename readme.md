# Trade Server 
[![Build Status](https://travis-ci.com/whylu/NVLP_trade-server.svg?branch=master)](https://travis-ci.com/whylu/NVLP_trade-server)
[![codecov](https://codecov.io/gh/whylu/NVLP_trade-server/branch/master/graph/badge.svg)](https://codecov.io/gh/whylu/NVLP_trade-server)

## Run Server by docker image `whylu/nvlp-trade-server`
```sh
docker run --name trade-server -p 8080:8080 whylu/nvlp-trade-server
```

## Build project
```sh
./mvnw package
```

## Build Docker Image and Run
```sh
docker build -t nvlp-trade-server .
docker run --name trade-server -p 8080:8080 nvlp-trade-server
```


## Endpoints
### Get Orderbook of Market  
GET /order/book/<symbol>
```sh
curl "localhost:8080/order/book/BTC-USD"
```
response 
```js
{
  "bids":[
    [5,1],
    [3.5,1],
    [3,1],
    [2,1],
    [1,2.9]
  ],
  "asks":[
    [101,0.2],
    [102,0.1],
    [103,0.1],
    [103.5,0.1],
    [104,0.1]
  ]
}
```


### Place Order
POST /order  
Body
```js
{
  "userId": int,
  "symbol": string, // market symbol, like: "BTC-USD"
  "type": "LIMIT", 
  "side": "BUY",  // BUY or SELL
  "price": 1, 
  "size": 1
}
```

Response  
- Rejected
```js
{
  "symbol": "BTC-USD",
  "orderId": null,		// if order are handel by engine, return a non-null orderId
  "status": "REJECTED", // order status
  "type": "LIMIT",
  "side": "BUY",
  "price": 1,
  "size": 1,
  "transactTime": 1605324752385,  // epoch in ms, 
  "filledOrders": null,
  "errorCode": -6,
  "rejected": true
}
```

- INSERTED
```js
{
  "symbol": "BTC-USD",
  "orderId": 1,
  "status": "INSERTED",
  "type": "LIMIT",
  "side": "BUY",
  "price": 1,
  "size": 1,
  "transactTime": 1605326286002,
  "filledOrders": [],
  "errorCode": 0
}
````

- FILLED
```js
{
  "symbol": "BTC-USD",
  "orderId": 3,
  "status": "FILLED",
  "type": "LIMIT",
  "side": "SELL",
  "price": 1,
  "size": 0.3,
  "transactTime": 1605328894910,
  "filledOrders": [
    {
      "price": 1,
      "size": 0.1,
      "timestamp": 1605328860788
    },
    {
      "price": 1,
      "size": 0.2,
      "timestamp": 1605328871980
    }
  ],
  "errorCode": 0
}
```

#### 

Example
```sh
curl -X POST -H "Content-Type: application/json" -d '{"userId" : 1, "symbol": "BTC-USD", "type" : "LIMIT", "side": "BUY", "price": 1, "size": 1}' "http://localhost:8080/order"
```


### Get Wallet
GET /wallet/<userId>/<currency>   

Example
```sh
curl "localhost:8080/wallet/1/USD"
```


### Deposite Wallet
POST /wallet/deposit  
Body
```js
{
  "amount": 1000,
  "currency": "USD",
  "userId": 1
}
````

Response
```js
{
  "amount": 1000,  // total amount in wallet
  "currency": "USD",
  "userId": 1
}
````

Example
```sh
curl -X POST -H "Content-Type: application/json" -d '{"amount":1000,"currency":"USD","userId":1}' "http://localhost:8080/wallet/deposit"
```



## Order Status
| Status  |  Description |
| ---- | ---- |
| INSERTED | An order is inserted into orderbook |
| PARTIALLY_FILLED | An order is partially match and inserted into orderbook |
| FILLED | An order is fully filled |
| CANCELED | An order is cancelled by user, |
| REJECTED | An order is rejected, use 'errorCode' to identify the reason |


## Error Code
| Code | Description |  
| ---- | ---- |
| 0 | None |
| -1 | Invalid market |
| -2 | Order price less than minimum |
| -3 | Order price increment invalid | 
| -4 | Order size less than minimum | 
| -5 | Order size increment invalid |
| -6 | insufficient balance |
| -7 | Missing userId |
| -8 | Missing order type |
| -9 | Missing order side |
| -10 | Order price less than zero |
| -11 | Missing order size | 
| -12 | Order size less than zero | 
| -13 | Invalid order type |





## Config Market Settings
resources/market.properties
```properties
markets.base.BTC.quote.USD.minPrice=0.5
markets.base.BTC.quote.USD.minPriceIncrement=0.5
markets.base.BTC.quote.USD.minSize=0.0002
markets.base.BTC.quote.USD.minSizeIncrement=0.0001
```

