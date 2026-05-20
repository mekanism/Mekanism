package mekanism.common.attachments.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.transfer.item.ItemResource;

@NothingNullByDefault
public record UpgradeAware(Map<Upgrade, Integer> upgrades, LargeResourceStack<ItemResource> inputSlot, LargeResourceStack<ItemResource> outputSlot) {

    public static final UpgradeAware EMPTY = new UpgradeAware(Collections.emptyMap(), LargeResourceStack.ITEM_HELPER.empty(), LargeResourceStack.ITEM_HELPER.empty());
    private static final Set<Upgrade> SUPPORTS_ALL = EnumSet.allOf(Upgrade.class);

    public static final Codec<UpgradeAware> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.unboundedMap(Upgrade.CODEC, ExtraCodecs.POSITIVE_INT).fieldOf(SerializationConstants.UPGRADES).forGetter(UpgradeAware::upgrades),
          LargeResourceStack.ITEM_HELPER.orEmptyCodec().fieldOf(SerializationConstants.INPUT).forGetter(UpgradeAware::inputSlot),
          LargeResourceStack.ITEM_HELPER.orEmptyCodec().fieldOf(SerializationConstants.OUTPUT).forGetter(UpgradeAware::outputSlot)
    ).apply(instance, UpgradeAware::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeAware> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.map(_ -> new EnumMap<>(Upgrade.class), Upgrade.STREAM_CODEC, ByteBufCodecs.VAR_INT), UpgradeAware::upgrades,
          LargeResourceStack.ITEM_HELPER.streamCodec(), UpgradeAware::inputSlot,
          LargeResourceStack.ITEM_HELPER.streamCodec(), UpgradeAware::outputSlot,
          UpgradeAware::new
    );

    public UpgradeAware {
        //Make the map unmodifiable to ensure we don't accidentally mutate it
        upgrades = Collections.unmodifiableMap(upgrades);
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
        input.setContentsUnchecked(inputSlot);
        output.setContentsUnchecked(outputSlot);
        return List.of(input, output);
    }
}