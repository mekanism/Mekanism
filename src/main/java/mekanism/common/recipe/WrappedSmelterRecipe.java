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
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.TypedInstance;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe.CookingBookInfo;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class WrappedSmelterRecipe extends ItemStackToItemStackRecipe<CookingBookCategory, CookingBookInfo> {

    public static ItemStackToItemStackRecipe<CookingBookCategory, CookingBookInfo> tryUnwrap(SmeltingRecipe original) {
        ItemStackIngredient input = IngredientCreatorAccess.item().from(original.input());
        if (original.getClass() == SmeltingRecipe.class) {
            //this is only valid on vanilla recipes as subclasses could override it or use other fields
            //method is normally protected
            return new BasicSmeltingRecipe(original.commonInfo, original.bookInfo, input, original.result());
        }
        return new WrappedSmelterRecipe(original);
    }

    private final SmeltingRecipe wrapped;
    private final ItemStackIngredient input;

    private WrappedSmelterRecipe(SmeltingRecipe wrapped) {
        this.wrapped = wrapped;
        this.input = IngredientCreatorAccess.item().from(wrapped.input());
        //In case the methods are overridden, we have to recreate the common info and book info here, instead of just passing the normally private fields
        super(MekanismRecipeTypes.TYPE_SMELTING.value(), RecipeBuilder.createCraftingCommonInfo(wrapped.showNotification()), new CookingBookInfo(
              wrapped.category(),
              wrapped.group()
        ));
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
                //Note: Theoretically there is no reason this should be empty, as an empty stream could just be returned instead
                // but just in case a mod is doing something dumb, handle it rather than crashing
                if (!stack.isEmpty()) {
                    list.add(ItemStackTemplate.fromNonEmptyStack(stack));
                }
            }
        }
        return list;
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return MekanismUtils.compactDisplay(display().stream().map(RecipeDisplay::result).toList());
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

    private static MapCodec<WrappedSmelterRecipe> CODEC = SmeltingRecipe.MAP_CODEC.xmap(WrappedSmelterRecipe::new, WrappedSmelterRecipe::getWrapped);
    private static StreamCodec<RegistryFriendlyByteBuf, WrappedSmelterRecipe> STREAM_CODEC = SmeltingRecipe.STREAM_CODEC.map(WrappedSmelterRecipe::new, WrappedSmelterRecipe::getWrapped);
    public static RecipeSerializer<WrappedSmelterRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
