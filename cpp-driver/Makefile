CC=g++

all: cpp-driver

cpp-driver: main.cpp PracticalSocket.cpp FeatureStreamer.cpp NeuralNetwork.cpp
	$(CC) main.cpp PracticalSocket.cpp FeatureStreamer.cpp NeuralNetwork.cpp -o cpp-driver -lblas -lpthread -g 

test-mailbox:
	$(CC) test_Mailbox.cpp -I/usr/local/include -o test_Mailbox -L/usr/local/lib -lcppunit -ldl