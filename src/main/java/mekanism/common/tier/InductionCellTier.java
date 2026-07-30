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

public enum InductionCellTier implements ITier, TooltipProvider {
    BASIC(BaseTier.BASIC, 8_000_000_000L),
    ADVANCED(BaseTier.ADVANCED, 64_000_000_000L),
    ELITE(BaseTier.ELITE, 512_000_000_000L),
    ULTIMATE(BaseTier.ULTIMATE, 4_000_000_000_000L);

    public static final IntFunction<InductionCellTier> BY_ID = ByIdMap.continuous(InductionCellTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, InductionCellTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, InductionCellTier::ordinal);
    public static final Codec<InductionCellTier> CODEC = StringRepresentable.fromEnum(InductionCellTier::values);

    private final String serializedName;
    private final long baseMaxEnergy;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue storageReference;

    InductionCellTier(BaseTier tier, long max) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        baseMaxEnergy = max;
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

    public long getMaxEnergy() {
        return storageReference == null ? getBaseMaxEnergy() : storageReference.getOrDefault();
    }

    public long getBaseMaxEnergy() {
        return baseMaxEnergy;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        builder.accept(MekanismLang.CAPACITY.translateColored(getBaseTier().getTextColor(), EnumColor.GRAY, EnergyDisplay.of(getMaxEnergy())));
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the InductionCellTier a reference to the actual config value object
    public void setConfigReference(CachedLongValue storageReference) {
        this.storageReference = storageReference;
    }
}