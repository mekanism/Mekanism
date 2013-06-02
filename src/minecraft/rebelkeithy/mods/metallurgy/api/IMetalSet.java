package rebelkeithy.mods.metallurgy.api;

import java.util.Map;

public interface IMetalSet 
{
	public IOreInfo getOreInfo(String name);
	public IOreInfo getOreInfo(int metadata);
	public Map<String, IOreInfo> getOreList();
}
