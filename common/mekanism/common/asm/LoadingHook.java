package mekanism.common.asm;

import java.util.Map;

import codechicken.core.launch.DepLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class LoadingHook implements IFMLLoadingPlugin
{
	public LoadingHook()
	{
		System.out.println("Loading DepLoader");
		DepLoader.load();
	}
	
	@Override
	public String[] getLibraryRequestClass()
	{
		return null;
	}

	@Override
	public String[] getASMTransformerClass()
	{
		return null;
	}

	@Override
	public String getModContainerClass()
	{
		return null;
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		
	}
}
