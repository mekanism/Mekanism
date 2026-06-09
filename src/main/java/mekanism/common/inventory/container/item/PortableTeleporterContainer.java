package mekanism.common.inventory.container.item;

import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.TileEntityTeleporter.TeleporterStatus;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class PortableTeleporterContainer extends FrequencyItemContainer<TeleporterFrequency> implements IEmptyContainer {

    private TeleporterStatus status = TeleporterStatus.NO_FREQUENCY;

    public PortableTeleporterContainer(int id, Inventory inv, InteractionHand hand, ItemAccess itemAccess) {
        super(MekanismContainerTypes.PORTABLE_TELEPORTER, id, inv, hand, itemAccess);
    }

    @Nullable
    public EnergyHandler getEnergyHandler() {
        return Capabilities.ENERGY.getCapability(itemAccess);
    }

    public TeleporterStatus getStatus() {
        return status;
    }

    @Override
    protected boolean isValidType(ItemResource itemType) {
        return super.isValidType(itemType) && MekanismItems.PORTABLE_TELEPORTER.is(itemType);
    }

    @Override
    protected FrequencyType<TeleporterFrequency> getFrequencyType() {
        return FrequencyTypes.TELEPORTER;
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
                    EnergyHandler energyHandler = getEnergyHandler();
                    if (energyHandler == null) {
                        return TeleporterStatus.NOT_ENOUGH_ENERGY;
                    }
                    GlobalPos coords = freq.getClosestCoords(getLevel().dimension(), inv.player.blockPosition());
                    if (coords != null) {
                        int energyNeeded = TileEntityTeleporter.calculateEnergyCost(inv.player, coords);
                        //Note: We just use a rough estimate for if there is enough energy, so that we can skip retreiving the transactional state for sync checking
                        if (energyNeeded != -1 && energyHandler.getAmountAsLong() < energyNeeded) {
                            return TeleporterStatus.NOT_ENOUGH_ENERGY;
                        }
                    }
                }
                return TeleporterStatus.READY;
            }, value -> status = value));
        }
    }
}