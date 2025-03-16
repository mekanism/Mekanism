package mekanism.api.datamaps.chemical.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ITooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * A {@link MekanismAPI#CHEMICAL_REGISTRY chemical} data map that allows defining radioactivity values for a chemical. If the radiation manager is enabled, this attribute
 * <i>requires validation</i>, meaning chemical containers won't be able to accept chemicals with this attribute by default. Radioactivity is measured in Sv/h.
 *
 * @param radioactivity Radioactivity of the chemical measured in Sv/h, must be greater than {@link IRadiationManager#baselineRadiation() baseline radiation}.
 *
 * @since 10.7.11
 */
public record ChemicalRadioactivity(double radioactivity) implements IChemicalAttribute {

    /**
     * The ID of the data map.
     *
     * @see mekanism.api.datamaps.IMekanismDataMapTypes#chemicalRadioactivity()
     */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_attribute_radioactivity");

    private static final Codec<Double> RADIATION_CODEC = Codec.doubleRange(IRadiationManager.INSTANCE.baselineRadiation(), Double.MAX_VALUE).validate(val -> {
        if (val == IRadiationManager.INSTANCE.baselineRadiation()) {
            return DataResult.error(() -> "Radiation must be greater than the baseline value");
        }
        return DataResult.success(val);
    });
    /**
     * Compressed codec for serializing and deserializing chemical radioactivity for use over the network.
     */
    public static final Codec<ChemicalRadioactivity> RADIOACTIVITY_CODEC = RADIATION_CODEC.xmap(ChemicalRadioactivity::new, ChemicalRadioactivity::radioactivity);
    /**
     * Codec for serializing and deserializing chemical radioactivity.
     */
    public static final Codec<ChemicalRadioactivity> CODEC = Codec.withAlternative(RecordCodecBuilder.create(in -> in.group(
          RADIATION_CODEC.fieldOf(SerializationConstants.RADIOACTIVITY).forGetter(ChemicalRadioactivity::radioactivity)
    ).apply(in, ChemicalRadioactivity::new)), RADIOACTIVITY_CODEC);

    public ChemicalRadioactivity {
        if (radioactivity <= IRadiationManager.INSTANCE.baselineRadiation()) {
            throw new IllegalArgumentException("Radiation attribute should only be used when there actually is radiation! Radioactivity: " + radioactivity);
        }
    }

    @Override
    public void collectTooltips(TooltipContext context, List<Component> tooltips, TooltipFlag tooltipFlag) {
        if (needsValidation()) {
            //Only show the radioactive tooltip information if radiation is actually enabled
            ITooltipHelper tooltipHelper = ITooltipHelper.INSTANCE;
            tooltips.add(APILang.CHEMICAL_ATTRIBUTE_RADIATION.translateColored(EnumColor.GRAY, EnumColor.INDIGO, tooltipHelper.getRadioactivityDisplayShort(radioactivity)));
        }
    }

    @Override
    public boolean needsValidation() {
        //This attribute only actually needs validation if radiation is enabled
        return IRadiationManager.INSTANCE.isRadiationEnabled();
    }

    @Internal
    @Override
    @SuppressWarnings("removal")
    public ChemicalAttributes.Radiation toLegacyAttribute() {
        return new ChemicalAttributes.Radiation(this);
    }
}
