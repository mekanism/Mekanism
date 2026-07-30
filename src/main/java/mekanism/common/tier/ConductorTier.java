package mekanism.common.tier;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.heat.HeatAPI;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.MekanismLang;
import mekanism.common.config.value.CachedDoubleValue;
import mekanism.common.lib.Color;
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

public enum ConductorTier implements ITier, TooltipProvider {
    BASIC(BaseTier.BASIC, 5, HeatAPI.DEFAULT_HEAT_CAPACITY, 10, Color.rgbad(0.2, 0.2, 0.2, 1)),
    ADVANCED(BaseTier.ADVANCED, 5, HeatAPI.DEFAULT_HEAT_CAPACITY, 400, Color.rgbad(0.2, 0.2, 0.2, 1)),
    ELITE(BaseTier.ELITE, 5, HeatAPI.DEFAULT_HEAT_CAPACITY, 8_000, Color.rgbad(0.2, 0.2, 0.2, 1)),
    ULTIMATE(BaseTier.ULTIMATE, 5, HeatAPI.DEFAULT_HEAT_CAPACITY, 100_000, Color.rgbad(0.2, 0.2, 0.2, 1));

    public static final IntFunction<ConductorTier> BY_ID = ByIdMap.continuous(ConductorTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, ConductorTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ConductorTier::ordinal);
    public static final Codec<ConductorTier> CODEC = StringRepresentable.fromEnum(ConductorTier::values);

    private final String serializedName;
    private final Color baseColor;
    private final double baseConduction;
    private final double baseHeatCapacity;
    private final double baseConductionInsulation;
    private final BaseTier baseTier;
    @Nullable
    private CachedDoubleValue conductionReference;
    @Nullable
    private CachedDoubleValue capacityReference;
    @Nullable
    private CachedDoubleValue insulationReference;

    ConductorTier(BaseTier tier, double conduction, double heatCapacity, double conductionInsulation, Color color) {
        this.serializedName = name().toLowerCase(Locale.ROOT);
        baseConduction = conduction;
        baseHeatCapacity = heatCapacity;
        baseConductionInsulation = conductionInsulation;

        baseColor = color;
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

    public double getInverseConduction() {
        return conductionReference == null ? getBaseConduction() : conductionReference.getOrDefault();
    }

    public double getInverseConductionInsulation() {
        return insulationReference == null ? getBaseConductionInsulation() : insulationReference.getOrDefault();
    }

    public double getHeatCapacity() {
        return capacityReference == null ? getBaseHeatCapacity() : capacityReference.getOrDefault();
    }

    public Color getBaseColor() {
        return baseColor;
    }

    public double getBaseConduction() {
        return baseConduction;
    }

    public double getBaseHeatCapacity() {
        return baseHeatCapacity;
    }

    public double getBaseConductionInsulation() {
        return baseConductionInsulation;
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
        builder.accept(MekanismLang.CONDUCTION.translateColored(EnumColor.INDIGO, EnumColor.GRAY, getInverseConduction()));
        builder.accept(MekanismLang.INSULATION.translateColored(EnumColor.INDIGO, EnumColor.GRAY, getInverseConductionInsulation()));
        builder.accept(MekanismLang.HEAT_CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, getHeatCapacity()));
        builder.accept(CommonComponents.EMPTY);
        builder.accept(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
        builder.accept(MekanismLang.HEAT.translateColored(EnumColor.GRAY, EnumColor.PURPLE, MekanismLang.MEKANISM));
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the ConductorTier a reference to the actual config value object
    public void setConfigReference(CachedDoubleValue conductionReference, CachedDoubleValue capacityReference, CachedDoubleValue insulationReference) {
        this.conductionReference = conductionReference;
        this.capacityReference = capacityReference;
        this.insulationReference = insulationReference;
    }
}