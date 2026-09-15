package mekanism.client.recipe_viewer.type;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public record FakeRVRecipeType<RECIPE>(
      Identifier id, @Nullable Identifier icon, @Nullable ItemLike item, IHasTranslationKey name, Class<? extends RECIPE> recipeClass,
      int xOffset, int yOffset, int width, int height, List<SlotDisplay> workstations
) implements IRecipeViewerRecipeType<RECIPE> {

    public FakeRVRecipeType(Identifier id, @Nullable Identifier icon, IHasTranslationKey name, Class<? extends RECIPE> recipeClass,
          int xOffset, int yOffset, int width, int height, ItemLike... altWorkstations) {
        this(id, icon, null, name, recipeClass, xOffset, yOffset, width, height, IRecipeViewerRecipeType.asWorkStations(altWorkstations));
    }

    public FakeRVRecipeType(Identifier id, ItemLike icon, IHasTranslationKey name, Class<? extends RECIPE> recipeClass,
          int xOffset, int yOffset, int width, int height, ItemLike... altWorkstations) {
        this(id, icon, name, recipeClass, xOffset, yOffset, width, height, true, altWorkstations);
    }

    public FakeRVRecipeType(Identifier id, ItemLike icon, IHasTranslationKey name, Class<? extends RECIPE> recipeClass,
          int xOffset, int yOffset, int width, int height, boolean iconIsWorkstation, ItemLike... altWorkstations) {
        List<SlotDisplay> workstations = new ArrayList<>();
        if (iconIsWorkstation) {
            workstations.addAll(IRecipeViewerRecipeType.asWorkStations(icon));
        }
        workstations.addAll(IRecipeViewerRecipeType.asWorkStations(altWorkstations));
        this(id, null, icon, name, recipeClass, xOffset, yOffset, width, height, List.copyOf(workstations));
    }

    public FakeRVRecipeType(BlockRegistryObject<?, ?> item, Class<? extends RECIPE> recipeClass, int xOffset, int yOffset, int width, int height, ItemLike... altWorkstations) {
        this(item.getId(), item, item, recipeClass, xOffset, yOffset, width, height, altWorkstations);
    }

    @Override
    public Component getTextComponent() {
        return TextComponentUtil.build(name);
    }

    @Override
    public boolean requiresHolder() {
        return false;
    }

    @Override
    public ItemStack iconStack() {
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
}