package mekanism.api.gas;


public class OreGas extends Gas
{
	private String oreName;
	private OreGas cleanGas;

	public OreGas(String s, String name)
	{
		super(s, "mekanism:blocks/liquid/Liquid" + (s.contains("clean") ? "Clean" : "") + "Ore");

		oreName = name;
	}

	public boolean isClean()
	{
		return getCleanGas() == null;
	}

	public OreGas getCleanGas()
	{
		return cleanGas;
	}

	public OreGas setCleanGas(OreGas gas)
	{
		cleanGas = gas;

		return this;
	}

	public String getOreName()
	{
		return StatCollector.translateToLocal(oreName);
	}
}
