package mekanism.api.datamaps.chemical.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ITooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * A {@link MekanismAPI#CHEMICAL_REGISTRY chemical} data map that allows defining fuel values for a chemical.
 *
 * @param burnTicks     The number of ticks one mB of fuel can be burned for before being depleted; must be greater than zero.
 * @param energyPerTick The energy produced per tick from one mB of fuel; must be greater than zero.
 *
 * @since 10.7.11
 */
public record ChemicalFuel(int burnTicks, long energyPerTick) implements IChemicalAttribute {

    /**
     * The ID of the data map.
     *
     * @see mekanism.api.datamaps.IMekanismDataMapTypes#chemicalFuel()
     */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_attribute_fuel");

    /**
     * Codec for serializing and deserializing chemical fuel.
     */
    public static final Codec<ChemicalFuel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.BURN_TIME).forGetter(ChemicalFuel::burnTicks),
          SerializerHelper.POSITIVE_NONZERO_LONG_CODEC.fieldOf(SerializationConstants.ENERGY).forGetter(ChemicalFuel::energyPerTick)
    ).apply(instance, ChemicalFuel::new));

    public ChemicalFuel {
        if (burnTicks < 1) {
            throw new IllegalArgumentException("Fuel attributes must burn for at least one tick! Burn Ticks: " + burnTicks);
        } else if (energyPerTick < 1) {
            throw new IllegalArgumentException("Fuel attributes must have a per tick energy density greater than zero!");
        }
    }

    /**
     * The energy density in one mB of fuel.
     */
    public long energyDensity() {
        return energyPerTick * burnTicks;
    }

    @Override
    public void collectTooltips(TooltipContext context, List<Component> tooltips, TooltipFlag tooltipFlag) {
        ITooltipHelper tooltipHelper = ITooltipHelper.INSTANCE;
        tooltips.add(APILang.CHEMICAL_ATTRIBUTE_FUEL_BURN_TICKS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, tooltipHelper.getFormattedNumber(burnTicks)));
        tooltips.add(APILang.CHEMICAL_ATTRIBUTE_FUEL_ENERGY_DENSITY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, tooltipHelper.getEnergyPerMBDisplayShort(energyDensity())));
    }

    @Internal
    @Override
    @SuppressWarnings("removal")
    public ChemicalAttributes.Fuel toLegacyAttribute() {
        return new ChemicalAttributes.Fuel(this);
    }
}
