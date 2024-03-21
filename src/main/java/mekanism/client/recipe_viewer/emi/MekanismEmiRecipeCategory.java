package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class MekanismEmiRecipeCategory extends EmiRecipeCategory {

    public static MekanismEmiRecipeCategory create(IRecipeViewerRecipeType<?> recipeType) {
        ItemStack stack = recipeType.iconStack();
        if (stack.isEmpty()) {
            ResourceLocation icon = recipeType.icon();
            if (icon == null) {
                throw new IllegalStateException("Expected recipe type to have either an icon stack or an icon location");
            }
            return new MekanismEmiRecipeCategory(recipeType, (graphics, x, y, delta) -> graphics.blit(icon, x - 1, y - 1, 0, 0, 18, 18, 18, 18));
        }
        return new MekanismEmiRecipeCategory(recipeType, EmiStack.of(stack));
    }

    private final IRecipeViewerRecipeType<?> recipeType;

    private MekanismEmiRecipeCategory(IRecipeViewerRecipeType<?> recipeType, EmiRenderable icon) {
        super(recipeType.id(), icon);
        this.recipeType = recipeType;
    }

    //TODO - 1.20.4: Do we want to use one of the other constructors?
    /*public MekanismEmiRecipeCategory(ResourceLocation id, EmiRenderable icon, EmiRenderable simplified) {
        super(id, icon, simplified);
    }

    public MekanismEmiRecipeCategory(ResourceLocation id, EmiRenderable icon, EmiRenderable simplified, Comparator<EmiRecipe> sorter) {
        super(id, icon, simplified, sorter);
    }*/

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