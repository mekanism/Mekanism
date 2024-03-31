package mekanism.common.recipe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;

//TODO - 1.20.4: Somehow represent this recipe in JEI??
@NothingNullByDefault
public class ClearConfigurationRecipe extends CustomRecipe {

    //TODO: Evaluate supporting some of these in some sort of generic way in RecipeUpgradeType?
    private static final Set<Holder<AttachmentType<?>>> CLEARABLE_ATTACHMENTS = Util.make(new HashSet<>(), set -> {
        set.add(MekanismAttachmentTypes.EDIT_MODE);
        set.add(MekanismAttachmentTypes.DUMP_MODE);
        set.add(MekanismAttachmentTypes.SECONDARY_DUMP_MODE);
        set.add(MekanismAttachmentTypes.REDSTONE_CONTROL);
        set.add(MekanismAttachmentTypes.REDSTONE_OUTPUT);
        set.add(MekanismAttachmentTypes.TRANSPORTER_COLOR);
        set.add(MekanismAttachmentTypes.BUCKET_MODE);
        set.add(MekanismAttachmentTypes.ROTARY_MODE);
        set.add(MekanismAttachmentTypes.AUTO);
        set.add(MekanismAttachmentTypes.SORTING);
        set.add(MekanismAttachmentTypes.EJECT);
        set.add(MekanismAttachmentTypes.PULL);
        set.add(MekanismAttachmentTypes.ROUND_ROBIN);
        set.add(MekanismAttachmentTypes.SINGLE_ITEM);
        set.add(MekanismAttachmentTypes.FUZZY);
        set.add(MekanismAttachmentTypes.SILK_TOUCH);
        set.add(MekanismAttachmentTypes.INVERSE);
        set.add(MekanismAttachmentTypes.INVERSE_REQUIRES_REPLACE);
        set.add(MekanismAttachmentTypes.FROM_RECIPE);
        set.add(MekanismAttachmentTypes.INSERT_INTO_FREQUENCY);
        set.add(MekanismAttachmentTypes.RADIUS);
        set.add(MekanismAttachmentTypes.MIN_Y);
        set.add(MekanismAttachmentTypes.MAX_Y);
        set.add(MekanismAttachmentTypes.DELAY);
        set.add(MekanismAttachmentTypes.LONG_AMOUNT);
        set.add(MekanismAttachmentTypes.MIN_THRESHOLD);
        set.add(MekanismAttachmentTypes.MAX_THRESHOLD);
        set.add(MekanismAttachmentTypes.EJECTOR);
        set.add(MekanismAttachmentTypes.SIDE_CONFIG);
        set.add(MekanismAttachmentTypes.REPLACE_STACK);
        set.add(MekanismAttachmentTypes.ITEM_TARGET);
        set.add(MekanismAttachmentTypes.STABILIZER_CHUNKS);
        set.add(MekanismAttachmentTypes.FILTER_AWARE);
        set.add(MekanismAttachmentTypes.CONFIGURATION_DATA);

        set.add(MekanismAttachmentTypes.HEAT_CAPACITORS);
        //TODO: Do we want to clear frequencies?
        //set.add(MekanismAttachmentTypes.FREQUENCY_AWARE);
        //set.add(MekanismAttachmentTypes.FREQUENCY_COMPONENT);
    });

    @SafeVarargs
    public static void addAttachments(Holder<AttachmentType<?>>... attachments) {
        Collections.addAll(CLEARABLE_ATTACHMENTS, attachments);
    }

    public ClearConfigurationRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack target = getTargetStack(container);
        if (target.isEmpty()) {
            //If we didn't find a singular block item our recipe can't possibly match
            return false;
        }
        //Only match the recipe if it has at least one attachment that we can clear
        for (Holder<AttachmentType<?>> clearableAttachment : CLEARABLE_ATTACHMENTS) {
            if (target.hasData(clearableAttachment.value())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack target = getTargetStack(container);
        if (target.isEmpty()) {
            //If we didn't find a singular block item our recipe can't possibly match
            return ItemStack.EMPTY;
        }
        ItemStack output = target.copyWithCount(1);
        //Only match the recipe if it has at least one attachment that we can clear
        for (Holder<AttachmentType<?>> clearableAttachment : CLEARABLE_ATTACHMENTS) {
            output.removeData(clearableAttachment.value());
        }
        return output;
    }

    private ItemStack getTargetStack(CraftingContainer container) {
        ItemStack target = ItemStack.EMPTY;
        //Note: We don't use inv#getItems as that may do unnecessary copies depending on impl
        for (int i = 0, slots = container.getContainerSize(); i < slots; ++i) {
            ItemStack stackInSlot = container.getItem(i);
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.hasAttachments()) {
                    if (!target.isEmpty()) {
                        //If we already have a stack, then this is not a valid recipe match
                        return ItemStack.EMPTY;
                    }
                    target = stackInSlot;
                } else {
                    //We currently only want to target block items that have at least one attachment
                    return ItemStack.EMPTY;
                }
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