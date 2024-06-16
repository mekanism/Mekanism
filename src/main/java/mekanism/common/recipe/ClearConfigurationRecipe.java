package mekanism.common.recipe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

//TODO - 1.21: Somehow represent this recipe in JEI and EMI??
@NothingNullByDefault
public class ClearConfigurationRecipe extends CustomRecipe {

    //TODO: Evaluate supporting some of these in some sort of generic way in RecipeUpgradeType?
    private static final Set<Holder<DataComponentType<?>>> CLEARABLE_ATTACHMENTS = Util.make(new HashSet<>(), set -> {
        set.add(MekanismDataComponents.EDIT_MODE);
        set.add(MekanismDataComponents.DUMP_MODE);
        set.add(MekanismDataComponents.SECONDARY_DUMP_MODE);
        set.add(MekanismDataComponents.REDSTONE_CONTROL);
        set.add(MekanismDataComponents.REDSTONE_OUTPUT);
        set.add(MekanismDataComponents.COLOR);
        set.add(MekanismDataComponents.BUCKET_MODE);
        set.add(MekanismDataComponents.ROTARY_MODE);
        set.add(MekanismDataComponents.AUTO);
        set.add(MekanismDataComponents.SORTING);
        set.add(MekanismDataComponents.EJECT);
        set.add(MekanismDataComponents.PULL);
        set.add(MekanismDataComponents.ROUND_ROBIN);
        set.add(MekanismDataComponents.SINGLE_ITEM);
        set.add(MekanismDataComponents.FUZZY);
        set.add(MekanismDataComponents.SILK_TOUCH);
        set.add(MekanismDataComponents.INVERSE);
        set.add(MekanismDataComponents.INVERSE_REQUIRES_REPLACE);
        set.add(MekanismDataComponents.FROM_RECIPE);
        set.add(MekanismDataComponents.INSERT_INTO_FREQUENCY);
        set.add(MekanismDataComponents.RADIUS);
        set.add(MekanismDataComponents.MIN_Y);
        set.add(MekanismDataComponents.MAX_Y);
        set.add(MekanismDataComponents.DELAY);
        set.add(MekanismDataComponents.LONG_AMOUNT);
        set.add(MekanismDataComponents.MIN_THRESHOLD);
        set.add(MekanismDataComponents.MAX_THRESHOLD);
        set.add(MekanismDataComponents.EJECTOR);
        set.add(MekanismDataComponents.SIDE_CONFIG);
        set.add(MekanismDataComponents.REPLACE_STACK);
        set.add(MekanismDataComponents.ITEM_TARGET);
        set.add(MekanismDataComponents.STABILIZER_CHUNKS);
        set.add(MekanismDataComponents.FILTER_AWARE);
        set.add(MekanismDataComponents.CONFIGURATION_DATA);
        set.add(MekanismDataComponents.FORMULA_HOLDER);

        set.add(MekanismDataComponents.ATTACHED_HEAT);
        //TODO: Do we want to clear frequencies?
        //set.add(MekanismDataComponents.FREQUENCY_AWARE);
        //set.add(MekanismDataComponents.FREQUENCY_COMPONENT);
    });

    @SafeVarargs
    public static void addAttachments(Holder<DataComponentType<?>>... components) {
        Collections.addAll(CLEARABLE_ATTACHMENTS, components);
    }

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
        //Only match the recipe if it has at least one attachment that we can clear
        for (Holder<DataComponentType<?>> clearableAttachment : CLEARABLE_ATTACHMENTS) {
            if (target.has(clearableAttachment.value())) {
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
        //Only match the recipe if it has at least one attachment that we can clear
        for (Holder<DataComponentType<?>> clearableAttachment : CLEARABLE_ATTACHMENTS) {
            resetComponent(output, prototype, clearableAttachment.value());
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