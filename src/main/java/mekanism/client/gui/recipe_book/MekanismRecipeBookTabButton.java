package mekanism.client.gui.recipe_book;

import mekanism.api.chemical.ChemicalResource;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.common.registration.impl.RecipeBookCategoryDeferredRegister.MekanismRecipeBookCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent.TabInfo;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

public class MekanismRecipeBookTabButton extends RecipeBookTabButton {

    private final Object primaryIcon;
    @Nullable
    private final Object secondaryIcon;

    public MekanismRecipeBookTabButton(int x, int y, TabInfo tabInfo, Button.OnPress onPress) {
        super(x, y, tabInfo, onPress);
        if (this.tabInfo.category() instanceof MekanismRecipeBookCategory mekanismBookCategory) {
            RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
            this.primaryIcon = mekanismBookCategory.category.getPrimaryIcon(registryAccess);
            this.secondaryIcon = mekanismBookCategory.category.getSecondaryIcon(registryAccess).orElse(null);
        } else {
            this.primaryIcon = this.tabInfo.primaryIcon();
            this.secondaryIcon = this.tabInfo.secondaryIcon().orElse(null);
        }
    }

    @Override
    protected void extractIcon(GuiGraphicsExtractor graphics) {
        int moveLeft = this.selected ? -2 : 0;
        //Render second first so that if it is a sprite it renders behind the primary
        if (secondaryIcon != null) {
            extractIcon(graphics, getX() + 14 + moveLeft, getY() + 5, secondaryIcon);
        }
        extractPrimaryIcon(graphics, moveLeft);
    }

    private void extractPrimaryIcon(GuiGraphicsExtractor graphics, int moveLeft) {
        int x = getX() + moveLeft + (secondaryIcon == null ? 9 : 3);
        extractIcon(graphics, x, getY() + 5, primaryIcon);
    }

    private void extractIcon(GuiGraphicsExtractor graphics, int x, int y, Object object) {
        switch (object) {
            case ItemStack stack -> graphics.fakeItem(stack, x, y);
            case FluidResource type -> extractSprite(graphics, x, y, MekanismRenderer.getFluidTexture(type, FluidTextureType.STILL), MekanismRenderer.getColorARGB(type));
            case ChemicalResource type -> extractSprite(graphics, x, y, MekanismRenderer.getChemicalTexture(type), MekanismRenderer.color(type));
            default -> {
            }
        }
    }

    private void extractSprite(GuiGraphicsExtractor graphics, int x, int y, TextureAtlasSprite sprite, int tint) {
        GuiUtils.drawTiledSprite(graphics, x, y, 16, 16, sprite, TilingDirection.UP_RIGHT, tint);
    }
}