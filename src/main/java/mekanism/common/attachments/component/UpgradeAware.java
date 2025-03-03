package mekanism.common.attachments.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public record UpgradeAware(Map<Upgrade, Integer> upgrades, ItemStack inputSlot, ItemStack outputSlot) {

    public static final UpgradeAware EMPTY = new UpgradeAware(Collections.emptyMap(), ItemStack.EMPTY, ItemStack.EMPTY);
    private static final Set<Upgrade> SUPPORTS_ALL = EnumSet.allOf(Upgrade.class);

    public static final Codec<UpgradeAware> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.unboundedMap(Upgrade.CODEC, ExtraCodecs.POSITIVE_INT).fieldOf(SerializationConstants.UPGRADES).forGetter(UpgradeAware::upgrades),
          SerializerHelper.LENIENT_OPTIONAL_STACK_CODEC.fieldOf(SerializationConstants.INPUT).forGetter(UpgradeAware::inputSlot),
          SerializerHelper.LENIENT_OPTIONAL_STACK_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(UpgradeAware::outputSlot)
    ).apply(instance, UpgradeAware::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeAware> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.map(size -> new EnumMap<>(Upgrade.class), Upgrade.STREAM_CODEC, ByteBufCodecs.VAR_INT), UpgradeAware::upgrades,
          ItemStack.OPTIONAL_STREAM_CODEC, UpgradeAware::inputSlot,
          ItemStack.OPTIONAL_STREAM_CODEC, UpgradeAware::outputSlot,
          UpgradeAware::new
    );

    public UpgradeAware {
        //Make the map unmodifiable to ensure we don't accidentally mutate it
        upgrades = Collections.unmodifiableMap(upgrades);
        //TODO - 1.21: For things like this if we have any that copy, we may want to make it keep existing stack or list instances
        // rather than wrapping them an extra time. And instead we can just pass in safe stacks, as data components are immutable
        // so we shouldn't be mutating our stacks regardless
        inputSlot = inputSlot.copy();
        outputSlot = outputSlot.copy();
    }

    public int getUpgradeCount(Upgrade upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    public List<IInventorySlot> asInventorySlots() {
        return asInventorySlots(SUPPORTS_ALL);
    }

    public List<IInventorySlot> asInventorySlots(Set<Upgrade> supportedUpgrades) {
        UpgradeInventorySlot input = UpgradeInventorySlot.input(null, supportedUpgrades);
        UpgradeInventorySlot output = UpgradeInventorySlot.output(null);
        //Note: The setStack calls will cause a copy to happen to the stacks
        input.setStack(inputSlot);
        output.setStack(outputSlot);
        return List.of(input, output);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpgradeAware other = (UpgradeAware) o;
        return ItemStack.matches(inputSlot, other.inputSlot) && ItemStack.matches(outputSlot, other.outputSlot) && Objects.equals(upgrades, other.upgrades);
    }

    @Override
    public int hashCode() {
        int hash = upgrades.hashCode();
        hash = 31 * hash + ItemStack.hashItemAndComponents(inputSlot);
        hash = 31 * hash + inputSlot.getCount();
        hash = 31 * hash + ItemStack.hashItemAndComponents(outputSlot);
        hash = 31 * hash + outputSlot.getCount();
        return hash;
    }
}