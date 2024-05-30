package mekanism.api;

import mekanism.api.energy.IEnergyConversionHelper;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.ITooltipHelper;
import mekanism.common.Mekanism;
import net.neoforged.fml.common.Mod;

/**
 * Add an extra injection point for all our service loaders to force load them while we know for a fact we are using the transforming class loader, as there seems to be
 * some issue with {@link net.neoforged.fml.junit.JUnitService}, and presumably the fact it doesn't use {@link org.junit.platform.launcher.LauncherInterceptor} which
 * causes all the test methods to actually be executed using the app class loader.
 */
@Mod(Mekanism.MODID)
public class MekanismClassInit {//TODO: Remove this as soon as possible

    public MekanismClassInit() {
        forceInit(IMekanismAccess.INSTANCE);
        forceInit(IModuleHelper.INSTANCE);
        forceInit(IRadialDataHelper.INSTANCE);
        forceInit(IRadiationManager.INSTANCE);
        forceInit(ISecurityUtils.INSTANCE);
        forceInit(IBlockSecurityUtils.INSTANCE);
        forceInit(IItemSecurityUtils.INSTANCE);
        forceInit(IEntitySecurityUtils.INSTANCE);
        forceInit(ITooltipHelper.INSTANCE);
        forceInit(IEnergyConversionHelper.INSTANCE);
    }

    private void forceInit(Object obj) {
    }
}