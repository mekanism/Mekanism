package mekanism.common.tier;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.tier.BaseTier;
import mekanism.common.MekanismLang;
import mekanism.common.config.value.CachedIntValue;
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
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

public enum ChemicalTankTier implements IStorageTier, TooltipProvider {
    BASIC(BaseTier.BASIC, 64L * FluidType.BUCKET_VOLUME, FluidType.BUCKET_VOLUME),
    ADVANCED(BaseTier.ADVANCED, 256L * FluidType.BUCKET_VOLUME, 16 * FluidType.BUCKET_VOLUME),
    ELITE(BaseTier.ELITE, 1_024L * FluidType.BUCKET_VOLUME, 128 * FluidType.BUCKET_VOLUME),
    ULTIMATE(BaseTier.ULTIMATE, 8_192L * FluidType.BUCKET_VOLUME, 512 * FluidType.BUCKET_VOLUME),
    CREATIVE(BaseTier.CREATIVE, Long.MAX_VALUE, Integer.MAX_VALUE);

    public static final IntFunction<ChemicalTankTier> BY_ID = ByIdMap.continuous(ChemicalTankTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, ChemicalTankTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ChemicalTankTier::ordinal);
    public static final Codec<ChemicalTankTier> CODEC = StringRepresentable.fromEnum(ChemicalTankTier::values);

    private final String serializedName;
    private final long baseCapacity;
    private final int baseTransferRate;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue capacityReference;
    @Nullable
    private CachedIntValue transferRateReference;

    ChemicalTankTier(BaseTier tier, long capacity, int transferRate) {
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
        StorageUtils.addCapacity(builder, this, MekanismLang.CAPACITY_MB);
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the GasTankTier a reference to the actual config value object
    public void setConfigReference(CachedLongValue capacityReference, CachedIntValue transferRateReference) {
        this.capacityReference = capacityReference;
        this.transferRateReference = transferRateReference;
    }
}