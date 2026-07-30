package mekanism.common.tier;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.tier.BaseTier;
import mekanism.common.MekanismLang;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.util.StorageUtils;
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

//TODO - 26.2: Do we want to up the default config limits for any of these tiers?
public enum BinTier implements IStorageTier, TooltipProvider {
    BASIC(BaseTier.BASIC, 4_096),
    ADVANCED(BaseTier.ADVANCED, 8_192),
    ELITE(BaseTier.ELITE, 32_768),
    ULTIMATE(BaseTier.ULTIMATE, 262_144),
    CREATIVE(BaseTier.CREATIVE, Long.MAX_VALUE);

    public static final IntFunction<BinTier> BY_ID = ByIdMap.continuous(BinTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, BinTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, BinTier::ordinal);
    public static final Codec<BinTier> CODEC = StringRepresentable.fromEnum(BinTier::values);

    private final String serializedName;
    private final long baseCapacity;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue capacityReference;

    BinTier(BaseTier tier, long capacity) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        baseTier = tier;
        baseCapacity = capacity;
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
    public int getTransferRate() {
        //TODO - 26.2: Do we want to set a transfer rate here?
        return Integer.MAX_VALUE;
    }

    @Override
    public long getCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.getOrDefault();
    }

    public long getBaseCapacity() {
        return baseCapacity;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        StorageUtils.addCapacity(builder, this, MekanismLang.CAPACITY_ITEMS);
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the BinTier a reference to the actual config value object
    public void setConfigReference(CachedLongValue capacityReference) {
        this.capacityReference = capacityReference;
    }
}