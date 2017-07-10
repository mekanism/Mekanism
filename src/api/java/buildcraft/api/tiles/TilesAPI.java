package buildcraft.api.tiles;

import javax.annotation.Nonnull;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import buildcraft.api.core.CapabilitiesHelper;

public class TilesAPI {
    @Nonnull
    public static final Capability<IControllable> CAP_CONTROLLABLE;

    @Nonnull
    public static final Capability<IHasWork> CAP_HAS_WORK;

    @Nonnull
    public static final Capability<IHeatable> CAP_HEATABLE;

    @Nonnull
    public static final Capability<ITileAreaProvider> CAP_TILE_AREA_PROVIDER;

    @CapabilityInject(IControllable.class)
    private static final Capability<IControllable> CAP_CONTROLLABLE_FIRST = null;

    @CapabilityInject(IHasWork.class)
    private static final Capability<IHasWork> CAP_HAS_WORK_FIRST = null;

    @CapabilityInject(IHeatable.class)
    private static final Capability<IHeatable> CAP_HEATABLE_FIRST = null;

    @CapabilityInject(ITileAreaProvider.class)
    private static final Capability<ITileAreaProvider> CAP_TILE_AREA_PROVIDER_FIRST = null;

    static {
        CapabilitiesHelper.registerCapability(IControllable.class);
        CapabilitiesHelper.registerCapability(IHasWork.class);
        CapabilitiesHelper.registerCapability(IHeatable.class);
        CapabilitiesHelper.registerCapability(ITileAreaProvider.class);

        CAP_CONTROLLABLE = CapabilitiesHelper.ensureRegistration(CAP_CONTROLLABLE_FIRST, IControllable.class);
        CAP_HAS_WORK = CapabilitiesHelper.ensureRegistration(CAP_HAS_WORK_FIRST, IHasWork.class);
        CAP_HEATABLE = CapabilitiesHelper.ensureRegistration(CAP_HEATABLE_FIRST, IHeatable.class);
        CAP_TILE_AREA_PROVIDER = CapabilitiesHelper.ensureRegistration(CAP_TILE_AREA_PROVIDER_FIRST, ITileAreaProvider.class);
    }
}
