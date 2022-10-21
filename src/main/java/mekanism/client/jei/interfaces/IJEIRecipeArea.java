package mekanism.client.jei.interfaces;

import mekanism.client.gui.element.GuiElement;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IJEIRecipeArea<ELEMENT extends GuiElement> extends GuiEventListener {

    /**
     * @return null if not an active recipe area, otherwise the category
     */
    @Nullable
    MekanismJEIRecipeType<?>[] getRecipeCategories();

    default boolean isJEIAreaActive() {
        return true;
    }

    ELEMENT jeiCategories(@NotNull MekanismJEIRecipeType<?>... recipeCategories);

    default ELEMENT jeiCategory(TileEntityMekanism tile) {
        return jeiCategories(MekanismJEIRecipeType.findType(tile.getBlockTypeRegistryName()));
    }

    default ELEMENT jeiCrafting() {
        return jeiCategories(MekanismJEIRecipeType.VANILLA_CRAFTING);
    }

    default boolean isMouseOverJEIArea(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }
}