package mekanism.client.recipe_viewer.type;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public record FakeRVRecipeType<RECIPE>(
      ResourceLocation id, @Nullable ResourceLocation icon, @Nullable IItemProvider item, IHasTranslationKey name, Class<? extends RECIPE> recipeClass,
      int xOffset, int yOffset, int width, int height, List<IItemProvider> workstations
) implements IRecipeViewerRecipeType<RECIPE> {

    public FakeRVRecipeType(ResourceLocation id, @Nullable ResourceLocation icon, IHasTranslationKey name, Class<? extends RECIPE> recipeClass,
          int xOffset, int yOffset, int width, int height, IItemProvider... altWorkstations) {
        this(id, icon, null, name, recipeClass, xOffset, yOffset, width, height, List.of(altWorkstations));
    }

    public FakeRVRecipeType(ResourceLocation id, IItemProvider icon, IHasTranslationKey name, Class<? extends RECIPE> recipeClass,
          int xOffset, int yOffset, int width, int height, IItemProvider... altWorkstations) {
        this(id, icon, name, recipeClass, xOffset, yOffset, width, height, true, altWorkstations);
    }

    public FakeRVRecipeType(ResourceLocation id, IItemProvider icon, IHasTranslationKey name, Class<? extends RECIPE> recipeClass,
          int xOffset, int yOffset, int width, int height, boolean iconIsWorkstation, IItemProvider... altWorkstations) {
        this(id, null, icon, name, recipeClass, xOffset, yOffset, width, height,
              iconIsWorkstation ? Stream.concat(Stream.of(icon), Arrays.stream(altWorkstations)).toList() : List.of(altWorkstations));
    }

    public FakeRVRecipeType(BlockRegistryObject<?, ?> item, Class<? extends RECIPE> recipeClass, int xOffset, int yOffset, int width, int height, IItemProvider... altWorkstations) {
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
        return item == null ? ItemStack.EMPTY : item.getItemStack();
    }
}