package mekanism.induction.common;

import mekanism.induction.common.wire.EnumWireMaterial;
import mekanism.induction.common.wire.PartWire;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartMI implements IPartFactory
{
	public MultipartMI()
	{
		init();
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if(name == "resonant_induction_wire")
		{
			return new PartWire(EnumWireMaterial.COPPER.ordinal());
		}
		
		return null;
	}

	public void init()
	{
		MultiPartRegistry.registerParts(this, new String[] {"resonant_induction_wire"});
		
		MultipartGenerator.registerPassThroughInterface("universalelectricity.core.block.IConductor");
		MultipartGenerator.registerPassThroughInterface("buildcraft.api.power.IPowerReceptor");
		MultipartGenerator.registerPassThroughInterface("cofh.api.energy.IEnergyHandler");
		MultipartGenerator.registerPassThroughInterface("mekanism.induction.common.wire.IInsulatedMaterial");
		MultipartGenerator.registerPassThroughInterface("mekanism.induction.common.wire.IBlockableConnection");
		MultipartGenerator.registerTrait("ic2.api.energy.tile.IEnergySink", "mekanism.induction.common.wire.TEnergySink");
	}
}
