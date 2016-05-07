package mekanism.common.multipart;

import java.util.Collections;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.render.RenderGlowPanel;
import mekanism.common.MekanismItems;
import mekanism.common.block.states.BlockStateFacing;

import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.util.IIcon;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
/*
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.AxisAlignedBB;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.HollowMicroblock;
import codechicken.multipart.IconHitEffects;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JIconHitEffects;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
*/
import mcmultipart.MCMultiPartMod;
import mcmultipart.block.TileMultipart;
import mcmultipart.microblock.IMicroblock;
import mcmultipart.microblock.IMicroblock.IFaceMicroblock;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IOccludingPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.util.TransformationHelper;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PartGlowPanel extends Multipart implements IOccludingPart//, JIconHitEffects
{
	public EnumColor colour = EnumColor.WHITE;
	public EnumFacing side = EnumFacing.DOWN;

	public static AxisAlignedBB[] bounds = new AxisAlignedBB[6];

	static
	{
		AxisAlignedBB cuboid = new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.125, 0.75);
		Vec3 fromOrigin = new Vec3(-0.5, -0.5, -0.5);

		for(EnumFacing side : EnumFacing.VALUES)
		{
			bounds[side.ordinal()] = TransformationHelper.rotate(cuboid.offset(fromOrigin.xCoord, fromOrigin.yCoord, fromOrigin.zCoord), side).offset(-fromOrigin.xCoord, -fromOrigin.zCoord, -fromOrigin.zCoord);
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
	public String getType()
	{
		return "mekanism:glow_panel";
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
			//TileMultipart.dropItem(new ItemStack(MekanismItems.GlowPanel, 1, colour.getMetaValue()), getWorld(), Vector3.fromTileEntityCenter(tile()));
			getContainer().removePart(this);
		}
	}

	@Override
	public void onPartChanged(IMultipart other)
	{
		if(!getWorld().isRemote && !canStay())
		{
			//TileMultipart.dropItem(new ItemStack(MekanismItems.GlowPanel, 1, colour.getMetaValue()), getWorld(), Vector3.fromTileEntityCenter(tile()));
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
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("side", side.ordinal());
		nbt.setInteger("colour", colour.getMetaValue());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		side = EnumFacing.getFront(nbt.getInteger("side"));
		colour = EnumColor.DYES[nbt.getInteger("colour")];
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass)
	{
		if(pass == 0)
		{
			RenderGlowPanel.getInstance().renderStatic(this);
			return true;
		}
		
		return false;
	}
*/

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

/*
	@Override
	public TextureAtlasSprite getBreakingIcon(Object subPart, EnumFacing side)
	{
		return RenderGlowPanel.icon;
	}

	@Override
	public TextureAtlasSprite getBrokenIcon(EnumFacing side)
	{
		return RenderGlowPanel.icon;
	}
*/

/*
	@Override
	public void addHitEffects(MovingObjectPosition hit, EffectRenderer effectRenderer)
	{
		IconHitEffects.addHitEffects(this, hit, effectRenderer);
	}

	@Override
	public void addDestroyEffects(MovingObjectPosition mop, EffectRenderer effectRenderer)
	{
		IconHitEffects.addDestroyEffects(this, effectRenderer, false);
	}
*/

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
		Coord4D adj = new Coord4D(getPos().offset(side), getWorld().provider.getDimensionId());
		return getWorld().isSideSolid(adj, side.getOpposite()) || (getContainer().getPartInSlot(PartSlot.getFaceSlot(side)) instanceof IFaceMicroblock && ((IFaceMicroblock)getContainer().getPartInSlot(PartSlot.getFaceSlot(side))).isFaceHollow());
	}

	@Override
	public String getModelPath()
	{
		return getType();
	}

	@Override
	public BlockState createBlockState()
	{
		return new BlockStateFacing(MCMultiPartMod.multipart);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state)
	{
		return state.withProperty(BlockStateFacing.facingProperty, side);
	}
}
