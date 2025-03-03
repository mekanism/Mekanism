package mekanism.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.content.assemblicator.RecipeFormula;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public record FormulaAttachment(List<ItemStack> inventory, boolean invalid) {

    public static final FormulaAttachment EMPTY = new FormulaAttachment(NonNullList.withSize(9, ItemStack.EMPTY), false);

    public static final Codec<FormulaAttachment> CODEC = RecordCodecBuilder.<FormulaAttachment>create(instance -> instance.group(
          SerializerHelper.OPTIONAL_SINGLE_ITEM_CODEC.listOf(9, 9).fieldOf(SerializationConstants.ITEMS).forGetter(FormulaAttachment::inventory),
          Codec.BOOL.optionalFieldOf(SerializationConstants.INVALID, false).forGetter(FormulaAttachment::invalid)
    ).apply(instance, FormulaAttachment::new)).orElse(
          (Consumer<String>) error -> Mekanism.logger.error("Failed to load stored formula: {}", error),
          EMPTY
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FormulaAttachment> STREAM_CODEC = StreamCodec.composite(
          ItemStack.OPTIONAL_LIST_STREAM_CODEC, FormulaAttachment::inventory,
          ByteBufCodecs.BOOL, FormulaAttachment::invalid,
          FormulaAttachment::new
    );

    public FormulaAttachment {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        inventory = Collections.unmodifiableList(inventory);
    }

    public static FormulaAttachment create(RecipeFormula formula) {
        return new FormulaAttachment(formula.getCopy(true), false);
    }

    //TODO - 1.21: I don't think this gets set if in a player's inventory when a reload happens or they rejoin after recipes have changed
    public FormulaAttachment asInvalid() {
        if (invalid) {
            return this;
        }
        //Note: We don't have to copy the inventory as FormulaAttachment is immutable, so nothing should be mutating the backing stacks
        return new FormulaAttachment(inventory, true);
    }

    public boolean isEmpty() {
        if (this == EMPTY) {
            return true;
        }
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    public Stream<ItemStack> nonEmptyItems() {
        if (this == EMPTY) {
            return Stream.empty();
        }
        return inventory.stream().filter(stack -> !stack.isEmpty());
    }

    public boolean hasItems() {
        if (this == EMPTY) {
            return false;
        }
        return inventory.stream().anyMatch(stack -> !stack.isEmpty());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FormulaAttachment other = (FormulaAttachment) o;
        return invalid == other.invalid && ItemStack.listMatches(inventory, other.inventory);
    }

    @Override
    public int hashCode() {
        int hash = ItemStack.hashStackList(inventory);
        return 31 * hash + Boolean.hashCode(invalid);
    }
}