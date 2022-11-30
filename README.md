Some notes on this project:

To trigger, please run the request GET http://localhost:8080/run/hubspot-interview
I had some issues getting the IDE set up when coding up the solution. If I had some more time, I would update the trigger endpoint to a POST passing the URL to get the data to make it more flexible. Additionally, I would add error handling for all of the exceptions thrown to return an appropriate HTTP response code and error message instead of just throwing and return a 500 if there is an Exception. Finally, if you see the translateSessionDataForPost method in AggregationService, this is horribly inefficient as I am looping through all of the user sessions again to cast my data to the specified contract for the POST. I did not set up my classes correctly that I use to perform the aggregation logic, and did not have time to refactor to avoid having to do this conversion. My initial POST was formatted like the following:

[
{
"visitorId":"d1177368-2310-11e8-9e2a-9b860a0d9039",
"sessions":[
{
"duration":195000,
"pagesVisited":[
"/pages/a-big-river",
"/pages/a-small-dog",
"/pages/a-big-river"
],
"startTime":1512754436000
}
]
},
{
"visitorId":"f877b96c-9969-4abc-bbe2-54b17d030f8b",
"sessions":[
{
"duration":1976000,
"pagesVisited":[
"/pages/a-big-talk",
"/pages/a-sad-story",
"/pages/a-sad-story"
],
"startTime":1512709024000
}
]
}
]

I apologize for not having time to perform the refactor.

As a final note, I would have liked to add some unit tests to test the individual methods (specifically the aggregation method) if time permitted.