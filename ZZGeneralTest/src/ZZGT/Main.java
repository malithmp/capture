package ZZGT;

import java.math.BigInteger;

public class Main {
	
	public static void main(String[] args){

		System.out.printf("%2x", (int)(-10));
//		System.out.println("meh");
//		R2 r2 = new R2();
//		final Thread t2 = new Thread(r2);
//		R1 r1 = new R1(t2);
//		final Thread t1 =  new Thread(r1);
//		t1.start();
//		t2.start();

		
	}
}

class R2 implements Runnable{
	@Override
	public void run() {
		System.out.println("T1");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.out.println("Wide awake");
		}
		
	}
	
}

class R1 implements Runnable{
	Thread idiot = null;
	public R1(Thread target){
		idiot=target;
	}
	@Override
	public void run() {
		System.out.println("T1");
		try {
			Thread.sleep(3000);
			System.out.println("Wake up the idiot");
			idiot.interrupt();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
}