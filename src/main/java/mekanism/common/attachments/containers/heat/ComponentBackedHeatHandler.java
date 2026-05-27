package mekanism.common.attachments.containers.heat;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.type.HeatContainerType;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: How if at all do we want to handle if the attached access is stacked?
@NothingNullByDefault
public class ComponentBackedHeatHandler extends ComponentBackedHandler<HeatCapacitorData, IHeatCapacitor, AttachedHeat, HeatContainerType> implements IMekanismHeatHandler {

    public ComponentBackedHeatHandler(HeatContainerType containerType, ItemAccess attachedAccess, int totalCapacitors) {
        super(containerType, attachedAccess, totalCapacitors);
    }

    @Override
    public int getHeatCapacitorCount(@Nullable Direction side) {
        return size();
    }

    @Override
    public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
        return getContainers();
    }

    @Nullable
    @Override
    public IHeatCapacitor getHeatCapacitor(int capacitor, @Nullable Direction side) {
        return getContainer(capacitor);
    }

    @Override
    public HeatCapacitorData getContents(int index) {
        AttachedHeat attached = getAttached();
        if (index < 0 || index >= attached.size()) {
            if (index > 0 && index < size()) {
                //Get the default. This isn't the cleanest way to look it up, but as we never use this method for component backed heat handlers
                // it should be fine for now
                return containerType().createNewAttachment(attachedAccess.getResource()).get(index);
            }
            //Allow it to fall through and cause an index out of bounds exception
        }
        return attached.get(index);
    }
}