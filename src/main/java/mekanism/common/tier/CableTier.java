package mekanism.common.tier;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.MekanismLang;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.util.text.EnergyDisplay;
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
import org.jspecify.annotations.Nullable;

public enum CableTier implements ITier, TooltipProvider {
    BASIC(BaseTier.BASIC, 8_000L),
    ADVANCED(BaseTier.ADVANCED, 128_000L),
    ELITE(BaseTier.ELITE, 1_024_000L),
    ULTIMATE(BaseTier.ULTIMATE, 8_192_000L);

    public static final IntFunction<CableTier> BY_ID = ByIdMap.continuous(CableTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, CableTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CableTier::ordinal);
    public static final Codec<CableTier> CODEC = StringRepresentable.fromEnum(CableTier::values);

    private final String serializedName;
    private final long baseCapacity;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue capacityReference;

    CableTier(BaseTier tier, long capacity) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        baseCapacity = capacity;
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

    public long getCableCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.getOrDefault();
    }

    public long getBaseCapacity() {
        return baseCapacity;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        builder.accept(MekanismLang.CAPACITY_PER_TICK.translateColored(EnumColor.INDIGO, EnumColor.GRAY, EnergyDisplay.of(getCableCapacity())));
        builder.accept(CommonComponents.EMPTY);
        builder.accept(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
        builder.accept(MekanismLang.GENERIC_TRANSFER.translateColored(EnumColor.GRAY, MekanismLang.ENERGY_FORGE_SHORT, EnumColor.PURPLE, MekanismLang.FORGE));
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the CableTier a reference to the actual config value object
    public void setConfigReference(CachedLongValue capacityReference) {
        this.capacityReference = capacityReference;
    }
}