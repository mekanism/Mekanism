package mekanism.tools.common.recipe;

import mekanism.tools.common.item.ItemMekanismShield;
import mekanism.tools.common.registries.ToolsRecipeSerializers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.NotNull;

public class MekBannerShieldRecipe extends CustomRecipe {

    public static final MekBannerShieldRecipe INSTANCE = new MekBannerShieldRecipe();

    private MekBannerShieldRecipe() {

    }

    @Override
    public boolean matches(CraftingInput inv, @NotNull Level world) {
        ItemStack shieldStack = ItemStack.EMPTY;
        ItemStack bannerStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof BannerItem) {
                    if (!bannerStack.isEmpty()) {
                        return false;
                    }
                    bannerStack = stackInSlot;
                } else {
                    if (!(stackInSlot.getItem() instanceof ItemMekanismShield) || !shieldStack.isEmpty()) {
                        return false;
                    }
                    BannerPatternLayers bannerpatternlayers = stackInSlot.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
                    if (!bannerpatternlayers.layers().isEmpty()) {
                        return false;
                    }
                    shieldStack = stackInSlot;
                }
            }
        }
        return !shieldStack.isEmpty() && !bannerStack.isEmpty();
    }

    @NotNull
    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemStack bannerStack = ItemStack.EMPTY;
        ItemStack shieldStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stackInSlot = inv.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getItem() instanceof BannerItem) {
                    bannerStack = stackInSlot;
                } else if (stackInSlot.getItem() instanceof ItemMekanismShield) {
                    shieldStack = stackInSlot.copy();
                }
            }
        }
        if (shieldStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        shieldStack.set(DataComponents.BANNER_PATTERNS, bannerStack.get(DataComponents.BANNER_PATTERNS));
        shieldStack.set(DataComponents.BASE_COLOR, ((BannerItem) bannerStack.getItem()).getColor());
        return shieldStack;
    }

    @NotNull
    @Override
    public RecipeSerializer<MekBannerShieldRecipe> getSerializer() {
        return ToolsRecipeSerializers.BANNER_SHIELD.get();
    }
}