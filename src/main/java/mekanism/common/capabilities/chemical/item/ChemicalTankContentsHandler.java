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
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@NothingNullByDefault
public class ChemicalTankContentsHandler extends MergedTankContentsHandler<MergedChemicalTank> {

    public static void attachCapsToItem(RegisterCapabilitiesEvent event, Item item) {
        event.registerItem(Capabilities.GAS_HANDLER.item(), (stack, ctx) -> getOrAttachHandler(stack).gasHandler, item);
        event.registerItem(Capabilities.INFUSION_HANDLER.item(), (stack, ctx) -> getOrAttachHandler(stack).infusionHandler, item);
        event.registerItem(Capabilities.PIGMENT_HANDLER.item(), (stack, ctx) -> getOrAttachHandler(stack).pigmentHandler, item);
        event.registerItem(Capabilities.SLURRY_HANDLER.item(), (stack, ctx) -> getOrAttachHandler(stack).slurryHandler, item);
    }

    /**
     * Gets or attaches a new handler to the stack so that we can have a single instance reach across capabilities. That way if the gas and infusion capabilities are
     * gotten, and we insert into gas and then try to insert into infusion we will properly not allow that.
     */
    private static ChemicalTankContentsHandler getOrAttachHandler(ItemStack stack) {
        //TODO - 1.20.4: Remove the need for the .get as it will be fixed
        if (stack.hasData(MekanismAttachmentTypes.CHEMICAL_TANK_CONTENTS_HANDLER.get())) {
            return stack.getData(MekanismAttachmentTypes.CHEMICAL_TANK_CONTENTS_HANDLER);
        }
        ChemicalTankContentsHandler handler = new ChemicalTankContentsHandler(stack);
        stack.setData(MekanismAttachmentTypes.CHEMICAL_TANK_CONTENTS_HANDLER, handler);
        return handler;
    }

    public static ChemicalTankContentsHandler createDummy() {
        return new ChemicalTankContentsHandler(MekanismBlocks.BASIC_CHEMICAL_TANK.getItemStack());
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