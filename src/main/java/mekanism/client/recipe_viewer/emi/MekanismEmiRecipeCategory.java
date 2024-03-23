package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;
import java.util.Comparator;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class MekanismEmiRecipeCategory extends EmiRecipeCategory {

    public static MekanismEmiRecipeCategory create(IRecipeViewerRecipeType<?> recipeType) {
        ItemStack stack = recipeType.iconStack();
        ResourceLocation icon = recipeType.icon();
        if (stack.isEmpty()) {
            if (icon == null) {
                throw new IllegalStateException("Expected recipe type to have either an icon stack or an icon location");
            }
            return new MekanismEmiRecipeCategory(recipeType, renderIcon(icon));
        }
        if (icon == null) {
            return new MekanismEmiRecipeCategory(recipeType, EmiStack.of(stack));
        }
        return new MekanismEmiRecipeCategory(recipeType, EmiStack.of(stack), renderIcon(icon));
    }

    private static EmiRenderable renderIcon(ResourceLocation icon) {
        return (graphics, x, y, delta) -> graphics.blit(icon, x - 1, y - 1, 0, 0, 18, 18, 18, 18);
    }

    private final IRecipeViewerRecipeType<?> recipeType;

    private MekanismEmiRecipeCategory(IRecipeViewerRecipeType<?> recipeType, EmiRenderable icon) {
        super(recipeType.id(), icon);
        this.recipeType = recipeType;
    }

    //TODO: Decide if we want to pass a simplified icon for any of our types. This basically just allows for having the recipe tree show a simpler non block icon
    public MekanismEmiRecipeCategory(IRecipeViewerRecipeType<?> recipeType, EmiRenderable icon, EmiRenderable simplified) {
        super(recipeType.id(), icon, simplified);
        this.recipeType = recipeType;
    }

    public MekanismEmiRecipeCategory(IRecipeViewerRecipeType<?> recipeType, EmiRenderable icon, EmiRenderable simplified, Comparator<EmiRecipe> sorter) {
        super(recipeType.id(), icon, simplified, sorter);
        this.recipeType = recipeType;
    }

    @Override
    public Component getName() {
        return recipeType.getTextComponent();
    }

    public int xOffset() {
        return recipeType.xOffset();
    }

    public int yOffset() {
        return recipeType.yOffset();
    }

    public int width() {
        return recipeType.width();
    }

    public int height() {
        return recipeType.height();
    }
}