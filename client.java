import static java.lang.System.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class client{
	//Setup Callable classes
	private static class InputListener implements Runnable{		
		public void run()
		{
			try{
				DataOutputStream dout = new DataOutputStream(s.getOutputStream());
				while(true){
					Scanner input = new Scanner(in);
					String msg = input.nextLine();
					if(msg.isEmpty()){
						//ok, how to quit?
					}
					dout.writeUTF(msg);
				}
			}
			catch(Exception e)
			{
				
			}
		}
	}
	
	private static class SocketListener implements Callable<String>
	{
		@Override
		public String call() throws Exception
		{
			DataInputStream din = new DataInputStream(s.getInputStream());
			String data;
			while(true)
			{
				try{
					data = (String)din.readUTF();
					out.println("Server message: " + data);
				}
				catch(Exception e)
				{
					return "Connection lost: " + e;
				}
			}
		}
	}
	
	private static int port = 6969; //Haha funny number
	private static Socket s;
	public static void main(String[] args) throws IOException
	{
		//Declarations
		InetAddress host = InetAddress.getLocalHost();
		
		ExecutorService inputExecutor = Executors.newSingleThreadExecutor(); //Dedicate a single thread for input listening
		
		//Start connection to server
		s = new Socket(host.getHostName(), port);
		
		//Start background input thread
		inputExecutor.submit(new InputListener());
		
		//Start listening to server for messages
		ExecutorService serverListener = Executors.newSingleThreadExecutor();
		serverListener.submit(new SocketListener());
		
	}
}