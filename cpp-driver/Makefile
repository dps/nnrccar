CC=g++

all: cpp-driver

cpp-driver:
	$(CC) main.cpp PracticalSocket.cpp FeatureStreamer.cpp -o cpp-driver -lblas -lpthread

test-mailbox:
	$(CC) test_Mailbox.cpp -I/usr/local/include -o test_Mailbox -L/usr/local/lib -lcppunit -ldl