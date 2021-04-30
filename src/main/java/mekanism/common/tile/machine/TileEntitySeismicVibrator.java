package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntitySeismicVibrator extends TileEntityMekanism implements IBoundingBlock {

    public int clientPiston;

    private MachineEnergyContainer<TileEntitySeismicVibrator> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntitySeismicVibrator() {
        super(MekanismBlocks.SEISMIC_VIBRATOR);
        cacheCoord();
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this), RelativeSide.BACK);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, this, 143, 35));
        return builder.build();
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (getActive()) {
            clientPiston++;
        }
        updateActiveVibrators();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        if (MekanismUtils.canFunction(this)) {
            FloatingLong energyPerTick = energyContainer.getEnergyPerTick();
            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                setActive(true);
                energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
            } else {
                setActive(false);
            }
        } else {
            setActive(false);
        }
        updateActiveVibrators();
    }

    private void updateActiveVibrators() {
        if (getActive()) {
            Mekanism.activeVibrators.add(getTileCoord());
        } else {
            Mekanism.activeVibrators.remove(getTileCoord());
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        Mekanism.activeVibrators.remove(getTileCoord());
        if (level != null) {
            level.removeBlock(getBlockPos().above(), false);
        }
    }

    @Override
    public void onPlace() {
        super.onPlace();
        WorldUtils.makeBoundingBlock(getLevel(), getBlockPos().above(), getBlockPos());
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(worldPosition, worldPosition.offset(1, 2, 1));
    }

    public MachineEnergyContainer<TileEntitySeismicVibrator> getEnergyContainer() {
        return energyContainer;
    }
}