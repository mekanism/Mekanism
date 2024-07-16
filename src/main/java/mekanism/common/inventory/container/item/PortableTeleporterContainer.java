package mekanism.common.inventory.container.item;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.sync.SyncableByte;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class PortableTeleporterContainer extends FrequencyItemContainer<TeleporterFrequency> implements IEmptyContainer {

    private byte status;

    public PortableTeleporterContainer(int id, Inventory inv, InteractionHand hand, ItemStack stack) {
        super(MekanismContainerTypes.PORTABLE_TELEPORTER, id, inv, hand, stack);
    }

    public ItemStack getStack() {
        return stack;
    }

    public byte getStatus() {
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
            track(SyncableByte.create(() -> status, value -> status = value));
        } else {
            //Server side sync handling
            //Note: It is important these are in the same order as the client side trackers
            track(SyncableByte.create(() -> {
                byte status = 3;
                TeleporterFrequency freq = getFrequency();
                if (freq != null && !freq.getActiveCoords().isEmpty()) {
                    status = 1;
                    if (!inv.player.isCreative()) {
                        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                        if (energyContainer == null) {
                            status = 4;
                        } else {
                            GlobalPos coords = freq.getClosestCoords(GlobalPos.of(getLevel().dimension(), inv.player.blockPosition()));
                            if (coords != null) {
                                long energyNeeded = TileEntityTeleporter.calculateEnergyCost(inv.player, coords);
                                if (energyNeeded != -1 && energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.MANUAL) < energyNeeded) {
                                    status = 4;
                                }
                            }
                        }
                    }
                }
                return status;
            }, value -> status = value));
        }
    }
}