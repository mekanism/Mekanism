package mekanism.common.content.gear.mekasuit;

import java.util.Map;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.FluidInDetails;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public record ModuleElectrolyticBreathingUnit(boolean fillHeld) implements ICustomModule<ModuleElectrolyticBreathingUnit> {

    public static final Identifier FILL_HELD = Mekanism.rl("breathing.held");

    public ModuleElectrolyticBreathingUnit(IModule<ModuleElectrolyticBreathingUnit> module) {
        this(module.getBooleanConfigOrFalse(FILL_HELD));
    }

    @Override
    public void tickServer(IModule<ModuleElectrolyticBreathingUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        int productionRate = 0;
        //Check if the mask is underwater
        //Note: Being in water is checked first to ensure that if it is raining and the player is in water
        // they get the full strength production
        float eyeHeight = player.getEyeHeight();
        Map<FluidType, FluidInDetails> fluidsIn = MekanismUtils.getFluidsIn(player, eyeHeight, (bb, data) -> {
            //Grab the center of the BB as that is where the player is for purposes of what it renders it intersects with
            double centerX = (bb.minX + bb.maxX) / 2;
            double centerZ = (bb.minZ + bb.maxZ) / 2;
            //For the y range check a range of where the mask's breathing unit is based on where the eyes are
            return new AABB(centerX, Math.min(bb.minY + data - 0.27, bb.maxY), centerZ, centerX, Math.min(bb.minY + data - 0.14, bb.maxY), centerZ);
        });
        if (fluidsIn.entrySet().stream().anyMatch(entry -> entry.getKey() == NeoForgeMod.WATER_TYPE.value() && entry.getValue().getMaxHeight() >= 0.11)) {
            //If the position the bottom of the mask is almost entirely in water set the production rate to our max rate
            // if the mask is only partially in water treat it as not being in it enough to actually function
            productionRate = getMaxRate(module);
        } else if (player.isInRain()) {
            //If the player is not in water but is in rain set the production to half power
            productionRate = getMaxRate(module) / 2;
        }
        if (productionRate > 0) {
            ChemicalResource hydrogen = ChemicalUtils.getResource(player.level(), MekanismChemicals.HYDROGEN);
            if (hydrogen.isEmpty()) {
                return;
            }
            int usage = MathUtils.multiplyClamped(2, ChemicalUtils.fuelEnergyDensity(hydrogen));
            //Calculate the max rate based on how much energy is available and can be extracted
            int maxRate = module.getEnergyRateLimit(player, itemAccess, usage, productionRate, transaction);
            int hydrogenUsed = 0;
            int availableHydrogen = 2 * maxRate;
            ItemAccess chestAccess = ItemAccessUtils.forEntitySlot(player, EquipmentSlot.CHEST);
            try (Transaction subTransaction = Transaction.open(transaction)) {
                if (!hydrogen.isEmpty() && checkChestPlate(chestAccess.getResource())) {
                    ResourceHandler<ChemicalResource> chestCapability = Capabilities.CHEMICAL.getCapability(chestAccess);
                    if (chestCapability != null) {
                        hydrogenUsed = chestCapability.insert(hydrogen, availableHydrogen, subTransaction);
                    }
                }
                if (fillHeld && !hydrogen.isEmpty()) {
                    ResourceHandler<ChemicalResource> handCapability = Capabilities.CHEMICAL.getCapability(ItemAccessUtils.playerHandAccess(player, InteractionHand.MAIN_HAND));
                    if (handCapability != null) {
                        hydrogenUsed += handCapability.insert(hydrogen, availableHydrogen - hydrogenUsed, subTransaction);
                    }
                }
                int oxygenUsed = Math.min(maxRate, player.getMaxAirSupply() - player.getAirSupply());
                int used = Math.max(Mth.ceil(hydrogenUsed / 2D), oxygenUsed);
                if (module.useAllEnergy(player, itemAccess, MathUtils.multiplyClamped(usage, used), subTransaction)) {
                    player.setAirSupply(player.getAirSupply() + oxygenUsed);
                    subTransaction.commit();
                }
            }
        }
    }

    /// Checks whether the given chestplate should be filled with hydrogen, if it can store hydrogen. Does not check whether the chestplate can store hydrogen.
    ///
    /// @param chest the chestplate to check
    ///
    /// @return whether the given chestplate should be filled with hydrogen.
    private <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean checkChestPlate(ITEM chest) {
        if (chest.is(MekanismItems.MEKASUIT_BODYARMOR)) {
            return IModuleHelper.INSTANCE.getModule(chest, MekanismModules.JETPACK_UNIT) != null;
        }
        return true;
    }

    private int getMaxRate(IModule<ModuleElectrolyticBreathingUnit> module) {
        return (int) Math.pow(2, module.getInstalledCount());
    }
}