package mekanism.common.tile.prefab;

import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.tier.BaseTier;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public abstract class TileEntityUpgradeableMachine<RECIPE extends MekanismRecipe> extends TileEntityBasicMachine<RECIPE> implements ITierUpgradeable {

    /**
     * The foundation of all machines - a simple tile entity with a facing, active state, initialized state, sound effect, and animated texture.
     *
     * @param baseTicksRequired - how many ticks it takes to run a cycle
     */
    public TileEntityUpgradeableMachine(IBlockProvider blockProvider, int baseTicksRequired, ResourceLocation location) {
        super(blockProvider, baseTicksRequired, location);
    }

    @Override
    public boolean upgrade(BaseTier upgradeTier) {
        if (upgradeTier != BaseTier.BASIC) {
            return false;
        }
        World world = getWorld();
        if (world == null) {
            return false;
        }
        //TODO: Grab proper factory. If this is moved into the block from the tile it may make it easier
        if (true) {
            return false;
        }
        world.removeBlock(getPos(), false);
        //world.setBlockState(getPos(), MekanismBlocks.MachineBlock.getStateFromMeta(5));

        //TODO: Make this copy the settings over, probably make a method TileEntityMekanism#copySettings(TileEntityMekanism other)
        /*TileEntityFactory factory = Objects.requireNonNull((TileEntityFactory) world.getTileEntity(getPos()));
        RecipeType type = RecipeType.getFromMachine(getBlock(), getBlockMetadata());

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
        factory.progress[0] = getOperatingTicks();
        factory.isActive = isActive;
        factory.setControlType(getControlType());
        //TODO: Copy over some of the cached recipe information
        factory.upgradeComponent.readFrom(upgradeComponent);
        factory.upgradeComponent.setUpgradeSlot(0);
        factory.ejectorComponent.readFrom(ejectorComponent);
        factory.ejectorComponent.setOutputData(TransmissionType.ITEM, factory.configComponent.getOutputs(TransmissionType.ITEM).get(2));
        factory.setRecipeType(type);
        factory.upgradeComponent.setSupported(Upgrade.GAS, type.fuelEnergyUpgrades());
        factory.securityComponent.readFrom(securityComponent);

        for (TransmissionType transmission : configComponent.getTransmissions()) {
            factory.configComponent.setConfig(transmission, configComponent.getConfig(transmission).asByteArray());
            factory.configComponent.setEjecting(transmission, configComponent.isEjecting(transmission));
        }

        upgradeInventory(factory);

        for (Upgrade upgrade : factory.upgradeComponent.getSupportedTypes()) {
            factory.recalculateUpgrades(upgrade);
        }

        factory.upgraded = true;
        factory.markDirty();*/

        return true;
    }

    protected abstract void upgradeInventory(TileEntityFactory<?> factory);
}