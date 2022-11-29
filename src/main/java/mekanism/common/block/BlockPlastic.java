package mekanism.common.block;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPlastic extends Block
{
	public BlockPlastic(String type)
	{
		super(Material.wood);
		setHardness(type == "reinforced" ? 50F : 5F);
		setResistance(type == "reinforced" ? 2000F : 10F);
		setCreativeTab(Mekanism.tabMekanism);
		if (type == "slick")
			slipperiness = 0.98F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register)
	{
		if(this == MekanismBlocks.PlasticBlock)
		{
			blockIcon = register.registerIcon("mekanism:PlasticBlock");
		}
		else if(this == MekanismBlocks.SlickPlasticBlock)
		{
			blockIcon = register.registerIcon("mekanism:SlickPlasticBlock");
		}
		else if(this == MekanismBlocks.GlowPlasticBlock)
		{
			blockIcon = register.registerIcon("mekanism:GlowPlasticBlock");
		}
		else if(this == MekanismBlocks.ReinforcedPlasticBlock)
		{
			blockIcon = register.registerIcon("mekanism:ReinforcedPlasticBlock");
		}
		else if(this == MekanismBlocks.RoadPlasticBlock)
		{
			blockIcon = register.registerIcon("mekanism:RoadPlasticBlock");
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		if (this == MekanismBlocks.RoadPlasticBlock) {
			final float f = 1 / 128f;
			return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1 - f, z + 1);
		} else {
			return AxisAlignedBB.getBoundingBox((double)x + this.minX, (double)y + this.minY, (double)z + this.minZ, (double)x + this.maxX, (double)y + this.maxY, (double)z + this.maxZ);
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity e) {
			if (this == MekanismBlocks.RoadPlasticBlock) {

				if (e.getEntityData().getInteger("Mekanism:road") == e.ticksExisted)
					return;
				e.getEntityData().setInteger("Mekanism:road", e.ticksExisted);

				double boost = 0.99 * slipperiness;

				final double minSpeed = 1e-9;

				if (Math.abs(e.motionX) > minSpeed || Math.abs(e.motionZ) > minSpeed) {
					e.motionX += e.motionX * boost;
					e.motionZ += e.motionZ * boost;
				}
			}
	}

	@Override
	public int damageDropped(int i)
	{
		return i;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List list)
	{
		for(int i = 0; i < EnumColor.DYES.length; i++)
		{
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z)
	{
		return getRenderColor(world.getBlockMetadata(x, y, z));
	}

	@Override
	public int getRenderColor(int meta)
	{
		EnumColor colour = EnumColor.DYES[meta];
		return (int)(colour.getColor(0)*255) << 16 | (int)(colour.getColor(1)*255) << 8 | (int)(colour.getColor(2)*255);
	}

	@Override
	public int getLightValue()
	{
		if(this == MekanismBlocks.GlowPlasticBlock)
		{
			return 10;
		}

		return 0;
	}
	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z){

		return false;
	}

	@Override
	public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour)
	{
		int meta = world.getBlockMetadata(x, y, z);
		
		if(meta != (15 - colour))
		{
			world.setBlockMetadataWithNotify(x, y, z, 15-colour, 3);
			return true;
		}
		
		return false;
	}

	@Override
	public int getRenderType()
	{
		return Mekanism.proxy.PLASTIC_RENDER_ID;
	}
}
