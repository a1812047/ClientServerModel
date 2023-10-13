for i in {1..10};do
    mkdir test$i
    cp GETClient.java ./test$i
    cp ContentServer.java ./test$i
    cp color.java ./test$i
    cp weatherData1.txt weatherData2.txt weatherData3.txt  ./test$i
    cd test$i;javac GETClient.java
    javac ContentServer.java
    java ContentServer localhost:4567
    java GETClient localhost:4567
    cd ..
    echo "test$i completed"
    rm -r test$i;
done
	