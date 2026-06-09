package mekanism.common.component.containers.type;

import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.CommonColors;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class FluidContainerType extends ResourceContainerType<FluidResource, IFluidTank> {

    FluidContainerType() {
        super(MekanismDataComponents.ATTACHED_FLUIDS, SerializationConstants.FLUID_TANKS, Capabilities.FLUID, LargeResourceStack.FLUID_HELPER);
    }

    @Override
    public FluidResource asResourceOrEmpty(Resource resource) {
        return resource instanceof FluidResource fluidResource ? fluidResource : emptyResource();
    }

    @Override
    public List<IFluidTank> getContainers(TileEntityMekanism tile) {
        return tile.getFluidTanks();
    }

    @Override
    public boolean canHandle(TileEntityMekanism tile) {
        return tile.canHandleFluid();
    }

    @Override
    protected boolean isVariableSize(IResourceContainer<FluidResource> container) {
        return container instanceof VariableCapacityFluidTank;
    }

    /// @param toFill      Item type to try and fill.
    /// @param fluid       Fluid type to try and fill the item with.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    ///
    /// @return Stack representation of the item access once it has been filled with the given resource.
    public ItemStack getFilledVariant(Holder<Item> toFill, Holder<Fluid> fluid, @Nullable TransactionContext transaction) {
        return getFilledVariant(toFill, FluidResource.of(fluid), transaction);
    }

    @Override
    public int getRGBDurabilityForDisplay(FluidResource fluidType) {
        if (fluidType.isEmpty()) {
            return CommonColors.WHITE;
        }
        //TODO: Technically doesn't support things where the color is part of the texture such as lava
        // for chemicals it is supported via allowing people to override getColorRepresentation in their
        // chemicals
        if (fluidType.getFluid().isSame(Fluids.LAVA)) {//Special case lava
            return 0xFFDB6B19;
        } else if (FMLEnvironment.getDist().isClient()) {
            //Note: We can only return an accurate result on the client side. This method should never be called from the server
            // but in case it is make sure we only run on the client side
            return MekanismRenderer.getColorARGB(fluidType);
        }
        return super.getRGBDurabilityForDisplay(fluidType);
    }

    @Override
    public boolean interactWithHandler(Player player, InteractionHand hand, @Nullable BlockPos pos, ResourceHandler<FluidResource> handler, @Nullable TransactionContext transaction) {
        return FluidUtil.interactWithFluidHandler(player, hand, pos, handler, transaction);
    }
}