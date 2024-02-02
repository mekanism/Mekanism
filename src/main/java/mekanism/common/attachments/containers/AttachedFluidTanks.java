package mekanism.common.attachments.containers;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class AttachedFluidTanks extends AttachedContainers<IExtendedFluidTank> implements IMekanismFluidHandler {

    public AttachedFluidTanks(List<IExtendedFluidTank> tanks) {
        super(tanks);
    }

    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return containers;
    }

    public static class AttachedItemFluidTanks extends AttachedFluidTanks implements IFluidHandlerItem {

        private final ItemStack stack;

        public AttachedItemFluidTanks(ItemStack stack, List<IExtendedFluidTank> tanks) {
            super(tanks);
            this.stack = stack;
        }

        @Override
        public ItemStack getContainer() {
            return stack;
        }
    }
}