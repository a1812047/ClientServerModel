all:AggregationServer.class ClientHandler.class ContentServer.class GETClient.class

AggregationServer.class: AggregationServer.java
	javac AggregationServer.java

ClientHandler.class: ClientHandler.java
	javac ClientHandler.java

ContentServer.class: ContentServer.java
	javac ContentServer.java

GETClient.class: GETClient.java
	javac GETClient.java
