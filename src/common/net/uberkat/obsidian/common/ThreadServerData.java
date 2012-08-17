package net.uberkat.obsidian.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import cpw.mods.fml.client.FMLClientHandler;

import net.minecraft.src.ModLoader;

public class ThreadServerData extends Thread
{
	/**
	 * Sends information about this mod to the Obsidian Ingots server.
	 */
	public ThreadServerData()
	{
		setDaemon(true);
		start();
	}
	
	public void run()
	{
		System.out.println("[ObsidianIngots] Initiating server protocol...");
		try {
			InetAddress address = InetAddress.getByName(ObsidianIngots.hostIP);
			Socket socket = new Socket(address, ObsidianIngots.hostPort);
			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			if(ObsidianUtils.isMultiplayer())
			{
				writer.println("USER:server");
			}
			else {
				if(FMLClientHandler.instance().getClient().thePlayer != null)
				{
					writer.println("USER:" + FMLClientHandler.instance().getClient().thePlayer.username);
				}
				else {
					writer.println("USER:client");
				}
			}
			writer.println("DONE");
			writer.close();
			socket.close();
		} catch (IOException e)
		{
			System.err.println("[ObsidianIngots] Could not connect to server, error was '" + e.getMessage() + ".'");
		}
	}
}
