package mekanism.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.assemblicator.RecipeFormula;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record FormulaComponent(List<ItemResource> inventory, boolean invalid) implements TooltipProvider {

    public static final FormulaComponent EMPTY = new FormulaComponent(NonNullList.withSize(9, ItemResource.EMPTY), false);

    public static final Codec<FormulaComponent> CODEC = RecordCodecBuilder.<FormulaComponent>create(instance -> instance.group(
          ItemResource.OPTIONAL_CODEC.listOf(9, 9).fieldOf(SerializationConstants.ITEMS).forGetter(FormulaComponent::inventory),
          Codec.BOOL.optionalFieldOf(SerializationConstants.INVALID, false).forGetter(FormulaComponent::invalid)
    ).apply(instance, FormulaComponent::new)).orElse(
          (Consumer<String>) error -> Mekanism.logger.error("Failed to load stored formula: {}", error),
          EMPTY
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FormulaComponent> STREAM_CODEC = StreamCodec.composite(
          ItemResource.STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)), FormulaComponent::inventory,
          ByteBufCodecs.BOOL, FormulaComponent::invalid,
          FormulaComponent::new
    );

    public FormulaComponent {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        inventory = Collections.unmodifiableList(inventory);
    }

    public static FormulaComponent create(RecipeFormula formula) {
        return new FormulaComponent(formula.getItemTypes(), false);
    }

    //TODO - 1.21: I don't think this gets set if in a player's inventory when a reload happens or they rejoin after recipes have changed
    public FormulaComponent asInvalid() {
        if (invalid) {
            return this;
        }
        //Note: We don't have to copy the inventory as FormulaAttachment is immutable, so nothing should be mutating the backing stacks
        return new FormulaComponent(inventory, true);
    }

    public boolean isEmpty() {
        if (this == EMPTY) {
            return true;
        }
        return inventory.stream().allMatch(ItemResource::isEmpty);
    }

    public boolean hasItems() {
        if (this == EMPTY) {
            return false;
        }
        return inventory.stream().anyMatch(resource -> !resource.isEmpty());
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        if (hasItems()) {
            builder.accept(MekanismLang.INGREDIENTS.translateColored(EnumColor.GRAY));
            Map<ItemResource, Integer> stacks = inventory.stream()
                  .filter(resource -> !resource.isEmpty())
                  .collect(Collectors.toMap(Function.identity(), _ -> 1, Integer::sum, LinkedHashMap::new));
            for (Map.Entry<ItemResource, Integer> entry : stacks.entrySet()) {
                builder.accept(MekanismLang.GENERIC_TRANSFER.translateColored(EnumColor.GRAY, entry.getKey(), entry.getValue()));
            }
        }
    }
}