package mekanism.api.datamaps.chemical.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ITooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

/**
 * A {@link MekanismAPI#CHEMICAL_REGISTRY chemical} data map that allows defining fuel values for a chemical.
 *
 * @param burnTicks     The number of ticks one mB of fuel can be burned for before being depleted; must be greater than zero.
 * @param energyDensity The energy density in one mB of fuel; must be greater than zero.
 *
 * @since 10.7.11
 */
public record ChemicalFuel(int burnTicks, long energyDensity) implements IChemicalAttribute {

    /**
     * The ID of the data map.
     *
     * @see net.neoforged.neoforge.registries.RegistryManager#getDataMap(ResourceKey, ResourceLocation)
     */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_attribute_fuel");

    public static final Codec<ChemicalFuel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.BURN_TIME).forGetter(ChemicalFuel::burnTicks),
          SerializerHelper.POSITIVE_NONZERO_LONG_CODEC.fieldOf(SerializationConstants.ENERGY_DENSITY).forGetter(ChemicalFuel::energyDensity)
    ).apply(instance, ChemicalFuel::new));

    public ChemicalFuel {
        if (burnTicks < 1) {
            throw new IllegalArgumentException("Fuel attributes must burn for at least one tick! Burn Ticks: " + burnTicks);
        } else if (energyDensity < 1) {
            throw new IllegalArgumentException("Fuel attributes must have an energy density greater than zero!");
        } else if (energyDensity / burnTicks == 0L) {
            throw new IllegalArgumentException("Energy density per tick must be greater than zero! (integer division)");
        }
    }

    /**
     * Gets the amount of energy produced per tick of this fuel.
     */
    public long energyPerTick() {
        return energyDensity / burnTicks;
    }

    @Override
    public void collectTooltips(Consumer<Component> adder) {
        ITooltipHelper tooltipHelper = ITooltipHelper.INSTANCE;
        adder.accept(APILang.CHEMICAL_ATTRIBUTE_FUEL_BURN_TICKS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, tooltipHelper.getFormattedNumber(burnTicks)));
        adder.accept(APILang.CHEMICAL_ATTRIBUTE_FUEL_ENERGY_DENSITY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, tooltipHelper.getEnergyPerMBDisplayShort(energyDensity)));
    }

    @Override
    @Deprecated
    public ChemicalAttributes.Fuel toLegacyAttribute() {
        return new ChemicalAttributes.Fuel(this);
    }
}
