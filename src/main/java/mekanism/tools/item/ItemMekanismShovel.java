package mekanism.tools.item;

import java.util.List;
import mekanism.common.util.LangUtils;
import mekanism.tools.common.Materials;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismShovel extends ItemSpade {

    public ItemMekanismShovel(Materials material) {
        super(material.getMaterial());
        setHarvestLevel("shovel", material.getMaterial().getHarvestLevel());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        list.add(LangUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
    }
}