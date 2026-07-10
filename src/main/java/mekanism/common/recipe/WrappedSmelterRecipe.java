package mekanism.common.recipe;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.basic.BasicSmeltingRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import net.minecraft.core.TypedInstance;
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
import net.minecraft.world.item.crafting.display.SlotDisplay;

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
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    public ItemStackTemplate getOutput(TypedInstance<Item> input) {
        ItemStack stack = IngredientCreatorAccess.item().createStack(input);
        return ItemStackTemplate.fromNonEmptyStack(wrapped.assemble(new SingleRecipeInput(stack)));
    }

    @Override
    public List<ItemStackTemplate> getOutputDefinition(ContextMap contextMap) {
        List<ItemStackTemplate> list = new ArrayList<>();
        for (RecipeDisplay display : wrapped.display()) {
            for (ItemStack stack : display.result().resolveForStacks(contextMap)) {
                if (!stack.isEmpty()) {//TODO - 26.2: Can resolved stacks ever be empty?
                    list.add(ItemStackTemplate.fromNonEmptyStack(stack));
                }
            }
        }
        return list;
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        List<SlotDisplay> displays = display().stream().map(RecipeDisplay::result).toList();
        if (displays.isEmpty()) {
            return SlotDisplay.Empty.INSTANCE;
        } else if (displays.size() == 1) {
            return displays.getFirst();
        }
        return new SlotDisplay.Composite(displays);
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
