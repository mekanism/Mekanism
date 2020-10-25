package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.AutomationType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.IModuleItem;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.Modules.ModuleData;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntityModificationStation extends TileEntityMekanism implements IBoundingBlock {

    private static final int BASE_TICKS_REQUIRED = 40;

    public int ticksRequired = BASE_TICKS_REQUIRED;
    public int operatingTicks;
    private EnergyInventorySlot energySlot;
    private InputInventorySlot moduleSlot;
    public InputInventorySlot containerSlot;
    private MachineEnergyContainer<TileEntityModificationStation> energyContainer;

    public TileEntityModificationStation() {
        super(MekanismBlocks.MODIFICATION_STATION);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this), RelativeSide.BACK);
        return builder.build();
    }

    public MachineEnergyContainer<TileEntityModificationStation> getEnergyContainer() {
        return energyContainer;
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(moduleSlot = InputInventorySlot.at(stack -> stack.getItem() instanceof IModuleItem, this, 35, 118));
        builder.addSlot(containerSlot = InputInventorySlot.at(stack -> stack.getItem() instanceof IModuleContainerItem, this, 125, 118));
        moduleSlot.setSlotType(ContainerSlotType.NORMAL);
        moduleSlot.setSlotOverlay(SlotOverlay.MODULE);
        containerSlot.setSlotType(ContainerSlotType.NORMAL);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 149, 21));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        if (MekanismUtils.canFunction(this)) {
            boolean operated = false;
            if (energyContainer.getEnergy().greaterOrEqual(energyContainer.getEnergyPerTick()) && !moduleSlot.isEmpty() && !containerSlot.isEmpty()) {
                ModuleData<?> data = ((IModuleItem) moduleSlot.getStack().getItem()).getModuleData();
                IModuleContainerItem item = (IModuleContainerItem) containerSlot.getStack().getItem();
                // make sure the container supports this module
                if (Modules.getSupported(containerSlot.getStack()).contains(data)) {
                    // make sure we can still install more of this module
                    if (!item.hasModule(containerSlot.getStack(), data) || item.getModule(containerSlot.getStack(), data).getInstalledCount() < data.getMaxStackSize()) {
                        operatingTicks++;
                        energyContainer.extract(energyContainer.getEnergyPerTick(), Action.EXECUTE, AutomationType.INTERNAL);
                        operated = true;
                    }
                }
            }

            if (!operated) {
                operatingTicks = 0;
            } else if (operatingTicks == ticksRequired) {
                operatingTicks = 0;
                // we're guaranteed this stuff is valid as we verified it in the same tick just above
                ItemStack stack = containerSlot.getStack();
                ModuleData<?> data = ((IModuleItem) moduleSlot.getStack().getItem()).getModuleData();
                IModuleContainerItem item = (IModuleContainerItem) stack.getItem();
                item.addModule(stack, data);
                containerSlot.setStack(stack);
                moduleSlot.shrinkStack(1, Action.EXECUTE);
            }
        }
    }

    public void removeModule(PlayerEntity player, ModuleData<?> type) {
        ItemStack stack = containerSlot.getStack();
        if (!stack.isEmpty()) {
            IModuleContainerItem container = (IModuleContainerItem) stack.getItem();
            if (container.hasModule(stack, type) && player.inventory.addItemStackToInventory(type.getStack().copy())) {
                container.removeModule(stack, type);
                containerSlot.setStack(stack);
            }
        }
    }

    public double getScaledProgress() {
        return (double) operatingTicks / ticksRequired;
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        operatingTicks = nbtTags.getInt(NBTConstants.PROGRESS);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt(NBTConstants.PROGRESS, operatingTicks);
        return nbtTags;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> operatingTicks, value -> operatingTicks = value));
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        // not exact, but doesn't matter that much
        return new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 2, 2));
    }

    @Override
    public void onPlace() {
        WorldUtils.makeBoundingBlock(getWorld(), getPos().up(), getPos());
        Direction side = getRightSide();
        WorldUtils.makeBoundingBlock(getWorld(), getPos().offset(side), getPos());
        WorldUtils.makeBoundingBlock(getWorld(), getPos().offset(side).up(), getPos());
    }

    @Override
    public void onBreak(BlockState oldState) {
        World world = getWorld();
        if (world != null) {
            Direction side = MekanismUtils.getRight(Attribute.getFacing(oldState));
            world.removeBlock(getPos().up(), false);
            world.removeBlock(getPos(), false);
            world.removeBlock(getPos().offset(side).up(), false);
            world.removeBlock(getPos().offset(side), false);
        }
    }
}
