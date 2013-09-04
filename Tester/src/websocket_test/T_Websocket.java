package websocket_test;

import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.token.BaseTokenClient;

public class T_Websocket implements WebSocketClientListener{
	String expected;
	public T_Websocket(String out){
		expected=out;
	}
	@Override
	public void processClosed(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processOpened(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processOpening(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processPacket(WebSocketClientEvent arg0, WebSocketPacket arg1) {
		if(arg1.getString().equals(expected)){
			System.out.println("Passed");
		}
		else{
			System.out.println("Failed");
			System.out.println(arg1.getString());
		}
	}

	@Override
	public void processReconnecting(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub

	}

}
