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
					if(stopFlag)
						throw new InterruptedException("Lost Connection");
					if(msg.isEmpty()){
						Disconnect();
					}
					dout.writeUTF(msg);
				}
			}
			catch(Exception e)
			{
				out.println("Input listener shutting down");
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
					if(stopFlag) //Disconnection expected
						return "Done";
					//Disconnection unexpected
					HandleLostConnection();
					return "Connection lost: " + e;
				}
			}
		}
	}

	private static void HandleLostConnection()
	{
		out.println("Lost connection to server. Press enter to quit");
		stopFlag = true;
	}

	private static void Disconnect()
	{
		out.println("Disconnecting...");
		stopFlag = true;
		try {
			s.close();
		} catch (IOException e) {
			out.println("Error disconnecting: " + e);
		}
	}
	
	private static int port = 6969; //Haha funny number
	private static Socket s;
	private static boolean stopFlag;

	public static void main(String[] args) throws IOException
	{
		//Declarations
		InetAddress host = InetAddress.getLocalHost();

		ExecutorService inputExecutor = Executors.newSingleThreadExecutor(); //Dedicate a single thread for input listening
		while(true) {
			try {
				//Start connection to server
				s = new Socket(host.getHostName(), port);
				out.println("Connected to server");
				break;
			}
			catch(Exception e)
			{
				out.println("Failed connecting to server. Retrying");
			}
		}
		stopFlag = false;

		//Start background input thread
		inputExecutor.submit(new InputListener());
		inputExecutor.shutdown();

		//Start listening to server for messages
		ExecutorService serverListener = Executors.newSingleThreadExecutor();
		serverListener.submit(new SocketListener());
		serverListener.shutdown();

	}
}