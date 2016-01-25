package buildcraft.api.transport.pipe_bc8;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.pipe_bc8.IInsertionManager.IInsertableFactory;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableFluid;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableItem;

public interface IExtractionManager {
    /** Gets an {@link IExtractable_BC8} interface for a given tile or movable entity. */
    IExtractable_BC8 getExtractableFor(Object obj);

    /** Registers an {@link IInsertableFactory}. Note that if two insertables are registered and have an overlapping
     * class child, the most specific one is used. */
    <T> void registerInsertable(Class<T> clazz, IExtractableFactory<T> factory);

    public interface IExtractableFactory<T> {
        IExtractable_BC8 createNew(T obj);
    }

    public interface IExtractable_BC8 {
        /** @param filter The filter to use when determining what can be extracted
         * @param extractor The object the the contents are being extracted by
         * @param direction The direction the contents will be going in
         * @param simulate If true no changes will be made to the target inventory
         * @return A pipe contents object if it was successfully extracted, or null if nothing was extracted. */
        IPipeContentsEditableItem tryExtractItems(IStackFilter filter, Object extractor, EnumFacing direction, boolean simulate);

        /** @param filter The filter to use when determining what can be extracted
         * @param extractor The object the the contents are being extracted by
         * @param direction The direction the contents will be going in
         * @param simulate If true no changes will be made to the target tank
         * @return A pipe contents object if it was successfully extracted, or null if nothing was extracted. */
        IPipeContentsEditableFluid tryExtractFluid(IFluidFilter filter, Object extractor, EnumFacing direction, boolean simulate);

        /** @return True if {@link #tryExtractItems(IStackFilter, Object, EnumFacing, boolean)} can return a non-null
         *         value at any future point in time. */
        boolean givesItems();

        /** @return True if {@link #tryExtractFluid(IFluidFilter, Object, EnumFacing, boolean)} can return a non-null
         *         value at any future point in time. */
        boolean givesFluids();
    }
}
