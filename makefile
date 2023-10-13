all:AggregationServer.class ContentServer.class GETClient.class

AggregationServer.class: AggregationServer.java OldDataHandler.class ClientHandler.class
	javac AggregationServer.java
ClientHandler.class: ClientHandler.java
	javac ClientHandler.java
OldDataHandler.class: OldDataHandler.java
	javac OldDataHandler.java

ContentServer.class: ContentServer.java
	javac ContentServer.java

GETClient.class: GETClient.java
	javac GETClient.java

run_server:
	java AggregationServer

run_getClient:
	java  getClient localhost:4567

run_ContentServer:
	java ContentServer localhost:4567
	

testIntegration1:
	mkdir test1
	cp GETClient.java ./test1
	cp ContentServer.java ./test1
	cp color.java ./test1
	cd test1
	javac GETClient.java
	javac ContentServer.java
	java ContentServer localhost:4567
	java GETClient localhost:4567
	cd ..
	echo "test1 completed"
	rm -r test1
	
testIntegration2:
	for i in {1..10};do mkdir test$i;cp GETClient.java ./test$i;cp ContentServer.java ./test$i;cp color.java ./test$i;cd test$i;javac GETClient.java;javac ContentServer.java;sleep 1;java ContentServer localhost:4567;java GETClient localhost:4567;cd ..;echo "test$i completed";m -r test$i;done;
	
testIntegrationWithBashScript:
	bash testingScript.bash