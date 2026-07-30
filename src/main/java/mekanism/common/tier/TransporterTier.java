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
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.Nullable;

public enum TransporterTier implements ITier, TooltipProvider {
    BASIC(BaseTier.BASIC, 1, 5),
    ADVANCED(BaseTier.ADVANCED, 16, 10),
    ELITE(BaseTier.ELITE, 32, 20),
    ULTIMATE(BaseTier.ULTIMATE, 64, 50);

    public static final IntFunction<TransporterTier> BY_ID = ByIdMap.continuous(TransporterTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, TransporterTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, TransporterTier::ordinal);
    public static final Codec<TransporterTier> CODEC = StringRepresentable.fromEnum(TransporterTier::values);

    private final String serializedName;
    private final int basePull;
    private final int baseSpeed;
    private final BaseTier baseTier;
    @Nullable
    private CachedIntValue pullReference;
    @Nullable
    private CachedIntValue speedReference;

    TransporterTier(BaseTier tier, int pull, int speed) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        basePull = pull;
        baseSpeed = speed;
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

    public int getPullAmount() {
        return pullReference == null ? getBasePull() : pullReference.getOrDefault();
    }

    //TODO - 1.21: Figure this out as speed is configured as per half second??
    public int getSpeed() {
        return speedReference == null ? getBaseSpeed() : speedReference.getOrDefault();
    }

    public int getBasePull() {
        return basePull;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        //Ensure no one somehow passes in invalid data
        float tickRate = Math.max(context.tickRate(), TickRateManager.MIN_TICKRATE);
        builder.accept(MekanismLang.SPEED.translateColored(EnumColor.INDIGO, EnumColor.GRAY, UnitDisplayUtils.roundDecimals(getSpeed() / (5 * SharedConstants.TICKS_PER_SECOND / tickRate))));
        builder.accept(MekanismLang.PUMP_RATE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, UnitDisplayUtils.roundDecimals(getPullAmount() * tickRate / MekanismUtils.TICKS_PER_HALF_SECOND)));
        builder.accept(CommonComponents.EMPTY);
        builder.accept(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
        builder.accept(MekanismLang.ITEMS.translateColored(EnumColor.GRAY, EnumColor.PURPLE, MekanismLang.UNIVERSAL));
        builder.accept(MekanismLang.BLOCKS.translateColored(EnumColor.GRAY, EnumColor.PURPLE, MekanismLang.UNIVERSAL));
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the TransporterTier a reference to the actual config value object
    public void setConfigReference(CachedIntValue pullReference, CachedIntValue speedReference) {
        this.pullReference = pullReference;
        this.speedReference = speedReference;
    }
}