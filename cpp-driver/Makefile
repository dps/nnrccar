CC=g++

all: cpp-driver

cpp-driver: main.cpp PracticalSocket.cpp FeatureStreamer.cpp NeuralNetwork.cpp
	$(CC) main.cpp PracticalSocket.cpp FeatureStreamer.cpp NeuralNetwork.cpp -o cpp-driver -lblas -lpthread

test-mailbox:
	$(CC) test_Mailbox.cpp -I/usr/local/include -o test_Mailbox -L/usr/local/lib -lcppunit -ldl

test-neuralnetwork:
	$(CC) test_NeuralNetwork.cpp NeuralNetwork.cpp -I/usr/local/include -o test_NeuralNetwork -L/usr/local/lib -lcppunit -ldl -lblas -g