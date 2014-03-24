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

		MekanismUtils.updateDonators();
	}
}
