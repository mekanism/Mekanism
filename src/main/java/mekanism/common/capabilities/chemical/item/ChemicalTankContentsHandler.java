package mekanism.common.capabilities.chemical.item;

import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.common.capabilities.Capabilities;
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
import mekanism.common.item.block.ItemBlockChemicalTank;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@NothingNullByDefault
public class ChemicalTankContentsHandler extends MergedTankContentsHandler<MergedChemicalTank> {

    public static void attachCapsToItem(RegisterCapabilitiesEvent event, Item item) {
        //TODO - 1.20.2: Figure out a better way to do this (as this may actually have sync issues if interacting with multiple caps at once??)
        // Replace the actual thing from using normal NBT to using attachments and actually attaching the backing tanks to it.
        // That way then each can have a merged tank, but then they can actually be updated on the backend?
        event.registerItem(Capabilities.GAS_HANDLER.item(), (stack, ctx) -> new ChemicalTankContentsHandler(stack).gasHandler, item);
        event.registerItem(Capabilities.INFUSION_HANDLER.item(), (stack, ctx) -> new ChemicalTankContentsHandler(stack).infusionHandler, item);
        event.registerItem(Capabilities.PIGMENT_HANDLER.item(), (stack, ctx) -> new ChemicalTankContentsHandler(stack).pigmentHandler, item);
        event.registerItem(Capabilities.SLURRY_HANDLER.item(), (stack, ctx) -> new ChemicalTankContentsHandler(stack).slurryHandler, item);
    }

    private ChemicalTankContentsHandler(ItemStack stack) {
        super(stack);
    }

    @Override
    protected MergedChemicalTank createMergedTank() {
        if (!(stack.getItem() instanceof ItemBlockChemicalTank tank)) {
            throw new IllegalStateException("ChemicalTankContentsHandler for a non chemical tank");
        }
        ChemicalTankTier tier = Objects.requireNonNull(tank.getTier(), "Chemical tank tier cannot be null");
        return MergedChemicalTank.create(
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