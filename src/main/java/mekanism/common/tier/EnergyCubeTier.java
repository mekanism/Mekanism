package mekanism.common.tier;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.common.MekanismLang;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.Nullable;

public enum EnergyCubeTier implements IStorageTier, TooltipProvider {
    BASIC(BaseTier.BASIC, 4_000_000L, 4_000),
    ADVANCED(BaseTier.ADVANCED, 16_000_000L, 16_000),
    ELITE(BaseTier.ELITE, 64_000_000L, 64_000),
    ULTIMATE(BaseTier.ULTIMATE, 256_000_000L, 256_000),
    CREATIVE(BaseTier.CREATIVE, Long.MAX_VALUE, Integer.MAX_VALUE);

    public static final IntFunction<EnergyCubeTier> BY_ID = ByIdMap.continuous(EnergyCubeTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, EnergyCubeTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, EnergyCubeTier::ordinal);
    public static final Codec<EnergyCubeTier> CODEC = StringRepresentable.fromEnum(EnergyCubeTier::values);

    private final String serializedName;
    private final long baseCapacity;
    private final int baseTransferRate;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue capacityReference;
    @Nullable
    private CachedIntValue transferRateReference;

    EnergyCubeTier(BaseTier tier, long capacity, int transferRate) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        baseCapacity = capacity;
        baseTransferRate = transferRate;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    @Override
    public long getCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.getOrDefault();
    }

    @Override
    public int getTransferRate() {
        return transferRateReference == null ? getBaseTransferRate() : transferRateReference.getOrDefault();
    }

    public long getBaseCapacity() {
        return baseCapacity;
    }

    public int getBaseTransferRate() {
        return baseTransferRate;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        builder.accept(MekanismLang.CAPACITY.translateColored(getBaseTier().getTextColor(), EnumColor.GRAY, EnergyDisplay.of(getCapacity())));
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the EnergyCubeTier a reference to the actual config value object
    public void setConfigReference(CachedLongValue capacityReference, CachedIntValue transferRateReference) {
        this.capacityReference = capacityReference;
        this.transferRateReference = transferRateReference;
    }
}