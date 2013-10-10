package servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

// Handles realtime actions of the server
// Exchange data in JSON formatted strings

@WebServlet(value="/WS")
public class WS_Server extends WebSocketServlet {
	private static final long serialVersionUID = 1L;
	public static final ArrayList<EchoSocket> connections = new ArrayList<EchoSocket>();
	public static int i=0;

	// LIST OF ALL THE DATABASES / OR ANY DATASTRUCTURES
	@Override
	protected StreamInbound createWebSocketInbound(String string, HttpServletRequest hsr) {
		EchoSocket echosocket = new EchoSocket();
		connections.add(echosocket);
		i++;
		return echosocket;
	}

	private class EchoSocket extends MessageInbound{


		@Override
		protected void onOpen(WsOutbound outbound) {
			System.out.println("meh");	
			super.onOpen(outbound);
		}

		@Override
		protected synchronized void onBinaryMessage(ByteBuffer bb) throws IOException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		protected synchronized void onTextMessage(CharBuffer cb) throws IOException {
			System.out.println("ThreadID = "+Thread.currentThread().getId());

			String msg = cb.toString();
			String temp = "";
			//try{
			//	JSONObject json = (JSONObject) JSONSerializer.toJSON( msg );
			//	System.out.println(msg);
			//	temp=json.getString("data");
			//}catch(Exception e){
			// nothing
			//}
			//Thread t = new Thread(new Runnn(("echo:"+temp),connections.get(0)));
			//t.start();
			for(int i=0;i<connections.size();i++){
				//         	for(int j=0;j<10000;j++){
				connections.get(i).getWsOutbound().writeTextMessage(CharBuffer.wrap(msg.toCharArray()));
			}
			//         }
			//getWsOutbound().writeTextMessage(CharBuffer.wrap(msg.toCharArray()));

		}

	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// YES WEBSOCKETS CAN HAVE GET,POST,DELETE and PUT (A.K.A REST API) toO!
		try {
			PrintWriter pw = response.getWriter();
			pw.println("websocket servlet here.. SUP? ");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	class Runnn implements Runnable{
		String name;
		EchoSocket echosocket;
		public Runnn(String name,EchoSocket es){
			this.name = name;
			this.echosocket = es;
		}
		@Override
		public void run() {
			try {
				Thread.sleep(4000);
				echosocket.getWsOutbound().writeTextMessage(CharBuffer.wrap(name.toCharArray()));
				Thread.sleep(4000);
				echosocket.getWsOutbound().writeTextMessage(CharBuffer.wrap("Later".toCharArray()));

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
