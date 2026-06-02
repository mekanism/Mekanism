package mekanism.common.attachments.containers.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.type.ResourceContainerType.ChemicalContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class ContainerType {

    private ContainerType() {
    }

    static final List<IContainerType<?, ?>> TYPES_INTERNAL = new ArrayList<>();
    public static final List<IContainerType<?, ?>> TYPES = Collections.unmodifiableList(TYPES_INTERNAL);

    public static final EnergyContainerType ENERGY = new EnergyContainerType();
    public static final ResourceContainerType<ItemResource, IInventorySlot> ITEM = new ResourceContainerType<>(
          MekanismDataComponents.ATTACHED_ITEMS, SerializationConstants.ITEMS, Capabilities.ITEM,
          TileEntityMekanism::getInventorySlots, TileEntityMekanism::hasInventory, LargeResourceStack.ITEM_HELPER,
          resource -> resource instanceof ItemResource
    );
    public static final ResourceContainerType<FluidResource, IFluidTank> FLUID = new ResourceContainerType<>(
          MekanismDataComponents.ATTACHED_FLUIDS, SerializationConstants.FLUID_TANKS, Capabilities.FLUID,
          TileEntityMekanism::getFluidTanks, TileEntityMekanism::canHandleFluid, LargeResourceStack.FLUID_HELPER,
          resource -> resource instanceof FluidResource
    );
    public static final ResourceContainerType<ChemicalResource, IChemicalTank> CHEMICAL = new ChemicalContainerType();
    public static final HeatContainerType HEAT = new HeatContainerType();

    public static boolean anySupports(Holder<Item> item) {
        for (IContainerType<?, ?> type : TYPES) {
            if (type.supports(item)) {
                return true;
            }
        }
        return false;
    }
}