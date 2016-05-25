package buildcraft.api.transport.pipe_bc8;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableFluid;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableItem;

public interface IInsertionManager {
    /** Gets an {@link IInsertable_BC8} interface for a given tile or movable entity. */
    IInsertable_BC8 getInsertableFor(Object obj);

    /** Registers an {@link IInsertableFactory}. Note that if two insertables are registered and have an overlapping
     * class child, the most specific one is used. */
    <T> void registerInsertable(Class<T> clazz, IInsertableFactory<T> factory);

    public interface IInsertableFactory<T> {
        IInsertable_BC8 createNew(T obj);
    }

    public interface IInsertable_BC8 {
        boolean tryInsertItems(IPipeContentsEditableItem contents, Object extractor, EnumFacing direction, boolean simulate);

        boolean tryInsertFluid(IPipeContentsEditableFluid fluid, Object extractor, EnumFacing direction, boolean simulate);

        /** @return True if {@link #tryExtractItems(IStackFilter, Object, EnumFacing, boolean)} can return a non-null
         *         value at any future point in time. */
        boolean acceptsItems();

        /** @return True if {@link #tryExtractFluid(IFluidFilter, Object, EnumFacing, boolean)} can return a non-null
         *         value at any future point in time. */
        boolean acceptsFluids();
    }
}
