package mekanism.common;

import java.net.Socket;

public class VoiceConnection 
{
	public Socket socket;
	
	public VoiceConnection(Socket s)
	{
		socket = s;
	}
}
