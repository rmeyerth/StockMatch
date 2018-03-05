#####################################################################
# Project: Exchange Stock Matcher (Royal Bank of Canada)            #
# By:      Robert Meyer                                             #
# Date:    05/03/2018                                               #
#####################################################################

Getting Started
===============
This project was developed with Java 8, Spring Boot and Maven. To compile the project and run the tests, navigate to the
directory in which the source resides and type the following:

    mvn clean install

It will take a short time to download the necessary dependencies and run the tests before creating a runnable jar which
can be found in the ./target/ directory. To run this jar, simply type:

    java -jar stockmatch-1.0-SNAPSHOT.jar

The jar is completely self-sufficient and once run should load up an embedded tomcat server and host the service on the
designated port, which is typically 8080.

Service Structure
=================
The service itself has five endpoints which can either be called from the browser or through a tool like Postman. These
endpoints consist of the following:

    POST /api/stockmatch/
    GET /api/stockmatch/interest/{ricCode}/{direction}
    GET /api/stockmatch/execution/{ricCode}/
    GET /api/stockmatch/quantity/{ricCode}/{user}/
    GET /api/stockmatch/clear

Add Order
---------
The first of these requires a JSON payload to be sent containing a new order. An order may consist of the following
fields and data:

    {
        "direction": "SELL",
        "ricCode": "VOD.L",
        "quantity": 1000,
        "price": "100.20",
        "user": "User1"
    }

If the order was processed and added correctly, the HTTP response code should be 201 (CREATED). If however there were
issues with any fields of content being sent, the response would be a 400 (BAD REQUEST).

Open Interest
-------------
The next endpoint takes two parameters which are ricCode and direction. These are specified in the URL. The StockMatch
service will scan currently open orders and calculate the Open Interest at each price point and will return these values
along with the two query parameters. A typical response from this endpoint would be the following:

    {
        "ricCode": "VOD.L",
        "direction": "SELL",
        "priceQuantity": [
            {
                "pricePoint": 100.2,
                "quantity": 1000
            }
        ]
    }

Average Execution Price
-----------------------
This endpoint takes a single parameter which is the ricCode and returns average weighted price for all executed orders.
Two values are returned which is the calculated average price and the query parameter.

    {
        "averagePrice": 99.88,
        "ricCode": "VOD.L"
    }

NOTE: Be aware that the ending backslash is required in the url e.g. execution/{ricCode}/. This is because unless the
correct URL encoding is used, full stops and other punctuation may cause the RIC code to be received in an unexpected
state.

Executed Quantity
-----------------
This endpoint uses the ricCode and user to calculate the quantity that the specified user has traded. For example, if
a user sell 1000 of VOD.L and then re-buys 500, the result will be -500. An example response can be seen below:

    {
        "ricCode": "VOD.L",
        "user": "User1",
        "quantity": 500
    }

Development Notes
=================
- Whilst the current implementation fulfills the requirement, there is a peculiarity in the design such as the dependency
between the OrderStore and ExecutedStore. This is because to calculate the interest, the store has to know whether the
current order has already been matched. This project really lends itself to a relational database, but if an in-memory
solution is used (as it is here), it would have been better to either flag the matches on the orders themselves, or
alternatively use a Query class structure to avoid a direct dependency between the two.
- The matcher classes use a version of the Observer pattern which register themselves in order to process events which
happen with given store object. This should allow other matchers with their own logic to be added without affecting any
existing code.
- Integration tests (SMControllerTest) were written prior to development based off of the example provided in the
specification document