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
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.Nullable;

public enum InductionProviderTier implements ITier, TooltipProvider {
    BASIC(BaseTier.BASIC, 256_000L),
    ADVANCED(BaseTier.ADVANCED, 2_048_000L),
    ELITE(BaseTier.ELITE, 16_384_000L),
    ULTIMATE(BaseTier.ULTIMATE, 131_072_000L);

    public static final IntFunction<InductionProviderTier> BY_ID = ByIdMap.continuous(InductionProviderTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, InductionProviderTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, InductionProviderTier::ordinal);
    public static final Codec<InductionProviderTier> CODEC = StringRepresentable.fromEnum(InductionProviderTier::values);

    private final String serializedName;
    private final long baseOutput;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue outputReference;

    InductionProviderTier(BaseTier tier, long output) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        baseOutput = output;
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

    public long getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.getOrDefault();
    }

    public long getBaseOutput() {
        return baseOutput;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        builder.accept(MekanismLang.INDUCTION_PORT_OUTPUT_RATE.translateColored(getBaseTier().getTextColor(), EnumColor.GRAY, EnergyDisplay.of(getOutput())));
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the InductionProviderTier a reference to the actual config value object
    public void setConfigReference(CachedLongValue outputReference) {
        this.outputReference = outputReference;
    }
}