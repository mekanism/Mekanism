package mekanism.common.multipart;

import java.util.Collections;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.render.RenderGlowPanel;
import mekanism.common.MekanismItems;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
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

public class PartGlowPanel extends JCuboidPart implements JNormalOcclusion, JIconHitEffects
{
	public EnumColor colour = EnumColor.WHITE;
	public ForgeDirection side = ForgeDirection.DOWN;

	public static Cuboid6[] bounds = new Cuboid6[6];

	static
	{
		Cuboid6 cuboid = new Cuboid6(0.25, 0, 0.25, 0.75, 0.125, 0.75);
		Translation fromOrigin = new Translation(Vector3.center);
		Translation toOrigin = (Translation)fromOrigin.inverse();
		
		for(int i = 0; i < 6; i++)
		{
			bounds[i] = cuboid.copy().apply(toOrigin).apply(Rotation.sideRotations[i]).apply(fromOrigin);
		}
	}

	public PartGlowPanel()
	{
		super();
	}

	public PartGlowPanel(EnumColor colour, ForgeDirection side)
	{
		super();
		setColour(colour);
		setOrientation(side);
	}

	@Override
	public Cuboid6 getBounds()
	{
		return bounds[side.ordinal()];
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

	public void setOrientation(ForgeDirection newSide)
	{
		side = newSide;
	}
	
	@Override
	public void onNeighborChanged()
	{
		if(!world().isRemote && !canStay())
		{
			TileMultipart.dropItem(new ItemStack(MekanismItems.GlowPanel, 1, colour.getMetaValue()), world(), Vector3.fromTileEntityCenter(tile()));
			tile().remPart(this);
		}
	}

	@Override
	public void onPartChanged(TMultiPart other)
	{
		if(!world().isRemote && !canStay())
		{
			TileMultipart.dropItem(new ItemStack(MekanismItems.GlowPanel, 1, colour.getMetaValue()), world(), Vector3.fromTileEntityCenter(tile()));
			tile().remPart(this);
		}
	}

	@Override
	public void writeDesc(MCDataOutput data)
	{
		data.writeInt(side.ordinal());
		data.writeInt(colour.getMetaValue());
	}

	@Override
	public void readDesc(MCDataInput data)
	{
		side = ForgeDirection.getOrientation(data.readInt());
		colour = EnumColor.DYES[data.readInt()];
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		nbt.setInteger("side", side.ordinal());
		nbt.setInteger("colour", colour.getMetaValue());
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		side = ForgeDirection.getOrientation(nbt.getInteger("side"));
		colour = EnumColor.DYES[nbt.getInteger("colour")];
	}

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

	@Override
	public int getLightValue()
	{
		return 15;
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return getCollisionBoxes();
	}

	@Override
	public boolean occlusionTest(TMultiPart other)
	{
		return NormalOcclusionTest.apply(this, other);
	}

	@Override
	public IIcon getBreakingIcon(Object subPart, int side)
	{
		return RenderGlowPanel.icon;
	}

	@Override
	public IIcon getBrokenIcon(int side)
	{
		return RenderGlowPanel.icon;
	}

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

	@Override
	public Iterable<ItemStack> getDrops()
	{
		return Collections.singletonList(pickItem(null));
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit)
	{
		return new ItemStack(MekanismItems.GlowPanel, 1, colour.getMetaValue());
	}

	@Override
	public boolean doesTick()
	{
		return false;
	}

	public boolean canStay()
	{
		Coord4D adj = Coord4D.get(tile()).getFromSide(side);
		return world().isSideSolid(adj.xCoord, adj.yCoord, adj.zCoord, side.getOpposite()) || tile().partMap(side.ordinal()) instanceof HollowMicroblock;
	}
}
