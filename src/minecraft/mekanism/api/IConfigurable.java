package mekanism.api;

import java.util.ArrayList;

public interface IConfigurable 
{
	public ArrayList<SideData> getSideData();
	
	public byte[] getConfiguration();
	
	public int getOrientation();
}
