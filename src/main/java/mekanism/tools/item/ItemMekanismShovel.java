package mekanism.tools.item;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.Materials;
import mekanism.tools.common.MekanismTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMekanismShovel extends ShovelItem implements IHasRepairType {

    public ItemMekanismShovel(Materials material) {
        super(material.getMaterial());
        setHarvestLevel("shovel", material.getMaterial().getHarvestLevel());
        setRegistryName(new ResourceLocation(MekanismTools.MODID, material.getMaterialName().toLowerCase(Locale.ROOT) + "_shovel"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(TextComponentUtil.build(Translation.of("mekanism.tooltip.hp"), ": " + (stack.getMaxDamage() - stack.getDamage())));
    }

    @Override
    public ItemStack getRepairStack() {
        return toolMaterial.getRepairItemStack();
    }
}