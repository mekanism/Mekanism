package mekanism.common.multipart;

import mekanism.common.Tier;
import mekanism.common.Tier.CableTier;
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

	public void init()
	{
		MultiPartRegistry.registerParts(this, new String[] {"mekanism:universal_cable_basic",
				"mekanism:universal_cable_advanced", "mekanism:universal_cable_elite",
				"mekanism:universal_cable_ultimate", "mekanism:mechanical_pipe",
				"mekanism:pressurized_tube", "mekanism:logistical_transporter",
				"mekanism:restrictive_transporter", "mekanism:diversion_transporter"});

		MultipartGenerator.registerPassThroughInterface("mekanism.api.transmitters.ITransmitter");
		MultipartGenerator.registerPassThroughInterface("mekanism.api.energy.IStrictEnergyAcceptor");
		MultipartGenerator.registerPassThroughInterface("mekanism.api.transmitters.IGridTransmitter");
		MultipartGenerator.registerPassThroughInterface("mekanism.common.ILogisticalTransporter");
		MultipartGenerator.registerPassThroughInterface("buildcraft.api.power.IPowerReceptor");
		MultipartGenerator.registerPassThroughInterface("cofh.api.energy.IEnergyHandler");
		MultipartGenerator.registerPassThroughInterface("mekanism.common.IConfigurable");
		MultipartGenerator.registerPassThroughInterface("mekanism.common.ITileNetwork");
		MultipartGenerator.registerPassThroughInterface("ic2.api.tile.IWrenchable");
		MultipartGenerator.registerPassThroughInterface("mekanism.api.transmitters.IBlockableConnection");
	}

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		if(name.equals("mekanism:universal_cable"))
		{
			return new PartUniversalCable(Tier.CableTier.BASIC);
		}
		else if(name.equals("mekanism:universal_cable_basic"))
		{
			return new PartUniversalCable(Tier.CableTier.BASIC);
		}
		else if(name.equals("mekanism:universal_cable_advanced"))
		{
			return new PartUniversalCable(Tier.CableTier.ADVANCED);
		}
		else if(name.equals("mekanism:universal_cable_elite"))
		{
			return new PartUniversalCable(Tier.CableTier.ELITE);
		}
		else if(name.equals("mekanism:universal_cable_ultimate"))
		{
			return new PartUniversalCable(Tier.CableTier.ULTIMATE);
		}
		else if(name.equals("mekanism:mechanical_pipe"))
		{
			return new PartMechanicalPipe();
		}
		else if(name.equals("mekanism:pressurized_tube"))
		{
			return new PartPressurizedTube();
		}
		else if(name.equals("mekanism:logistical_transporter"))
		{
			return new PartLogisticalTransporter();
		}
		else if(name.equals("mekanism:restrictive_transporter"))
		{
			return new PartRestrictiveTransporter();
		}
		else if(name.equals("mekanism:diversion_transporter"))
		{
			return new PartDiversionTransporter();
		}

		return null;
	}
}
