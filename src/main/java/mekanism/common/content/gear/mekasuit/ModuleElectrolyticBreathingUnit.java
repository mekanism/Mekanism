package mekanism.common.content.gear.mekasuit;

import java.util.Map;
import mekanism.api.Action;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.FluidInDetails;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;

@ParametersAreNotNullByDefault
public record ModuleElectrolyticBreathingUnit(boolean fillHeld) implements ICustomModule<ModuleElectrolyticBreathingUnit> {

    public static final ResourceLocation FILL_HELD = Mekanism.rl("breathing.held");

    public ModuleElectrolyticBreathingUnit(IModule<ModuleElectrolyticBreathingUnit> module) {
        this(module.getBooleanConfigOrFalse(FILL_HELD));
    }

    @Override
    public void tickServer(IModule<ModuleElectrolyticBreathingUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
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
            long usage = MathUtils.multiplyClamped(2, ChemicalUtil.hydrogenEnergyDensity());
            int maxRate = MathUtils.clampToInt(Math.min(productionRate, module.getContainerEnergy(stack) / usage));
            long hydrogenUsed = 0;
            ChemicalStack hydrogenStack = MekanismChemicals.HYDROGEN.asStack(maxRate * 2L);
            ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
            if (checkChestPlate(chestStack)) {
                IChemicalHandler chestCapability = Capabilities.CHEMICAL.getCapability(chestStack);
                if (chestCapability != null) {
                    hydrogenUsed = maxRate * 2L - chestCapability.insertChemical(hydrogenStack, Action.EXECUTE).getAmount();
                    hydrogenStack.shrink(hydrogenUsed);
                }
            }
            if (fillHeld) {
                ItemStack handStack = player.getItemBySlot(EquipmentSlot.MAINHAND);
                IChemicalHandler handCapability = Capabilities.CHEMICAL.getCapability(handStack);
                if (handCapability != null) {
                    hydrogenUsed = maxRate * 2L - handCapability.insertChemical(hydrogenStack, Action.EXECUTE).getAmount();
                }
            }
            int oxygenUsed = Math.min(maxRate, player.getMaxAirSupply() - player.getAirSupply());
            long used = Math.max(Mth.ceil(hydrogenUsed / 2D), oxygenUsed);
            module.useEnergy(player, stack, MathUtils.multiplyClamped(usage, used));
            player.setAirSupply(player.getAirSupply() + oxygenUsed);
        }
    }

    /**
     * Checks whether the given chestplate should be filled with hydrogen, if it can store hydrogen. Does not check whether the chestplate can store hydrogen.
     *
     * @param chestPlate the chestplate to check
     *
     * @return whether the given chestplate should be filled with hydrogen.
     */
    private boolean checkChestPlate(ItemStack chestPlate) {
        if (chestPlate.is(MekanismItems.MEKASUIT_BODYARMOR)) {
            return IModuleHelper.INSTANCE.getModule(chestPlate, MekanismModules.JETPACK_UNIT) != null;
        }
        return true;
    }

    private int getMaxRate(IModule<ModuleElectrolyticBreathingUnit> module) {
        return (int) Math.pow(2, module.getInstalledCount());
    }
}