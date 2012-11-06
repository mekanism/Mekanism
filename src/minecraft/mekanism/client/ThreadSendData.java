package mekanism.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import cpw.mods.fml.client.FMLClientHandler;

import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import net.minecraft.src.ModLoader;

/**
 * Sends information about this mod to the Mekanism server.
 */
public class ThreadSendData extends Thread
{
	public ThreadSendData()
	{
		setDaemon(true);
		start();
	}
	
	@Override
	public void run()
	{
		System.out.println("[Mekanism] Initiating server protocol...");
		try {
			InetAddress address = InetAddress.getByName(Mekanism.hostIP);
			Socket socket = new Socket(address, Mekanism.hostPort);
			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			writer.println("USER:" + FMLClientHandler.instance().getClient().session.username);
			writer.println("DONE");
			writer.close();
			socket.close();
			try {
				finalize();
			} catch (Throwable e) {
				System.err.println("[Mekanism] Could not end server thread, error was '" + e.getMessage() + ".'");
			}
		} catch (IOException e)
		{
			System.err.println("[Mekanism] Could not connect to server, error was '" + e.getMessage() + ".'");
			try {
				finalize();
			} catch (Throwable e1) {
				System.err.println("[Mekanism] Could not end server thread, error was '" + e.getMessage() + ".'");
			}
		}
	}
}
