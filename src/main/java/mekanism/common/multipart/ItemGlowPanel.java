package mekanism.common.multipart;

import mcmultipart.item.ItemMultiPart;
import mcmultipart.multipart.IMultipart;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.base.IMetaItem;
import mekanism.common.util.LangUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemGlowPanel extends ItemMultiPart implements IMetaItem
{
	public ItemGlowPanel()
	{
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public IMultipart createPart(World world, BlockPos pos, EnumFacing orientation, Vec3d vHit, ItemStack item, EntityPlayer player)
	{
		EnumColor col = EnumColor.DYES[item.getItemDamage()];

		if(pos != null && orientation != null)
		{
			BlockPos pos1 = pos.offset(orientation);
			
			if(world.isSideSolid(pos1, orientation.getOpposite()))
			{
				return new PartGlowPanel(col, orientation);
			}
		}

		return null;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> listToAddTo)
	{
		for(EnumColor color : EnumColor.DYES)
		{
			listToAddTo.add(new ItemStack(item, 1, color.getMetaValue()));
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		EnumColor colour = EnumColor.DYES[stack.getItemDamage()];
		String colourName;

        if(I18n.canTranslate(getUnlocalizedName(stack) + "." + colour.dyeName))
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

	@Override
	public boolean shouldRotateAroundWhenRendering()
	{
		return true;
	}

	@Override
	public String getTexture(int meta)
	{
		return "glow_panel";
	}

	@Override
	public int getVariants()
	{
		return EnumColor.DYES.length;
	}
}
