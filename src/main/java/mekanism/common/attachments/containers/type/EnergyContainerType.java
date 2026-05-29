package mekanism.common.attachments.containers.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.attachments.containers.energy.AttachedEnergy;
import mekanism.common.attachments.containers.energy.ComponentBackedEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.component.DataComponentGetter;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public final class EnergyContainerType extends CapableContainerType<IEnergyContainer, AttachedEnergy, EnergyHandler> {

    EnergyContainerType() {
        super(MekanismDataComponents.ATTACHED_ENERGY, SerializationConstants.ENERGY_CONTAINERS, SerializationConstants.CONTAINER, Capabilities.ENERGY,
              AttachedEnergy.EMPTY, tile -> {
                  //TODO - 26.1: Re-evaluate this
                  IEnergyContainer energyContainer = tile.getEnergyContainer();
                  return energyContainer == null ? Collections.emptyList() : Collections.singletonList(energyContainer);
              }, TileEntityMekanism::canHandleEnergy);
    }

    @Override
    protected EnergyHandler createHandler(ItemAccess attachedAccess, int totalContainers) {
        return new ComponentBackedEnergyHandler(this, attachedAccess, totalContainers);
    }

    public void copyToContainer(IEnergyContainer containers, DataComponentGetter componentGetter) {
        //TODO - 26.1: Re-evaluate this
        copyToContainers(Collections.singletonList(containers), componentGetter);
    }

    @Override
    public void copyToContainers(List<IEnergyContainer> containers, AttachedEnergy attached) {
        List<Long> stored = attached.containers();
        int size = stored.size();
        if (size == containers.size()) {
            for (int i = 0; i < size; i++) {
                containers.get(i).setEnergy(stored.get(i), null);
            }
        }
    }

    @Nullable
    @Override
    public AttachedEnergy attachedCopyOf(List<IEnergyContainer> containers) {
        boolean hasNonEmpty = false;
        List<Long> stored = new ArrayList<>(containers.size());
        for (IEnergyContainer container : containers) {
            stored.add(container.energy());
            if (!container.isEmpty()) {
                hasNonEmpty = true;
            }
        }
        return hasNonEmpty ? new AttachedEnergy(stored) : null;
    }

    @Override
    public void copy(IEnergyContainer from, IEnergyContainer to) {
        to.copyContents(from);
    }
}