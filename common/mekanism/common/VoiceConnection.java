package mekanism.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class VoiceConnection 
{
	public Socket socket;
	
	public DataInputStream input;
	public DataOutputStream output;
	
	public VoiceConnection(Socket s)
	{
		socket = s;
	}
	
	public void start()
	{
		try {
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try {
						
					} catch(Exception e) {}
				}
			}).start();
		} catch(Exception e) {}
	}
}
