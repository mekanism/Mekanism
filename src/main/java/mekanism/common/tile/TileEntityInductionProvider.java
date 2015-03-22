package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.common.Tier.InductionProviderTier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityInductionProvider extends TileEntityBasicBlock
{
	public InductionProviderTier tier = InductionProviderTier.BASIC;
	
	@Override
	public void onUpdate() {}
	
	@Override
	public boolean canUpdate()
	{
		return false;
	}
	
	public String getInventoryName()
	{
		return MekanismUtils.localize(getBlockType().getUnlocalizedName() + ".InductionProvider" + tier.getBaseTier().getName() + ".name");
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		tier = InductionProviderTier.values()[dataStream.readInt()];

		super.handlePacketData(dataStream);

		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(tier.ordinal());

		super.getNetworkedData(data);

		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		tier = InductionProviderTier.values()[nbtTags.getInteger("tier")];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("tier", tier.ordinal());
	}
}
