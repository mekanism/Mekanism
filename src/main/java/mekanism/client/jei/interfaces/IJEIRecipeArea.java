package mekanism.client.jei.interfaces;

import javax.annotation.Nullable;
import mekanism.client.gui.element.GuiElement;
import mekanism.common.Mekanism;
import mekanism.common.tile.base.TileEntityMekanism;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;

public interface IJEIRecipeArea<ELEMENT extends GuiElement> extends GuiEventListener {

    /**
     * @return null if not an active recipe area, otherwise the category
     */
    @Nullable
    ResourceLocation[] getRecipeCategories();

    default boolean isJEIAreaActive() {
        return true;
    }

    ELEMENT jeiCategories(@Nullable ResourceLocation... recipeCategories);

    default ELEMENT jeiCategory(TileEntityMekanism tile) {
        return jeiCategories(tile.getBlockType().getRegistryName());
    }

    default ELEMENT jeiCrafting() {
        if (Mekanism.hooks.JEILoaded) {
            return jeiCategories(VanillaRecipeCategoryUid.CRAFTING);
        }
        return jeiCategories((ResourceLocation) null);
    }

    default boolean isMouseOverJEIArea(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }
}