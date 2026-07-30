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
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.CommonComponents;
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

public enum PipeTier implements IStorageTier, TooltipProvider {//TODO - 26.2: Do we want to change capacities to match chemicals?
    BASIC(BaseTier.BASIC, 2L * FluidType.BUCKET_VOLUME, FluidType.BUCKET_VOLUME / 4),
    ADVANCED(BaseTier.ADVANCED, 8L * FluidType.BUCKET_VOLUME, FluidType.BUCKET_VOLUME),
    ELITE(BaseTier.ELITE, 32L * FluidType.BUCKET_VOLUME, 8 * FluidType.BUCKET_VOLUME),
    ULTIMATE(BaseTier.ULTIMATE, 128L * FluidType.BUCKET_VOLUME, 32 * FluidType.BUCKET_VOLUME);

    public static final IntFunction<PipeTier> BY_ID = ByIdMap.continuous(PipeTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, PipeTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, PipeTier::ordinal);
    public static final Codec<PipeTier> CODEC = StringRepresentable.fromEnum(PipeTier::values);

    private final String serializedName;
    private final long baseCapacity;
    private final int baseTransferRate;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue capacityReference;
    @Nullable
    private CachedIntValue transferRateReference;

    PipeTier(BaseTier tier, long capacity, int transferRate) {
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
        builder.accept(MekanismLang.CAPACITY_MB_PER_TICK.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(getCapacity())));
        builder.accept(MekanismLang.PUMP_RATE_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(getTransferRate())));
        builder.accept(CommonComponents.EMPTY);
        builder.accept(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
        builder.accept(MekanismLang.FLUIDS.translateColored(EnumColor.GRAY, EnumColor.PURPLE, MekanismLang.FORGE));
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the PipeTier a reference to the actual config value object
    public void setConfigReference(CachedLongValue capacityReference, CachedIntValue transferRateReference) {
        this.capacityReference = capacityReference;
        this.transferRateReference = transferRateReference;
    }
}