package mekanism.common.asm;

import java.util.Map;

import codechicken.core.launch.DepLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class LoadingHook implements IFMLLoadingPlugin
{
	public LoadingHook()
	{
		DepLoader.load();
	}
	
	public String[] getLibraryRequestClass()
	{
		return null;
	}

	public String[] getASMTransformerClass()
	{
		return null;
	}

	public String getModContainerClass()
	{
		return null;
	}

	public String getSetupClass()
	{
		return null;
	}

	public void injectData(Map<String, Object> data)
	{
		
	}
}
