package mekanism.tools.item;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import mekanism.common.util.LangUtils;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.Materials;
import mekanism.tools.common.MekanismTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ToolItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMekanismPaxel extends ToolItem implements IHasRepairType {

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
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(LangUtils.localize("tooltip.hp") + ": " + (stack.getMaxDamage() - stack.getItemDamage()));
    }

    @Override
    public ItemStack getRepairStack() {
        return toolMaterial.getRepairItemStack();
    }
}