package mekanism.common.capabilities.chemical.item;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.common.capabilities.DynamicHandler.InteractPredicate;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicGasHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicInfusionHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicPigmentHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicSlurryHandler;
import mekanism.common.capabilities.chemical.item.ChemicalTankRateLimitChemicalTank.GasTankRateLimitChemicalTank;
import mekanism.common.capabilities.chemical.item.ChemicalTankRateLimitChemicalTank.InfusionTankRateLimitChemicalTank;
import mekanism.common.capabilities.chemical.item.ChemicalTankRateLimitChemicalTank.PigmentTankRateLimitChemicalTank;
import mekanism.common.capabilities.chemical.item.ChemicalTankRateLimitChemicalTank.SlurryTankRateLimitChemicalTank;
import mekanism.common.capabilities.merged.MergedTankContentsHandler;
import mekanism.common.tier.ChemicalTankTier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalTankContentsHandler extends MergedTankContentsHandler<MergedChemicalTank> {

    public static ChemicalTankContentsHandler create(ChemicalTankTier tier) {
        Objects.requireNonNull(tier, "Chemical tank tier cannot be null");
        return new ChemicalTankContentsHandler(tier);
    }

    private ChemicalTankContentsHandler(ChemicalTankTier tier) {
        mergedTank = MergedChemicalTank.create(
              new GasTankRateLimitChemicalTank(tier, gasHandler = new DynamicGasHandler(side -> gasTanks, InteractPredicate.ALWAYS_TRUE, InteractPredicate.ALWAYS_TRUE,
                    () -> onContentsChanged(NBTConstants.GAS_TANKS, gasTanks))),
              new InfusionTankRateLimitChemicalTank(tier, infusionHandler = new DynamicInfusionHandler(side -> infusionTanks, InteractPredicate.ALWAYS_TRUE,
                    InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.INFUSION_TANKS, infusionTanks))),
              new PigmentTankRateLimitChemicalTank(tier, pigmentHandler = new DynamicPigmentHandler(side -> pigmentTanks, InteractPredicate.ALWAYS_TRUE,
                    InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.PIGMENT_TANKS, pigmentTanks))),
              new SlurryTankRateLimitChemicalTank(tier, slurryHandler = new DynamicSlurryHandler(side -> slurryTanks, InteractPredicate.ALWAYS_TRUE,
                    InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.SLURRY_TANKS, slurryTanks)))
        );
    }
}