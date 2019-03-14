package mekanism.common.tile.prefab;

import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismBlocks;
import mekanism.common.Tier;
import mekanism.common.Upgrade;
import mekanism.common.base.IFactory;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.outputs.MachineOutput;
import mekanism.common.tile.TileEntityFactory;
import net.minecraft.util.ResourceLocation;

public abstract class TileEntityUpgradeableMachine<INPUT extends MachineInput<INPUT>, OUTPUT extends MachineOutput<OUTPUT>, RECIPE extends MachineRecipe<INPUT, OUTPUT, RECIPE>> extends
      TileEntityBasicMachine<INPUT, OUTPUT, RECIPE> implements ITierUpgradeable {

    /**
     * The foundation of all machines - a simple tile entity with a facing, active state, initialized state, sound
     * effect, and animated texture.
     *
     * @param soundPath - location of the sound effect
     * @param name - full name of this machine
     * @param maxEnergy - how much energy this machine can store
     * @param baseTicksRequired - how many ticks it takes to run a cycle
     */
    public TileEntityUpgradeableMachine(String soundPath, String name, double maxEnergy, double baseEnergyUsage,
          int upgradeSlot, int baseTicksRequired, ResourceLocation location) {
        super(soundPath, name, maxEnergy, baseEnergyUsage, upgradeSlot, baseTicksRequired, location);
    }

    @Override
    public boolean upgrade(Tier.BaseTier upgradeTier) {
        if (upgradeTier != Tier.BaseTier.BASIC) {
            return false;
        }

        world.setBlockToAir(getPos());
        world.setBlockState(getPos(), MekanismBlocks.MachineBlock.getStateFromMeta(5), 3);

        TileEntityFactory factory = (TileEntityFactory) world.getTileEntity(getPos());
        IFactory.RecipeType type = IFactory.RecipeType.getFromMachine(getBlockType(), getBlockMetadata());

        //Basic
        factory.facing = facing;
        factory.clientFacing = clientFacing;
        factory.ticker = ticker;
        factory.redstone = redstone;
        factory.redstoneLastTick = redstoneLastTick;
        factory.doAutoSync = doAutoSync;

        //Electric
        factory.electricityStored = electricityStored;

        //Machine
        factory.progress[0] = operatingTicks;
        factory.isActive = isActive;
        factory.controlType = controlType;
        factory.prevEnergy = prevEnergy;
        factory.upgradeComponent.readFrom(upgradeComponent);
        factory.upgradeComponent.setUpgradeSlot(0);
        factory.ejectorComponent.readFrom(ejectorComponent);
        factory.ejectorComponent
              .setOutputData(TransmissionType.ITEM, factory.configComponent.getOutputs(TransmissionType.ITEM).get(2));
        factory.setRecipeType(type);
        factory.upgradeComponent.setSupported(Upgrade.GAS, type.fuelEnergyUpgrades());
        factory.securityComponent.readFrom(securityComponent);

        for (TransmissionType transmission : configComponent.transmissions) {
            factory.configComponent.setConfig(transmission, configComponent.getConfig(transmission).asByteArray());
            factory.configComponent.setEjecting(transmission, configComponent.isEjecting(transmission));
        }

        upgradeInventory(factory);

        for (Upgrade upgrade : factory.upgradeComponent.getSupportedTypes()) {
            factory.recalculateUpgradables(upgrade);
        }

        factory.upgraded = true;
        factory.markDirty();

        return true;
    }

    protected abstract void upgradeInventory(TileEntityFactory factory);
}