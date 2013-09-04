package Network;

import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.token.BaseTokenClient;

public class WebsocketStuff implements WebSocketClientListener{

	public static void doWebsocketStuff(){
		WebsocketStuff c = new WebsocketStuff();
        BaseTokenClient client = new BaseTokenClient();
        client.addListener(c);
        try {
			client.open("ws://localhost:8080/Capture_Server/WS");
			//client.login(null, null);
			client.broadcastText("bbbb");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		System.out.println(arg1.getString());

	}

	@Override
	public void processReconnecting(WebSocketClientEvent arg0) {
		// TODO Auto-generated method stub

	}

}