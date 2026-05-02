package mekanism.common.recipe;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.basic.BasicSmeltingRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

@NothingNullByDefault
public class WrappedSmelterRecipe extends ItemStackToItemStackRecipe {
    public static ItemStackToItemStackRecipe tryUnwrap(SmeltingRecipe original) {
        ItemStackIngredient input = IngredientCreatorAccess.item().from(original.input());
        if (original.getClass() == SmeltingRecipe.class) {
            //this is only valid on vanilla recipes as subclasses could override it or use other fields
            //method is normally protected
            ItemStackTemplate vanillaOutput = original.result();
            return new BasicSmeltingRecipe(input, vanillaOutput);
        }
        return new WrappedSmelterRecipe(original);
    }

    private final SmeltingRecipe wrapped;
    private final ItemStackIngredient input;

    private WrappedSmelterRecipe(SmeltingRecipe wrapped) {
        super(MekanismRecipeTypes.TYPE_SMELTING.value());
        this.wrapped = wrapped;
        input = IngredientCreatorAccess.item().from(wrapped.input());
    }

    @Override
    public boolean test(ItemStack input) {
        return this.input.test(input);
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    public <INPUT extends TypedInstance<Item> & DataComponentHolder> ItemStackTemplate getOutput(INPUT input) {
        ItemStack stack = IngredientCreatorAccess.createItemStack(input);
        return ItemStackTemplate.fromNonEmptyStack(wrapped.assemble(new SingleRecipeInput(stack)));
    }

    @Override
    public List<ItemStack> getOutputDefinition() {
        List<ItemStack> list = new ArrayList<>();
        for (RecipeDisplay display : wrapped.display()) {
            //TODO - 26.1 can we get a level here and use SlotDisplayContext.fromLevel()?
            list.addAll(display.result().resolveForStacks(ContextMap.EMPTY));
        }
        return list;
    }

    @Override
    public List<RecipeDisplay> display() {
        return wrapped.display();
    }

    public SmeltingRecipe getWrapped() {
        return wrapped;
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return MekanismRecipeSerializersInternal.WRAPPED_SMELTER.value();
    }

    static MapCodec<WrappedSmelterRecipe> CODEC = SmeltingRecipe.MAP_CODEC.xmap(WrappedSmelterRecipe::new, WrappedSmelterRecipe::getWrapped);
    static StreamCodec<RegistryFriendlyByteBuf, WrappedSmelterRecipe> STREAM_CODEC = SmeltingRecipe.STREAM_CODEC.map(WrappedSmelterRecipe::new, WrappedSmelterRecipe::getWrapped);
    public static RecipeSerializer<WrappedSmelterRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
