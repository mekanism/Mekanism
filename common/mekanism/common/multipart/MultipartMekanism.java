package mekanism.common.multipart;

import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartMekanism implements IPartFactory
{
	public MultipartMekanism()
	{
		init();
	}
	
	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if(name == "mekanism:universal_cable")
			return new PartUniversalCable();
		if(name == "mekanism:mechanical_pipe")
			return new PartMechanicalPipe();
		if(name == "mekanism:pressurized_tube")
			return new PartPressurizedTube();
		return null;
	}
	
	public void init()
	{
		MultiPartRegistry.registerParts(this, new String[]{"mekanism:universal_cable", "mekanism:mechanical_pipe", "mekanism:pressurized_tube"});
		MultipartGenerator.registerPassThroughInterface("mekanism.api.transmitters.ITransmitter");
		MultipartGenerator.registerPassThroughInterface("cofh.api.energy.IEnergyHandler");
		MultipartGenerator.registerPassThroughInterface("ic2.api.energy.tile.IEnergySink");
	}
}
