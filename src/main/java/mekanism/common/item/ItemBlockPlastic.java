package mekanism.common.item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import mekanism.api.EnumColor;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class ItemBlockPlastic extends ItemBlock
{
	public Block metaBlock;

	public ItemBlockPlastic(Block block)
	{
		super(block);
		metaBlock = block;
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int i)
	{
		return i;
	}

/*
	@Override
	public IIcon getIconFromDamage(int i)
	{
		return metaBlock.getIcon(2, i);
	}
*/

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		EnumDyeColor dyeColour = EnumDyeColor.byMetadata(stack.getItemDamage()&15);
		EnumColor colour = EnumColor.DYES[dyeColour.getDyeDamage()];
		String colourName;

        if(StatCollector.canTranslate(getUnlocalizedName(stack) + "." + colour.dyeName))
        {
            return LangUtils.localize(getUnlocalizedName(stack) + "." + colour.dyeName);
        }

		if(colour == EnumColor.BLACK)
		{
			colourName = EnumColor.DARK_GREY + colour.getDyeName();
		}
		else {
			colourName = colour.getDyedName();
		}

		return colourName + " " + super.getItemStackDisplayName(stack);
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int renderPass)
	{
		return this.metaBlock.getRenderColor(this.metaBlock.getStateFromMeta(stack.getMetadata()));
	}
}
