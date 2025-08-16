package mekanism.common.inventory.container.item;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.TileEntityTeleporter.TeleporterStatus;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PortableTeleporterContainer extends FrequencyItemContainer<TeleporterFrequency> implements IEmptyContainer {

    private TeleporterStatus status = TeleporterStatus.NO_FREQUENCY;

    public PortableTeleporterContainer(int id, Inventory inv, InteractionHand hand, ItemStack stack) {
        super(MekanismContainerTypes.PORTABLE_TELEPORTER, id, inv, hand, stack);
    }

    @Nullable
    public IEnergyContainer getEnergyContainer() {
        return StorageUtils.getEnergyContainer(stack, 0);
    }

    public TeleporterStatus getStatus() {
        return status;
    }

    @Override
    protected FrequencyType<TeleporterFrequency> getFrequencyType() {
        return FrequencyType.TELEPORTER;
    }

    @Override
    protected void addContainerTrackers() {
        super.addContainerTrackers();
        //Relies on super being called first
        if (getLevel().isClientSide()) {
            //Client side sync handling
            track(SyncableEnum.create(TeleporterStatus.BY_ID, TeleporterStatus.NO_FREQUENCY, this::getStatus, value -> status = value));
        } else {
            //Server side sync handling
            //Note: It is important these are in the same order as the client side trackers
            track(SyncableEnum.create(TeleporterStatus.BY_ID, TeleporterStatus.NO_FREQUENCY, () -> {
                TeleporterFrequency freq = getFrequencyFromStack();
                if (freq == null) {
                    return TeleporterStatus.NO_FREQUENCY;
                }
                if (freq.getActiveCoords().isEmpty()) {
                    return TeleporterStatus.NO_DESTINATION;
                }
                if (!inv.player.isCreative()) {
                    IEnergyContainer energyContainer = getEnergyContainer();
                    if (energyContainer == null) {
                        return TeleporterStatus.NOT_ENOUGH_ENERGY;
                    }
                    GlobalPos coords = freq.getClosestCoords(GlobalPos.of(getLevel().dimension(), inv.player.blockPosition()));
                    if (coords != null) {
                        long energyNeeded = TileEntityTeleporter.calculateEnergyCost(inv.player, coords);
                        if (energyNeeded != -1 && energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.MANUAL) < energyNeeded) {
                            return TeleporterStatus.NOT_ENOUGH_ENERGY;
                        }
                    }
                }
                return TeleporterStatus.READY;
            }, value -> status = value));
        }
    }
}