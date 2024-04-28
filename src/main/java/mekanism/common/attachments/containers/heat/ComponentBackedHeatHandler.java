package mekanism.common.attachments.containers.heat;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedHeatHandler extends ComponentBackedHandler<HeatCapacitorData, IHeatCapacitor, AttachedHeat> implements IMekanismHeatHandler {

    public ComponentBackedHeatHandler(ItemStack attachedTo) {
        super(attachedTo);
    }

    @Override
    protected ContainerType<IHeatCapacitor, AttachedHeat, ?> containerType() {
        return ContainerType.HEAT;
    }

    @Override
    public int getHeatCapacitorCount(@Nullable Direction side) {
        return containerCount();
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
}