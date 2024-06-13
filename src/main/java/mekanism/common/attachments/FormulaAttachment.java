package mekanism.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.content.assemblicator.RecipeFormula;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public record FormulaAttachment(List<ItemStack> inventory, boolean invalid) {//TODO - 1.21: Do we want an empty variant of this?

    public static final Codec<FormulaAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ItemStack.OPTIONAL_CODEC.listOf(9, 9).fieldOf(SerializationConstants.ITEMS).forGetter(FormulaAttachment::inventory),
          Codec.BOOL.fieldOf(SerializationConstants.INVALID).forGetter(FormulaAttachment::invalid)
    ).apply(instance, FormulaAttachment::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FormulaAttachment> STREAM_CODEC = StreamCodec.composite(
          ItemStack.OPTIONAL_LIST_STREAM_CODEC, FormulaAttachment::inventory,
          ByteBufCodecs.BOOL, FormulaAttachment::invalid,
          FormulaAttachment::new
    );

    public FormulaAttachment {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        inventory = Collections.unmodifiableList(inventory);
    }

    public static Optional<FormulaAttachment> existingFormula(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemCraftingFormula) {
            return Optional.ofNullable(stack.get(MekanismDataComponents.FORMULA_HOLDER));
        }
        return Optional.empty();
    }

    public static FormulaAttachment create(RecipeFormula formula) {
        List<ItemStack> stacks = new ArrayList<>(formula.craftingInput.size());
        for (ItemStack stack : formula.craftingInput.items()) {
            stacks.add(stack.copy());
        }
        return new FormulaAttachment(stacks, false);
    }

    public FormulaAttachment asInvalid() {
        if (invalid) {
            return this;
        }
        List<ItemStack> stacks = new ArrayList<>(inventory.size());
        for (ItemStack stack : inventory) {
            stacks.add(stack.copy());
        }
        return new FormulaAttachment(stacks, true);
    }

    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    public boolean hasItems() {
        return inventory.stream().anyMatch(slot -> !slot.isEmpty());
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