package mekanism.client.jei.interfaces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.gui.components.events.GuiEventListener;

public interface IJEIRecipeArea<ELEMENT extends GuiElement> extends GuiEventListener {

    /**
     * @return null if not an active recipe area, otherwise the category
     */
    @Nullable
    MekanismJEIRecipeType<?>[] getRecipeCategories();

    default boolean isJEIAreaActive() {
        return true;
    }

    ELEMENT jeiCategories(@Nonnull MekanismJEIRecipeType<?>... recipeCategories);

    default ELEMENT jeiCategory(TileEntityMekanism tile) {
        return jeiCategories(MekanismJEIRecipeType.findType(tile.getBlockType().getRegistryName()));
    }

    default ELEMENT jeiCrafting() {
        return jeiCategories(MekanismJEIRecipeType.VANILLA_CRAFTING);
    }

    default boolean isMouseOverJEIArea(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }
}