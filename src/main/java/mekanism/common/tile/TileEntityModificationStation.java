package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.IModuleItem;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
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
import net.minecraft.util.math.BlockPos;

public class TileEntityModificationStation extends TileEntityMekanism implements IBoundingBlock {

    private static final int BASE_TICKS_REQUIRED = 40;

    public int ticksRequired = BASE_TICKS_REQUIRED;
    public int operatingTicks;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getModuleItem")
    private InputInventorySlot moduleSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getContainerItem")
    public InputInventorySlot containerSlot;
    private MachineEnergyContainer<TileEntityModificationStation> energyContainer;

    public TileEntityModificationStation() {
        super(MekanismBlocks.MODIFICATION_STATION);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
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
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, this, 149, 21));
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
                ItemStack stack = containerSlot.getStack();
                // make sure the container supports this module
                if (MekanismAPI.getModuleHelper().getSupported(stack).contains(data)) {
                    // make sure we can still install more of this module
                    IModule<?> module = MekanismAPI.getModuleHelper().load(stack, data);
                    if (module == null || module.getInstalledCount() < data.getMaxStackSize()) {
                        operated = true;
                        operatingTicks++;
                        energyContainer.extract(energyContainer.getEnergyPerTick(), Action.EXECUTE, AutomationType.INTERNAL);
                        if (operatingTicks == ticksRequired) {
                            operatingTicks = 0;
                            ((IModuleContainerItem) stack.getItem()).addModule(stack, data);
                            containerSlot.setStack(stack);
                            MekanismUtils.logMismatchedStackSize(moduleSlot.shrinkStack(1, Action.EXECUTE), 1);
                        }
                    }
                }
            }
            if (!operated) {
                operatingTicks = 0;
            }
        }
    }

    public void removeModule(PlayerEntity player, ModuleData<?> type) {
        ItemStack stack = containerSlot.getStack();
        if (!stack.isEmpty()) {
            IModuleContainerItem container = (IModuleContainerItem) stack.getItem();
            if (container.hasModule(stack, type) && player.inventory.add(type.getItemProvider().getItemStack())) {
                container.removeModule(stack, type);
                containerSlot.setStack(stack);
            }
        }
    }

    public double getScaledProgress() {
        return (double) operatingTicks / ticksRequired;
    }

    @Override
    public void load(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.load(state, nbtTags);
        operatingTicks = nbtTags.getInt(NBTConstants.PROGRESS);
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT nbtTags) {
        super.save(nbtTags);
        nbtTags.putInt(NBTConstants.PROGRESS, operatingTicks);
        return nbtTags;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> operatingTicks, value -> operatingTicks = value));
    }

    @Override
    public void onPlace() {
        super.onPlace();
        WorldUtils.makeBoundingBlock(getLevel(), getBlockPos().above(), getBlockPos());
        Direction side = getRightSide();
        WorldUtils.makeBoundingBlock(getLevel(), getBlockPos().relative(side), getBlockPos());
        WorldUtils.makeBoundingBlock(getLevel(), getBlockPos().relative(side).above(), getBlockPos());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) {
            level.removeBlock(getBlockPos().above(), false);
            BlockPos rightPos = getBlockPos().relative(getRightSide());
            level.removeBlock(rightPos, false);
            level.removeBlock(rightPos.above(), false);
        }
    }
}