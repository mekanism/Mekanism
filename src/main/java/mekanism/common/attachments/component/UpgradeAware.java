package mekanism.common.attachments.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public record UpgradeAware(Map<Upgrade, Integer> upgrades, @Nullable ItemStackTemplate inputSlot, @Nullable ItemStackTemplate outputSlot) {

    public static final UpgradeAware EMPTY = new UpgradeAware(Collections.emptyMap(), null, (ItemStackTemplate) null);
    private static final Set<Upgrade> SUPPORTS_ALL = EnumSet.allOf(Upgrade.class);

    public static final Codec<UpgradeAware> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.unboundedMap(Upgrade.CODEC, ExtraCodecs.POSITIVE_INT).fieldOf(SerializationConstants.UPGRADES).forGetter(UpgradeAware::upgrades),
          //TODO - 26.1: Did these being lenient codecs before actually do anything? Add tests
          ItemStackTemplate.CODEC.optionalFieldOf(SerializationConstants.INPUT).forGetter(UpgradeAware::optionalInputSlot),
          ItemStackTemplate.CODEC.optionalFieldOf(SerializationConstants.OUTPUT).forGetter(UpgradeAware::optionalOutputSlot)
    ).apply(instance, UpgradeAware::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeAware> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.map(size -> new EnumMap<>(Upgrade.class), Upgrade.STREAM_CODEC, ByteBufCodecs.VAR_INT), UpgradeAware::upgrades,
          ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), UpgradeAware::optionalInputSlot,
          ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), UpgradeAware::optionalOutputSlot,
          UpgradeAware::new
    );

    public UpgradeAware {
        //Make the map unmodifiable to ensure we don't accidentally mutate it
        upgrades = Collections.unmodifiableMap(upgrades);
    }

    public UpgradeAware(Map<Upgrade, Integer> upgrades, ItemResource inputResource, int inputAmount, ItemResource outputResource, int outputAmount) {
        if (inputResource.isEmpty() != (inputAmount == 0)) {
            throw new IllegalArgumentException("Input amount must be zero for an empty resource");
        } else if (outputResource.isEmpty() != (outputAmount == 0)) {
            throw new IllegalArgumentException("Output amount must be zero for an empty resource");
        }
        this(upgrades, inputResource.isEmpty() ? null : new ItemStackTemplate(inputResource.typeHolder(), inputAmount, inputResource.getComponentsPatch()),
              outputResource.isEmpty() ? null : new ItemStackTemplate(outputResource.typeHolder(), outputAmount, outputResource.getComponentsPatch()));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private UpgradeAware(Map<Upgrade, Integer> upgrades, Optional<ItemStackTemplate> inputSlot, Optional<ItemStackTemplate> outputSlot) {
        this(upgrades, inputSlot.orElse(null), outputSlot.orElse(null));
    }

    public int getUpgradeCount(Upgrade upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    private Optional<ItemStackTemplate> optionalInputSlot() {
        return Optional.ofNullable(inputSlot);
    }

    private Optional<ItemStackTemplate> optionalOutputSlot() {
        return Optional.ofNullable(outputSlot);
    }

    public List<IInventorySlot> asInventorySlots() {
        return asInventorySlots(SUPPORTS_ALL);
    }

    public List<IInventorySlot> asInventorySlots(Set<Upgrade> supportedUpgrades) {
        UpgradeInventorySlot input = UpgradeInventorySlot.input(null, supportedUpgrades);
        UpgradeInventorySlot output = UpgradeInventorySlot.output(null);
        setSlot(input, inputSlot);
        setSlot(output, outputSlot);
        return List.of(input, output);
    }

    public static void setSlot(UpgradeInventorySlot slot, @Nullable ItemStackTemplate template) {
        if (template == null) {
            slot.setEmpty();
        } else {
            slot.setStack(ItemResource.of(template), template.count());
        }
    }
}