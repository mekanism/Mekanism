package buildcraft.api.transport.pipe_bc8;

import net.minecraft.item.EnumDyeColor;

import buildcraft.api.APIHelper;
import buildcraft.api.transport.pipe_bc8.IExtractionManager.IExtractable_BC8;
import buildcraft.api.transport.pipe_bc8.IInsertionManager.IInsertable_BC8;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyImplicit;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyValue;

public enum PipeAPI_BC8 {
    INSTANCE;

    public static final IPropertyRegistry PROPERTY_REGISTRY;
    /** An insertion manager- used to get an instance of {@link IInsertable_BC8} for a given tile entity or movable
     * entity. Can also be used to register custom {@link IInsertable_BC8} for custom tiles. */
    public static final IInsertionManager INSERTION_MANAGER;
    /** An extraction manager- used to get an instance of {@link IExtractable_BC8} for a given tile entity or movable
     * entity. Can also be used to register custom {@link IExtractable_BC8} for custom entities. */
    public static final IExtractionManager EXTRACTION_MANAGER;

    public static final IPipeRegistry PIPE_REGISTRY;
    public static final IPipeListenerRegistry PIPE_LISTENER_REGISTRY;

    public static final IPipeHelper PIPE_HELPER;

    public static final IPipeType PIPE_TYPE_STRUCTURE;
    public static final IPipeType PIPE_TYPE_POWER;
    public static final IPipeType PIPE_TYPE_FLUID;
    public static final IPipeType PIPE_TYPE_ITEM;

    public static final IPipePropertyValue<EnumDyeColor> ITEM_COLOUR;
    public static final IPipePropertyValue<Boolean> ITEM_PAUSED;

    public static final IPipePropertyImplicit<Integer> ITEM_COUNT;
    public static final IPipePropertyImplicit<Integer> STACK_COUNT;

    static {
        PROPERTY_REGISTRY = APIHelper.getInstance("buildcraft.transport.api.impl.PropertyRegistry", IPropertyRegistry.class);
        INSERTION_MANAGER = APIHelper.getInstance("buildcraft.transport.api.impl.InsertionManager", IInsertionManager.class);
        EXTRACTION_MANAGER = APIHelper.getInstance("buildcraft.transport.api.impl.ExtractionManager", IExtractionManager.class);

        PIPE_REGISTRY = APIHelper.getInstance("buildcraft.transport.api.impl.PipeRegistry", IPipeRegistry.class);
        PIPE_LISTENER_REGISTRY = APIHelper.getInstance("buildcraft.transport.api.impl.PipeListenerRegistry", IPipeListenerRegistry.class);
        PIPE_HELPER = APIHelper.getInstance("buildcraft.transport.api.impl.PipeHelper", IPipeHelper.class);

        PIPE_TYPE_STRUCTURE = APIHelper.getNamedInstance("buildcraft.transport.api.impl.EnumPipeType", "STRUCTURE", IPipeType.class);
        PIPE_TYPE_POWER = APIHelper.getNamedInstance("buildcraft.transport.api.impl.EnumPipeType", "POWER", IPipeType.class);
        PIPE_TYPE_FLUID = APIHelper.getNamedInstance("buildcraft.transport.api.impl.EnumPipeType", "FLUID", IPipeType.class);
        PIPE_TYPE_ITEM = APIHelper.getNamedInstance("buildcraft.transport.api.impl.EnumPipeType", "ITEM", IPipeType.class);

        ITEM_COLOUR = PROPERTY_REGISTRY.getValueProperty("BuildCraft|Transport", "item_colour");
        ITEM_PAUSED = PROPERTY_REGISTRY.getValueProperty("BuildCraft|Transport", "item_paused");

        ITEM_COUNT = PROPERTY_REGISTRY.getImplicitProperty("BuildCraft|Transport", "item_count");
        STACK_COUNT = PROPERTY_REGISTRY.getImplicitProperty("BuildCraft|Transport", "stack_count");
    }
}
