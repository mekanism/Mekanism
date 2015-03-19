package mekanism.common.util;

public class DebugUtils 
{
	private static long prevNanoTime;
	
	public static void startTracking()
	{
		prevNanoTime = System.nanoTime();
	}
	
	public static long getTrackedNanos()
	{
		return System.nanoTime()-prevNanoTime;
	}
	
	public static long getTrackedMicros()
	{
		return getTrackedNanos()/1000;
	}
	
	public static long getTrackedMillis()
	{
		return getTrackedMicros()/1000;
	}
}
