package mekanism.tools.item;

import java.util.List;
import mekanism.common.util.LangUtils;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.Materials;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismAxe extends ItemAxe implements IHasRepairType {

    public ItemMekanismAxe(Materials material) {
        this(material.getMaterial(), material.getAxeDamage(), material.getAxeSpeed());
    }

    public ItemMekanismAxe(ToolMaterial toolMaterial, float axeDamage, float axeSpeed) {
        super(toolMaterial, axeDamage, axeSpeed);
        setHarvestLevel("axe", toolMaterial.getHarvestLevel());
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