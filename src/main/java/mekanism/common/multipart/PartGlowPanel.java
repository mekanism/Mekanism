package mekanism.common.multipart;

import java.util.Collections;
import java.util.List;

import mcmultipart.microblock.IMicroblock.IFaceMicroblock;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.INormallyOccludingPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.PartMOP;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.MekanismItems;
import mekanism.common.block.states.BlockStateFacing;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.util.IIcon;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.property.IExtendedBlockState;

public class PartGlowPanel extends Multipart implements INormallyOccludingPart
{
	public EnumColor colour = EnumColor.WHITE;
	public EnumFacing side = EnumFacing.DOWN;

	public static AxisAlignedBB[] bounds = new AxisAlignedBB[6];

	static
	{
		AxisAlignedBB cuboid = new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.125, 0.75);
		Vec3d fromOrigin = new Vec3d(-0.5, -0.5, -0.5);

		for(EnumFacing side : EnumFacing.VALUES)
		{
			bounds[side.ordinal()] = MultipartMekanism.rotate(cuboid.offset(fromOrigin.xCoord, fromOrigin.yCoord, fromOrigin.zCoord), side).offset(-fromOrigin.xCoord, -fromOrigin.zCoord, -fromOrigin.zCoord);
		}
	}

	public PartGlowPanel()
	{
		super();
	}

	public PartGlowPanel(EnumColor colour, EnumFacing side)
	{
		super();
		setColour(colour);
		setOrientation(side);
	}

	@Override
	public void addSelectionBoxes(List<AxisAlignedBB> list)
	{
		list.add(bounds[side.ordinal()]);
	}

	@Override
	public ResourceLocation getType()
	{
		return new ResourceLocation("mekanism:glow_panel");
	}

	public void setColour(EnumColor newColour)
	{
		colour = newColour;
	}

	public void setOrientation(EnumFacing newSide)
	{
		side = newSide;
	}
	
	@Override
	public void onNeighborTileChange(EnumFacing side)
	{
		if(!getWorld().isRemote && !canStay())
		{
			MultipartMekanism.dropItem(new ItemStack(MekanismItems.GlowPanel, 1, colour.getMetaValue()), this);
			getContainer().removePart(this);
		}
	}

	@Override
	public void onPartChanged(IMultipart other)
	{
		if(!getWorld().isRemote && !canStay())
		{
			MultipartMekanism.dropItem(new ItemStack(MekanismItems.GlowPanel, 1, colour.getMetaValue()), this);
			getContainer().removePart(this);
		}
	}

	@Override
	public void writeUpdatePacket(PacketBuffer data)
	{
		data.writeInt(side.ordinal());
		data.writeInt(colour.getMetaValue());
	}

	@Override
	public void readUpdatePacket(PacketBuffer data)
	{
		side = EnumFacing.getFront(data.readInt());
		colour = EnumColor.DYES[data.readInt()];
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("side", side.ordinal());
		nbt.setInteger("colour", colour.getMetaValue());
		
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		side = EnumFacing.getFront(nbt.getInteger("side"));
		colour = EnumColor.DYES[nbt.getInteger("colour")];
	}
	
	@Override
	public int getLightValue()
	{
		return 15;
	}

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list)
	{
		addSelectionBoxes(list);
	}
	
	@Override
	public float getHardness(PartMOP partHit)
	{
		return 3.5F;
	}
	
	@Override
	public List<ItemStack> getDrops()
	{
		return Collections.singletonList(getPickBlock(null, null));
	}

	@Override
	public ItemStack getPickBlock(EntityPlayer player, PartMOP hit)
	{
		return new ItemStack(MekanismItems.GlowPanel, 1, colour.getMetaValue());
	}

	public boolean canStay()
	{
		Coord4D adj = new Coord4D(getPos().offset(side), getWorld());
		return getWorld().isSideSolid(adj.getPos(), side.getOpposite()) || (getContainer().getPartInSlot(PartSlot.getFaceSlot(side)) instanceof IFaceMicroblock && ((IFaceMicroblock)getContainer().getPartInSlot(PartSlot.getFaceSlot(side))).isFaceHollow());
	}

	@Override
	public ResourceLocation getModelPath()
	{
		return getType();
	}

	@Override
	public BlockStateContainer createBlockState()
	{
		return new GlowPanelBlockState();
	}
	
	@Override
	public IBlockState getActualState(IBlockState state)
	{
		return state.withProperty(BlockStateFacing.facingProperty, side);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state)
	{
		state = state.withProperty(BlockStateFacing.facingProperty, side);
		
		if(state instanceof IExtendedBlockState)
		{
			return ((IExtendedBlockState)state).withProperty(ColorProperty.INSTANCE, new ColorProperty(colour));
		}
		
		return state;
	}
	
	public static int hash(IExtendedBlockState state)
	{
		int hash = 1;
		hash = 31 * hash + state.getValue(ColorProperty.INSTANCE).color.ordinal();
		hash = 31 * hash + state.getValue(BlockStateFacing.facingProperty).ordinal();
		
		return hash;
	}

	@Override
	public boolean shouldBreakingUseExtendedState() {
		return true;
	}
}
