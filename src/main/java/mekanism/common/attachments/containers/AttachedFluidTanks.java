package mekanism.common.attachments.containers;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class AttachedFluidTanks extends AttachedContainers<IExtendedFluidTank> implements IMekanismFluidHandler {

    AttachedFluidTanks(List<IExtendedFluidTank> tanks, @Nullable IContentsListener listener) {
        super(tanks, listener);
    }

    @Override
    protected ContainerType<IExtendedFluidTank, ?, ?> getContainerType() {
        return ContainerType.FLUID;
    }

    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return containers;
    }

    public static class AttachedItemFluidTanks extends AttachedFluidTanks implements IFluidHandlerItem {

        private final ItemStack stack;

        AttachedItemFluidTanks(ItemStack stack, List<IExtendedFluidTank> tanks) {
            //Attachments on ItemStacks auto save, so we don't need to bother taking a listener as a parameter
            super(tanks, null);
            this.stack = stack;
        }

        @Override
        public ItemStack getContainer() {
            return stack;
        }
    }
}