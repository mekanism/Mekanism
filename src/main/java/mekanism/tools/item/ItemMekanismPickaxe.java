package mekanism.tools.item;

import java.util.List;
import java.util.Locale;
import mekanism.common.util.LangUtils;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.Materials;
import mekanism.tools.common.MekanismTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismPickaxe extends ItemPickaxe implements IHasRepairType {

    public ItemMekanismPickaxe(Materials material) {
        super(material.getMaterial());
        setHarvestLevel("pickaxe", material.getMaterial().getHarvestLevel());
        setRegistryName(new ResourceLocation(MekanismTools.MODID, material.getMaterialName().toLowerCase(Locale.ROOT) + "_pickaxe"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        list.add(LangUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
    }

    @Override
    public ItemStack getRepairStack() {
        return toolMaterial.getRepairItemStack();
    }
}