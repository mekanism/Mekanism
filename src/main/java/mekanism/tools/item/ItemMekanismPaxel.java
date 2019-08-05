package mekanism.tools.item;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import mekanism.common.util.LangUtils;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.Materials;
import mekanism.tools.common.MekanismTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismPaxel extends ItemTool implements IHasRepairType {

    public ItemMekanismPaxel(Materials material) {
        this(material.getPaxelMaterial(), true);
        setRegistryName(new ResourceLocation(MekanismTools.MODID, material.getMaterialName().toLowerCase(Locale.ROOT) + "_paxel"));
    }

    public ItemMekanismPaxel(ToolMaterial material) {
        this(material, false);
    }

    public ItemMekanismPaxel(ToolMaterial material, boolean nameSet) {
        super(4, -2.4F, material, new HashSet<>());
        setHarvestLevel("pickaxe", material.getHarvestLevel());
        setHarvestLevel("shovel", material.getHarvestLevel());
        setHarvestLevel("axe", material.getHarvestLevel());
        if (!nameSet) {
            setRegistryName(new ResourceLocation(MekanismTools.MODID, material.name().toLowerCase(Locale.ROOT) + "_paxel"));
        }
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