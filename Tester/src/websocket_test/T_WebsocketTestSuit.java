package websocket_test;

import org.jwebsocket.client.token.BaseTokenClient;


public class T_WebsocketTestSuit {
	public void websocketTest1(){
		T_Websocket c1 = new T_Websocket("echo:c1");
        BaseTokenClient client1 = new BaseTokenClient();
        T_Websocket c2 = new T_Websocket("echo:c2");
        BaseTokenClient client2 = new BaseTokenClient();
        T_Websocket c3 = new T_Websocket("echo:c3");
        BaseTokenClient client3 = new BaseTokenClient();
        T_Websocket c4 = new T_Websocket("echo:c4");
        BaseTokenClient client4 = new BaseTokenClient();
        T_Websocket c5 = new T_Websocket("echo:c5");
        BaseTokenClient client5 = new BaseTokenClient();
        T_Websocket c6 = new T_Websocket("echo:c6");
        BaseTokenClient client6 = new BaseTokenClient();
        T_Websocket c7 = new T_Websocket("echo:c7");
        BaseTokenClient client7 = new BaseTokenClient();
        client1.addListener(c1);
        client2.addListener(c2);
        client3.addListener(c3);
        client4.addListener(c4);
        client5.addListener(c5);
        client6.addListener(c6);
        client7.addListener(c7);
        try {
        	client1.open("ws://localhost:8080/Capture_Server/WS");
			//client.login(null, null);
			client1.broadcastText("c1");
			
			client2.open("ws://localhost:8080/Capture_Server/WS");
			//client.login(null, null);
			client2.broadcastText("c2");
			
			client3.open("ws://localhost:8080/Capture_Server/WS");
			//client.login(null, null);
			client3.broadcastText("c3");
			
			client4.open("ws://localhost:8080/Capture_Server/WS");
			//client.login(null, null);
			client4.broadcastText("c4");
			
			client5.open("ws://localhost:8080/Capture_Server/WS");
			//client.login(null, null);
			client5.broadcastText("c5");
			
			client6.open("ws://localhost:8080/Capture_Server/WS");
			//client.login(null, null);
			client6.broadcastText("c6");
			
			client7.open("ws://localhost:8080/Capture_Server/WS");
			//client.login(null, null);
			client7.broadcastText("c7");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
