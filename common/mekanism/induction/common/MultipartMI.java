package mekanism.induction.common;

import mekanism.induction.common.wire.EnumWireMaterial;
import mekanism.induction.common.wire.multipart.PartWire;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartMI implements IPartFactory
{

	public MultipartMI()
	{
		this.init();
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if (name == "resonant_induction_wire")
			return new PartWire(EnumWireMaterial.COPPER.ordinal());
		return null;
	}

	public void init()
	{
		MultiPartRegistry.registerParts(this, new String[] { "resonant_induction_wire" });
		MultipartGenerator.registerPassThroughInterface("universalelectricity.core.block.IConductor");
		MultipartGenerator.registerPassThroughInterface("buildcraft.api.power.IPowerReceptor");
		MultipartGenerator.registerPassThroughInterface("resonantinduction.wire.IInsulatedMaterial");
		MultipartGenerator.registerPassThroughInterface("resonantinduction.wire.multipart.IBlockableConnection");
		MultipartGenerator.registerTrait("ic2.api.energy.tile.IEnergySink", "resonantinduction.wire.multipart.javatraits.TEnergySink");
	}

}
