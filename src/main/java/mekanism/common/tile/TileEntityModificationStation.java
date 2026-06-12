package mekanism.common.tile;

import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.single.BasicSingleHolder;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.UnknownNullability;

public class TileEntityModificationStation extends TileEntityMekanism implements IBoundingBlock {

    private static final int BASE_TICKS_REQUIRED = MekanismUtils.TICKS_PER_HALF_SECOND;

    public int ticksRequired = BASE_TICKS_REQUIRED;
    public int operatingTicks;
    private boolean usedEnergy = false;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getModuleItem", docPlaceholder = "module slot")
    InputInventorySlot moduleSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getContainerItem", docPlaceholder = "module holder slot (suit, tool, etc)")
    public InputInventorySlot containerSlot;
    @UnknownNullability//Initialized via getInitialEnergyContainer
    private MachineEnergyContainer<TileEntityModificationStation> energyContainer;

    public TileEntityModificationStation(BlockPos pos, BlockState state) {
        super(MekanismBlocks.MODIFICATION_STATION, pos, state);
    }

    @Override
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = MachineEnergyContainer.input(this, listener);
        return new BasicSingleHolder<>(energyContainer, facingSupplier, BACK_ONLY);
    }

    public MachineEnergyContainer<TileEntityModificationStation> energyContainer() {
        return energyContainer;
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(moduleSlot = InputInventorySlot.at(stack -> stack.getItem() instanceof IModuleItem, listener, 35, 118));
        builder.addContainer(containerSlot = InputInventorySlot.at(1, IModuleHelper.INSTANCE::isModuleContainer, listener, 125, 118));
        moduleSlot.setSlotType(ContainerSlotType.NORMAL);
        moduleSlot.setSlotOverlay(SlotOverlay.MODULE);
        containerSlot.setSlotType(ContainerSlotType.NORMAL);
        builder.addContainer(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 151, 21));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        energySlot.fillContainerOrConvert(null);
        int clientEnergyUsed = 0;
        if (canFunction()) {
            boolean operated = false;
            if (!moduleSlot.isEmpty() && !containerSlot.isEmpty()) {
                int energyPerTick = energyContainer.getEnergyPerTick();
                try (Transaction transaction = Transaction.openRoot()) {
                    if (energyContainer.extract(energyPerTick, transaction, AutomationType.INTERNAL) == energyPerTick) {
                        ItemResource moduleResource = moduleSlot.resource();
                        ItemAccess containerAccess = containerSlot.asItemAccess();
                        ModuleContainer container = ModuleHelper.get().getModuleContainer(containerAccess.getResource());
                        if (container != null) {
                            // make sure the container supports this module and that we can still install more of this module
                            Holder<ModuleData<?>> data = ((IModuleItem) moduleResource.getItem()).getModuleData();
                            if (container.canInstall(containerAccess, data)) {
                                operatingTicks++;
                                if (operatingTicks == ticksRequired) {
                                    operatingTicks = 0;
                                    try (Transaction subTransaction = Transaction.open(transaction)) {
                                        int added = container.addModule(level.registryAccess(), containerAccess, data, moduleSlot.amountAsInt(), subTransaction);
                                        //If the module could be added to the container, and we were able to extract it from the module slot (which we should always be able to do)
                                        if (added > 0 && moduleSlot.extract(moduleResource, added, subTransaction, AutomationType.INTERNAL) == added) {
                                            //Commit to update the stored item type, and the removal of the module from the module slot
                                            subTransaction.commit();
                                        }
                                    }
                                }
                                operated = true;
                                clientEnergyUsed = energyPerTick;
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
        usedEnergy = clientEnergyUsed > 0;
        return sendUpdatePacket;
    }

    public boolean usedEnergy() {
        return usedEnergy;
    }

    public void removeModule(Player player, Holder<ModuleData<?>> type, boolean removeAll) {
        ItemAccess containerAccess = containerSlot.asItemAccess();
        ModuleContainer container = ModuleHelper.get().getModuleContainer(containerAccess.getResource());
        if (container != null) {
            int installed = container.installedCount(type);
            if (installed > 0) {
                try (Transaction transaction = Transaction.openRoot()) {
                    PlayerInventoryWrapper playerInv = PlayerInventoryWrapper.of(player);
                    int toRemove = playerInv.insert(ItemResource.of(type.value().getItemHolder()), removeAll ? installed : 1, transaction);
                    //If we were able to add at least some of the modules to the player's inventory,
                    // and we are able to remove the corresponding number of modules from the item
                    if (toRemove > 0 && container.removeModule(player.registryAccess(), containerAccess, type, toRemove, transaction)) {
                        //Commit to update the stored item type, and the addition of the module item to the player's inventory
                        transaction.commit();
                    }
                }
            }
        }
    }

    public double getScaledProgress() {
        return (double) operatingTicks / ticksRequired;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        operatingTicks = input.getIntOr(SerializationConstants.PROGRESS, operatingTicks);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
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
