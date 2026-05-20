package mekanism.common.tile;

import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.IContainerHolder;
import mekanism.common.capabilities.holder.MekContainerHelper;
import mekanism.common.content.gear.IModuleItem;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class TileEntityModificationStation extends TileEntityMekanism implements IBoundingBlock {

    private static final int BASE_TICKS_REQUIRED = MekanismUtils.TICKS_PER_HALF_SECOND;

    public int ticksRequired = BASE_TICKS_REQUIRED;
    public int operatingTicks;
    private boolean usedEnergy = false;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getModuleItem", docPlaceholder = "module slot")
    InputInventorySlot moduleSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getContainerItem", docPlaceholder = "module holder slot (suit, tool, etc)")
    public InputInventorySlot containerSlot;
    private MachineEnergyContainer<TileEntityModificationStation> energyContainer;

    public TileEntityModificationStation(BlockPos pos, BlockState state) {
        super(MekanismBlocks.MODIFICATION_STATION, pos, state);
    }

    @NotNull
    @Override
    protected IContainerHolder<IEnergyContainer> getInitialEnergyContainers(IContentsListener listener) {
        MekContainerHelper<IEnergyContainer> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener), RelativeSide.BACK);
        return builder.build();
    }

    public MachineEnergyContainer<TileEntityModificationStation> getEnergyContainer() {
        return energyContainer;
    }

    @NotNull
    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(moduleSlot = InputInventorySlot.at(stack -> stack.getItem() instanceof IModuleItem, listener, 35, 118));
        builder.addContainer(containerSlot = InputInventorySlot.at(IModuleHelper.INSTANCE::isModuleContainer, listener, 125, 118));
        moduleSlot.setSlotType(ContainerSlotType.NORMAL);
        moduleSlot.setSlotOverlay(SlotOverlay.MODULE);
        containerSlot.setSlotType(ContainerSlotType.NORMAL);
        builder.addContainer(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 151, 21));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        long clientEnergyUsed = 0L;
        if (canFunction()) {
            boolean operated = false;
            if (!moduleSlot.isEmpty() && !containerSlot.isEmpty()) {
                long energyPerTick = energyContainer.getEnergyPerTick();
                try (Transaction transaction = Transaction.openRoot()) {
                    if (energyContainer.extract(energyPerTick, transaction, AutomationType.INTERNAL) == energyPerTick) {
                        ItemResource moduleResource = moduleSlot.resource();
                        //TODO - 26.1: Should we have any handling for if there is more than one item in the container slot?
                        ItemStack stack = containerSlot.resource().toStack(containerSlot.amountAsInt());
                        //TODO - 26.1: Make the module container act upon an item access? And have that item access control setting the slot's contents
                        ModuleContainer container = ModuleHelper.get().getModuleContainer(stack);
                        if (container != null) {
                            // make sure the container supports this module and that we can still install more of this module
                            Holder<ModuleData<?>> data = ((IModuleItem) moduleResource.getItem()).getModuleData();
                            if (container.canInstall(stack, data)) {
                                operated = true;
                                operatingTicks++;
                                clientEnergyUsed = energyPerTick;
                                if (operatingTicks == ticksRequired) {
                                    operatingTicks = 0;
                                    int added = container.addModule(level.registryAccess(), stack, data, moduleSlot.amountAsInt());
                                    if (added > 0) {
                                        try (Transaction subTransaction = Transaction.open(transaction)) {
                                            //Validate that the module is actually able to be extracted from the module slot (this should always be true)
                                            if (moduleSlot.extract(moduleResource, added, subTransaction, AutomationType.INTERNAL) == added) {
                                                //Update the item type of the module container to the version that has the moduled added
                                                containerSlot.setContents(ItemResource.of(stack), stack.count());
                                                subTransaction.commit();
                                            }
                                        }
                                    }
                                }
                                transaction.commit();
                            }
                        }
                    }
                }
            }
            if (!operated) {
                operatingTicks = 0;
            }
        }
        usedEnergy = clientEnergyUsed > 0L;
        return sendUpdatePacket;
    }

    public boolean usedEnergy() {
        return usedEnergy;
    }

    public void removeModule(Player player, Holder<ModuleData<?>> type, boolean removeAll) {
        //TODO - 26.1: Should we have any handling for if there is more than one item in the container slot?
        ItemStack stack = containerSlot.resource().toStack(containerSlot.amountAsInt());
        //TODO - 26.1: Make the module container act upon an item access? And have that item access control setting the slot's contents
        ModuleContainer container = ModuleHelper.get().getModuleContainer(stack);
        if (container != null) {
            int installed = container.installedCount(type);
            if (installed > 0) {
                int toRemove = removeAll ? installed : 1;
                if (player.getInventory().add(new ItemStack(type.value().getItemHolder(), toRemove))) {
                    container.removeModule(player.registryAccess(), stack, type, toRemove);
                    //Update the item type of the module container to the version that has the moduled added
                    containerSlot.setContents(ItemResource.of(stack), stack.count());
                }
            }
        }
    }

    public double getScaledProgress() {
        return (double) operatingTicks / ticksRequired;
    }

    @Override
    public void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        operatingTicks = input.getIntOr(SerializationConstants.PROGRESS, operatingTicks);
    }

    @Override
    public void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(SerializationConstants.PROGRESS, operatingTicks);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> operatingTicks, value -> operatingTicks = value));
        container.track(SyncableBoolean.create(this::usedEnergy, value -> usedEnergy = value));
    }
}
