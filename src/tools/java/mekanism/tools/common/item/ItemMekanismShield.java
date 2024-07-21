package mekanism.tools.common.item;

import java.util.List;
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.NotNull;

public class ItemMekanismShield extends ShieldItem {

    private final Tier tier;

    public ItemMekanismShield(BaseMekanismMaterial material, Item.Properties properties) {
        super(properties.durability(material.getShieldDurability()).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY));
        this.tier = material;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);//Add the banner type description
        ToolsUtils.addDurability(tooltip, stack);
    }

    @NotNull
    public Ingredient getRepairMaterial() {
        return tier.getRepairIngredient();
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        return getRepairMaterial().test(repair);
    }

    @Override
    @Deprecated
    public int getEnchantmentValue() {
        return tier.getEnchantmentValue();
    }
}