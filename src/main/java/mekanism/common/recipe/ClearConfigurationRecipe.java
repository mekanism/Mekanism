package mekanism.common.recipe;

import java.util.Map;
import java.util.Optional;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

//TODO: Somehow represent this recipe in JEI and EMI??
@NothingNullByDefault
public class ClearConfigurationRecipe extends CustomRecipe {

    public ClearConfigurationRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack target = getTargetStack(input);
        if (target.isEmpty()) {
            //If we didn't find a singular block item our recipe can't possibly match
            return false;
        }
        //Only match the recipe if it has at least one data component that we can clear
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : target.getComponentsPatch().entrySet()) {
            if (BuiltInRegistries.DATA_COMPONENT_TYPE.wrapAsHolder(entry.getKey()).is(MekanismTags.DataComponents.CLEARABLE_CONFIG)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        ItemStack target = getTargetStack(input);
        if (target.isEmpty()) {
            //If we didn't find a singular block item our recipe can't possibly match
            return ItemStack.EMPTY;
        }
        ItemStack output = target.copyWithCount(1);
        DataComponentMap prototype = output.getPrototype();
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : output.getComponentsPatch().entrySet()) {
            DataComponentType<?> component = entry.getKey();
            Holder<DataComponentType<?>> componentHolder = BuiltInRegistries.DATA_COMPONENT_TYPE.wrapAsHolder(component);
            if (componentHolder.is(MekanismTags.DataComponents.CLEARABLE_CONFIG)) {
                resetComponent(output, prototype, component);
            }
        }
        return output;
    }

    private <TYPE> void resetComponent(ItemStack output, DataComponentMap prototype, DataComponentType<TYPE> componentType) {
        if (prototype.has(componentType)) {
            output.set(componentType, prototype.get(componentType));
        } else {
            output.remove(componentType);
        }
    }

    private ItemStack getTargetStack(CraftingInput input) {
        ItemStack target = ItemStack.EMPTY;
        //Note: We don't use inv#getItems as that may do unnecessary copies depending on impl
        for (int i = 0, slots = input.size(); i < slots; ++i) {
            ItemStack stackInSlot = input.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.isComponentsPatchEmpty()) {
                    //We currently only want to target block items that have at least one component
                    return ItemStack.EMPTY;
                }
                if (!target.isEmpty()) {
                    //If we already have a stack, then this is not a valid recipe match
                    return ItemStack.EMPTY;
                }
                target = stackInSlot;
            }
        }
        return target;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height > 0;
    }

    @Override
    public boolean isIncomplete() {
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MekanismRecipeSerializersInternal.CLEAR_CONFIGURATION.get();
    }
}