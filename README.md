## Crawler test task

Solution consists of two packages:

 * com.vjache.crawler.engine   - actually a crawler
 * com.vjache.crawler.restapi  - a RESTul API wor crawler
 
Also there is an embedded simple web site with dummy but linked pages 
for demo & testing purposes (see 'main/resources').

The server starts on localhost:8080 hence check port is free.
The documents are stored at './crawler-data' directory.

TODO

1. To make server restartable and scalable, I can use external queue 
server (e.g. RabbitMQ) instead of internal 'Crawler.queue', an also I 
need to store the fact that crawler loaded some page, this can be done 
in two ways:
 * store downloaded URL in a data base
 * or, store document files in such a way that allows fast check if document 
   for particular URL is already downloaded. This is partially already 
   done -- documents files are distributed over 256 buckets, and URL of 
   a document stored in a separate file. But it is better to enhance this 
   aspect to protect performance and concurrent file readings(files can be 
   partiallu written). Also if we want a set of such a crawlers work on 
   different computers in a collaboration, we would need to share such a 
   store to make it possible for concurrent crawlers do not download the 
   same URL, and of course they must be connected to the same queue on 
   the queue server.  

