package mekanism.generators.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.generators.common.MekanismGenerators;

public class MekanismGeneratorsClient extends MekanismGenerators
{

	@SideOnly(Side.CLIENT)

	//General Configuration
	public static boolean enableAmbientLighting;
	public static int ambientLightingLevel;

}
