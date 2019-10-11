import static java.lang.System.*;
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

public class server{
	
	//Setup Callable classes
	private static class InputListener implements Callable<String>{
		public String call() throws Exception
		{
			while(true){
				Scanner input = new Scanner(in);
				String msg = input.nextLine();
				if(msg.isEmpty()){
					//ok, how to quit?
					ShutdownServer();
					break;
				}
				if(msg.charAt(0) == '/') //Handle comment
				{	
					if(msg.equals("/status"))
					{
						PrintStatus();
					}
				}
				else
				for(Socket s : sockets){
					try{
						DataOutputStream dout = new DataOutputStream(s.getOutputStream());						
						dout.writeUTF(msg);
					}
					catch(Exception e)
					{
						//Maybe client already disconnected
					}
				}
			}
			return "NP"; //No problem
		}
	}
	
	private static class SocketListener implements Callable<String>
	{
		private Socket s;
		private String ip;
		public SocketListener(Socket socket)
		{
			s = socket;
			ip = s.getRemoteSocketAddress().toString();
		}
		
		@Override
		public String call() throws Exception
		{
			DataInputStream din = new DataInputStream(s.getInputStream());
			String data;
			while(true)
			{
				try{
					data = (String)din.readUTF();
					if(!data.isEmpty())
						out.println("Client " + ip + " message: " + data);
				}
				catch(Exception e)
				{
					out.println("Connection to client lost");
					sockets.remove(s);
					return "Connection lost: " + e;
				}
			}
			
		}
	}
	
	private static int port = 6969; //Haha funny number
	private static ServerSocket ss;
	private static List<Future<String>> listeners;
	private static List<Socket> sockets = new ArrayList<Socket>();
	
	private static void PrintStatus()
	{
		out.println("\n=============STATUS===============\n");
		out.println(sockets.size() + " sockets");
		out.println(listeners.size() + " listeners");
		out.println("\n=============------===============\n");
	}
	
	private static void StartMasterListener()//Handles input listener and closing server upon command
	{
		ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
		Future<String> inputResult = inputExecutor.submit(new InputListener());
		inputExecutor.shutdown();
	}
	
	private static void HandleNewSocket(Socket soc, ExecutorService executor)
	{
		sockets.add(soc);
		listeners.add(executor.submit(new SocketListener(soc)));
	}
	
	private static void ShutdownServer()
	{
		for(Socket s : sockets)
		{
			try{
				s.close();
			}
			catch(Exception e)
			{
				out.println("Failed shutting down socket of client ");
			}
		}
		try{
			ss.close();
		}
		catch(Exception e)
		{
			out.println("Failed shutting down server socket");
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		//Declarations
		ss = new ServerSocket(port);
		listeners = new ArrayList<Future<String>>();
		
		ExecutorService socketPools = Executors.newFixedThreadPool(10);
		
		//Start master listener
		StartMasterListener();
		
		out.println("Start accepting clients");
		while(true)
		{
			try{
				Socket s = ss.accept(); //Establishes the connection
				out.println("New client connected from " + s.getRemoteSocketAddress().toString());
				HandleNewSocket(s, socketPools);
			}
			catch(Exception e)
			{
				out.println("Exception occurred while listening, most likely server is shutting down: " + e);
				break;
			}
		}
		socketPools.shutdown();
		
	}
}