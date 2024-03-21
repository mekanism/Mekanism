package mekanism.client.recipe_viewer.type;

import java.util.List;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IRecipeViewerRecipeType<RECIPE> extends IHasTextComponent {

    ResourceLocation id();

    Class<? extends RECIPE> recipeClass();

    boolean requiresHolder();

    ItemStack iconStack();

    @Nullable
    ResourceLocation icon();

    //TODO - 1.20.4: Docs how these are to get the thing back into the same frame of reference as the GUI so that we can copy gui elements more directly
    int xOffset();

    int yOffset();

    int width();

    int height();

    List<IItemProvider> workstations();
}