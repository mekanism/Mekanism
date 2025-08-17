package mekanism.api.datamaps.chemical.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ITooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;

/**
 * A {@link MekanismAPI#CHEMICAL_REGISTRY chemical} data map that allows defining fuel values for a chemical.
 *
 * @param maxBurnPerTick how many mB per tick can be burnt (max amount burned when tank is full).
 * @param energyPerTick  The energy produced per tick from one mB of fuel; must be greater than zero.
 *
 * @since 10.7.11
 */
public record ChemicalFuel(int maxBurnPerTick, long energyPerTick) implements IChemicalAttribute {

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
          ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.MAX_BURN_RATE).forGetter(ChemicalFuel::maxBurnPerTick),
          SerializerHelper.POSITIVE_NONZERO_LONG_CODEC.fieldOf(SerializationConstants.ENERGY).forGetter(ChemicalFuel::energyPerTick)
    ).apply(instance, ChemicalFuel::new));

    public ChemicalFuel {
        if (maxBurnPerTick < 1) {
            throw new IllegalArgumentException("Fuel attributes must be able to burn at least 1 per tick. maxBurnPerTick: " + maxBurnPerTick);
        } else if (energyPerTick < 1) {
            throw new IllegalArgumentException("Fuel attributes must have a per tick energy density greater than zero!");
        }
    }

    /**
     * The energy density in one mB of fuel.
     */
    public long energyDensity() {
        return energyPerTick;
    }

    @Override
    public void collectTooltips(TooltipContext context, List<Component> tooltips, TooltipFlag tooltipFlag) {
        ITooltipHelper tooltipHelper = ITooltipHelper.INSTANCE;
        tooltips.add(APILang.CHEMICAL_ATTRIBUTE_FUEL_MAX_BURN.translateColored(EnumColor.GRAY, EnumColor.INDIGO, tooltipHelper.getFluidDisplay(maxBurnPerTick(), true)));
        tooltips.add(APILang.CHEMICAL_ATTRIBUTE_FUEL_ENERGY_DENSITY.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
              tooltipHelper.getEnergyPerMBDisplayShort(energyDensity())));
        tooltips.add(APILang.CHEMICAL_ATTRIBUTE_FUEL_ENERGY_MAX_TOTAL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
              tooltipHelper.getEnergyDisplay(getMaxJoulesPerTick(), true)));
    }

    public long getMaxJoulesPerTick() {
        return maxBurnPerTick() * energyPerTick();
    }
}
