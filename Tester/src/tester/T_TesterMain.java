package tester;

import resolver_test.MultiThreadDataStoreTests;
import websocket_test.T_WebsocketTestSuit;

public class T_TesterMain {
	public static void main(String[] args){
		//T_WebsocketTestSuit test1 = new T_WebsocketTestSuit();
		//test1.websocketTest1();
		
		MultiThreadDataStoreTests mtdt = new MultiThreadDataStoreTests();
		mtdt.test(1000,13);
	}
}
