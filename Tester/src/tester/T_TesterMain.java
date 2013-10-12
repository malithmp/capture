package tester;

import resolver_test.BetterMultiThreadedTest;

public class T_TesterMain {
	public static void main(String[] args){
		//T_WebsocketTestSuit test1 = new T_WebsocketTestSuit();
		//test1.websocketTest1();
		
		BetterMultiThreadedTest test1 = new BetterMultiThreadedTest();
		test1.test(1);
	}
}
