package mekanism.common.component.containers.type;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.component.containers.resource.AttachedResources;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ChemicalContainerType extends ResourceContainerType<ChemicalResource, IChemicalTank> {

    ChemicalContainerType() {
        super(MekanismDataComponents.ATTACHED_CHEMICALS, SerializationConstants.CHEMICAL_TANKS, Capabilities.CHEMICAL, LargeResourceStack.CHEMICAL_HELPER);
    }

    @Override
    public ChemicalResource asResourceOrEmpty(Resource resource) {
        return resource instanceof ChemicalResource chemicalResource ? chemicalResource : emptyResource();
    }

    @Override
    public List<IChemicalTank> getContainers(TileEntityMekanism tile) {
        return tile.getChemicalTanks();
    }

    @Override
    public boolean canHandle(TileEntityMekanism tile) {
        return tile.canHandleChemicals();
    }

    @Override
    protected boolean isVariableSize(IResourceContainer<ChemicalResource> container) {
        return container instanceof VariableCapacityChemicalTank;
    }

    /// @param toFill      Item type to try and fill.
    /// @param chemical    Chemical type to try and fill the item with.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    ///
    /// @return Stack representation of the item access once it has been filled with the given resource.
    public ItemStack getFilledVariant(Holder<Item> toFill, Holder<Chemical> chemical, @Nullable TransactionContext transaction) {
        return getFilledVariant(toFill, ChemicalResource.of(chemical), transaction);
    }

    @Override
    public int getRGBDurabilityForDisplay(ChemicalResource chemicalType) {
        return chemicalType.isEmpty() ? 0 : chemicalType.value().colorRepresentation();
    }

    /// Dumps the contents of a container into the level, and then clears the container. If the level is `null`, this will instead just clear the contents.
    ///
    /// @param level       The level on which to act.
    /// @param pos         Location in the level that the container was dumped.
    /// @param container   The container to dump the contents of. This is effectively just clears it if there are no side effects for dumping the stored resource.
    /// @param transaction The transaction that this operation is part of. May be `null`.
    public void dumpOrClearContents(@Nullable Level level, BlockPos pos, IResourceContainer<ChemicalResource> container, @Nullable TransactionContext transaction) {
        if (level == null) {
            clearContents(container, transaction);
        } else {
            dumpContents(level, pos, container, transaction);
        }
    }

    @Override
    public void dumpContents(Level level, BlockPos pos, IResourceContainer<ChemicalResource> container, @Nullable TransactionContext transaction) {
        LargeResourceStack<ChemicalResource> current = container.asStack();
        //Dump any radiation the current contents might contain
        IRadiationManager.INSTANCE.dumpRadiation(level, pos, current.resource(), current.amount());
        super.dumpContents(level, pos, container, transaction);
    }

    @Nullable
    @Override
    public AttachedResources<ChemicalResource> copyFromTile(TileEntityMekanism tile, List<IChemicalTank> containers) {
        Level level = tile.getLevel();
        RegistryAccess registryAccess = level == null ? null : level.registryAccess();
        boolean skipRadioactive = RadiationManager.isGlobalRadiationEnabled() && tile.shouldDumpRadiation();
        boolean hasNonEmpty = false;
        List<LargeResourceStack<ChemicalResource>> stacks = new ArrayList<>(containers.size());
        for (IChemicalTank container : containers) {
            LargeResourceStack<ChemicalResource> stack;
            if (skipRadioactive && container.resource().isRadioactive(registryAccess)) {
                stack = stackHelper().empty();
            } else {
                stack = container.asStack();
            }
            stacks.add(stack);
            if (!stack.isEmpty()) {
                hasNonEmpty = true;
            }
        }
        return hasNonEmpty ? new AttachedResources<>(stacks) : null;
    }

}