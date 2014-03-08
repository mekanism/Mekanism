package mekanism.tools.item;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;

public class ItemMekanismPaxel extends ItemMekanismTool
{
	public ItemMekanismPaxel(int i, EnumToolMaterial enumtoolmaterial)
	{
		super(i, 3, enumtoolmaterial, new Block[256]);
	}

	@Override
	public float getStrVsBlock(ItemStack itemstack, Block block)
	{
		return block.blockID != Block.bedrock.blockID ? efficiencyOnProperMaterial : 1.0F;
	}

	@Override
	public float getStrVsBlock(ItemStack stack, Block block, int meta)
	{
		if(ForgeHooks.isToolEffective(stack, block, meta))
		{
			return efficiencyOnProperMaterial;
		}

		return getStrVsBlock(stack, block);
	}

	@Override
	public boolean canHarvestBlock(Block block)
	{
		if(block == Block.obsidian)
		{
			return toolMaterial.getHarvestLevel() == 3;
		}

		if(block == Block.blockDiamond || block == Block.oreDiamond)
		{
			return toolMaterial.getHarvestLevel() >= 2;
		}

		if(block == Block.blockGold || block == Block.oreGold)
		{
			return toolMaterial.getHarvestLevel() >= 2;
		}

		if(block == Block.blockIron || block == Block.oreIron)
		{
			return toolMaterial.getHarvestLevel() >= 1;
		}

		if(block == Block.blockLapis || block == Block.oreLapis)
		{
			return toolMaterial.getHarvestLevel() >= 1;
		}

		if(block == Block.oreRedstone || block == Block.oreRedstoneGlowing)
		{
			return toolMaterial.getHarvestLevel() >= 2;
		}

		if(block.blockMaterial == Material.rock)
		{
			return true;
		}

		return block.blockMaterial == Material.iron;
	}
}
