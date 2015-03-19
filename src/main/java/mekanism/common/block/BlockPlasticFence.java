package mekanism.common.block;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;

import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPlasticFence extends BlockFence
{
	public BlockPlasticFence()
	{
		super("mekanism:PlasticFence", Material.clay);
		setCreativeTab(Mekanism.tabMekanism);
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

    public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour)
    {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta != (15 - colour))
        {
            world.setBlockMetadataWithNotify(x, y, z, 15-colour, 3);
            return true;
        }
        return false;
    }

	@Override
	public int damageDropped(int i)
	{
		return i;
	}
}
