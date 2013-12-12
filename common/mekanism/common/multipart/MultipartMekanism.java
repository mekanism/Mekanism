package mekanism.common.multipart;

import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartConverter;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;

public class MultipartMekanism implements IPartFactory, IPartConverter
{
	public MultipartMekanism()
	{
		init();
	}
	
	public void init()
	{
		MultiPartRegistry.registerConverter(this);
		MultiPartRegistry.registerParts(this, new String[]{"mekanism:universal_cable", "mekanism:mechanical_pipe", "mekanism:pressurized_tube", "mekanism:logistical_transporter"});
		MultipartGenerator.registerPassThroughInterface("mekanism.api.transmitters.ITransmitter");
		MultipartGenerator.registerPassThroughInterface("ic2.api.energy.tile.IEnergySink");
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
//		if(name == "mekanism:logistical_transporter")
//			return new PartLogisticalTransporter();
		return null;
	}
	
	@Override
	public boolean canConvert(int blockID)
	{
		return blockID == Mekanism.transmitterID;
	}

	@Override
	public TMultiPart convert(World world, BlockCoord pos)
	{
		if(world.getBlockId(pos.x, pos.y, pos.z) == Mekanism.transmitterID)
		{
			int meta = world.getBlockMetadata(pos.x, pos.y, pos.z);
			return PartTransmitter.getPartType(TransmissionType.fromOldMeta(meta));
		}
		return null;
	}
}
