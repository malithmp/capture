package UserInterface;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;

import Global.GlobalVar;
import Network.HttpMethods;

import com.google.gson.Gson;
public class MainUserInteface {

	// Application Specific Globals
	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainUserInteface window = new MainUserInteface();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainUserInteface() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("ze map");
		frame.setBounds(GlobalVar.STARTX, GlobalVar.STARTY, GlobalVar.WIDTH, GlobalVar.HEIGHT); // alternative method:: frame.setSize(450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		final MapPanel map = new MapPanel();
		map.setBounds(GlobalVar.MAP_STARTX,GlobalVar.MAP_STARTY ,GlobalVar.MAP_WIDTH, GlobalVar.MAP_HEIGHT);
		frame.add(map);
		JButton btnShow = new JButton("Show");
		btnShow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//WebsocketStuff wss = new WebsocketStuff();
				//wss.doWebsocketStuff();
//				int[][] x = new int[6][2];
//				x[0] = new int[] {0,20};
//				x[1] = new int[] {30,40};
//				x[2] = new int[] {50,60};
//				x[3] = new int[] {70,80};
//				x[4] = new int[] {80,80};
//				x[5] = new int[] {90,80};
//				map.drawCurve(x,5,Color.RED);
				TalkToServerAndUpdateMap d = new TalkToServerAndUpdateMap(map,"","");
				Thread t = new Thread(d);
				t.start();
			}
		});
		btnShow.setBounds(683, 543, 117, 29);
		frame.getContentPane().add(btnShow);
	}
	
}

class TalkToServerAndUpdateMap implements Runnable{
	/* Gets Data from the server using a separate thread 
	*  Updates the recieved data on the map
	*  1 object per request
	*/
	
	private MapPanel map;
	private String method;	// GET/POST/UPDATE/DELETE
	private String params;	// Parameters for the above methods
	
	TalkToServerAndUpdateMap(MapPanel map,String method,String params){
		this.map=map;
		this.method=method;
		this.params=params;
	}
	@Override
	public void run() {
		System.out.println("Startig thread");
		try {
			Thread.sleep(1000);
			//HttpMethods.get(new String[][] {{"requesttype","admin"},{"adminname","malithmp"},{"password","pass"},{"action","tempgetdbpath"}});
			String gson = test_temp_gsonTest();
			HttpMethods.post(new String[][]{{"requesttype","admin"}},gson);
			//PrintWriter out = new PrintWriter("filename.txt");
			//out.println(gson);
			//System.out.println(gson.hashCode());
			
		} catch (Exception e) {
			//TODO Auto-generated catch block
			e.printStackTrace();
		}

		int[][] x = new int[6][2];
		x[0] = new int[] {5,20};
		x[1] = new int[] {30,40};
		x[2] = new int[] {100,60};
		x[3] = new int[] {30,8};
		x[4] = new int[] {80,180};
		x[5] = new int[] {2,30};
		map.drawCurve(x,5,Color.YELLOW);
	}
	
	String test_temp_gsonTest(){
		// convert arraylist to json so we can send it using httppost
		
		Gson gson = new Gson();
		ArrayList<String> list = new ArrayList<String>();
		String x ="String11212";
		for(int i=0;i<10;i++){
			x = ""+x.hashCode();
			list.add(x);
			System.out.println(x);
		}
		System.out.println("client hash: "+list.hashCode());
		return gson.toJson(list);
	}
}
