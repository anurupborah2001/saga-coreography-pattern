# Java Spring Boot project Using Microservice Saga Choreography Pattern
### NB: This project is using Java 11 , if need to change to any other Java version change it in the `pom.xml` file 

### Componets :
1. Spring Boot
2. Kafka
3. MYSQL

**Saga Choreography Pattern :**  A saga is a sequence of local transactions. Each local transaction updates the database and publishes a message or event to trigger the next local transaction in the saga. If a local transaction fails because it violates a business rule then the saga executes a series of compensating transactions that undo the changes that were made by the preceding local transactions.

![Saga Choreography pattern](https://github.com/anurupborah2001/saga-coreography-pattern/images/saga-choreography1.png)


##Order-Payment Saga Choreography Pattern           
![Saga Choreography pattern](https://github.com/anurupborah2001/saga-coreography-pattern/images/saga-choreography-design.png)

##Request and Response :
 **1. Create Order Request**
> curl --location --request POST 'http://localhost:8081/order/create' \
--header 'Content-Type: application/json' \
--data-raw '{
"userId": 103,
"productId": 33,
"amount": 4000
}'

 **Response**
>{
"id": 15,
"userId": 101,
"productId": 57,
"price": 1500,
"orderStatus": "ORDER_CREATED",
"paymentStatus": null
}

 **2. Retrieve All Order Request**
> curl --location --request GET 'http://localhost:8081/orders' \
   --header 'Content-Type: application/json' \
   --data-raw '

 **Response**
> [
{
"id": 5,
"userId": 105,
"productId": 3276,
"price": 500,
"orderStatus": "ORDER_COMPLETED",
"paymentStatus": "PAYMENT_COMPLETED"
},
{
"id": 4,
"userId": 105,
"productId": 3276,
"price": 500,
"orderStatus": "ORDER_CANCELLED",
"paymentStatus": "PAYMENT_FAILED"
}
]

## Kafka Payload
  ## Success Payment 
> {"eventId":"30c4a629-640f-4ec4-9f08-44c422d79034","eventDate":"2021-09-24T04:51:35.945+00:00","orderRequestDto":{"userId":105,"productId":458,"amount":500,"orderId":10},"orderStatus":"ORDER_CREATED","date":"2021-09-24T04:51:35.945+00:00"}

> {"eventId":"e42472b8-6fad-4874-8f23-1c5379e80a1d","eventDate":"2021-09-24T04:51:36.093+00:00","paymentRequestDto":{"orderId":10,"userId":105,"amount":500},"paymentStatus":"PAYMENT_COMPLETED","date":"2021-09-24T04:51:36.093+00:00"}

 ## Insufficient Payment
> {"eventId":"9284716c-6676-4693-8135-af1f07655937","eventDate":"2021-09-24T04:51:41.711+00:00","orderRequestDto":{"userId":105,"productId":458,"amount":500,"orderId":11},"orderStatus":"ORDER_CANCELLED","date":"2021-09-24T04:51:41.711+00:00"}

> {"eventId":"d46d6fb7-d460-4814-af0e-b1e972e27192","eventDate":"2021-09-24T04:51:41.699+00:00","paymentRequestDto":{"orderId":11,"userId":105,"amount":500},"paymentStatus":"PAYMENT_FAILED","date":"2021-09-24T04:51:41.699+00:00"}

## DB Schema
1. User Balance Table

![Saga Choreography pattern](https://github.com/anurupborah2001/saga-coreography-pattern/images/db-img1.png)

2. Purchase Order Table

![Saga Choreography pattern](https://github.com/anurupborah2001/saga-coreography-pattern/images/db-img2.png)

3. User Transaction Table 

![Saga Choreography pattern](https://github.com/anurupborah2001/saga-coreography-pattern/images/db-img3.png)

## Reference
> https://microservices.io/patterns/data/saga.html