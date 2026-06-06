package mekanism.common.util;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.attachments.containers.type.ContainerType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class FluidUtils {

    private FluidUtils() {
    }

    public static ItemStack getFilledVariant(Holder<Item> toFill, Holder<Fluid> fluid, @Nullable TransactionContext transaction) {
        ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(ItemResource.of(toFill));
        return ContainerType.FLUID.getFilledVariant(itemAccess, FluidResource.of(fluid), transaction);
    }

    public static int getRGBDurabilityForDisplay(ItemAccess itemAccess) {
        return getRGBDurabilityForDisplay(ContainerType.FLUID.getFirstResourceFromAttachment(itemAccess));
    }

    public static int getRGBDurabilityForDisplay(FluidResource fluidType) {
        if (fluidType.isEmpty()) {
            return 0xFFFFFFFF;
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
        return 0xFFFFFFFF;
    }
}