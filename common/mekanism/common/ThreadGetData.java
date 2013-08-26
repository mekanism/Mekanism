package mekanism.common;

import mekanism.common.util.MekanismUtils;

/**
 * Thread used to retrieve data from the Mekanism server.
 * @author AidanBrady
 *
 */
public class ThreadGetData extends Thread
{
	public ThreadGetData()
	{
		setDaemon(true);
		start();
	}
	
	@Override
	public void run()
	{
		Mekanism.latestVersionNumber = MekanismUtils.getLatestVersion();
		Mekanism.recentNews = MekanismUtils.getRecentNews();
		System.out.println("[Mekanism] Successfully retrieved data from server.");
		try {
			finalize();
		} catch(Throwable t) {
			System.out.println("[Mekanism] Unable to finalize server data.");
		}
	}
}
