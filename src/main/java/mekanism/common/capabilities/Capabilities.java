package mekanism.common.capabilities;

import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IConfigurable;
import mekanism.api.IEvaporationSolar;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.lasers.ILaserDissipation;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.api.radiation.capability.IRadiationShielding;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class Capabilities {

    private Capabilities() {
    }

    public static final Capability<IGasHandler> GAS_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IInfusionHandler> INFUSION_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IPigmentHandler> PIGMENT_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<ISlurryHandler> SLURRY_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IHeatHandler> HEAT_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IStrictEnergyHandler> STRICT_ENERGY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IConfigurable> CONFIGURABLE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IAlloyInteraction> ALLOY_INTERACTION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IConfigCardAccess> CONFIG_CARD_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IEvaporationSolar> EVAPORATION_SOLAR_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<ILaserReceptor> LASER_RECEPTOR_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<ILaserDissipation> LASER_DISSIPATION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IRadiationShielding> RADIATION_SHIELDING_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IRadiationEntity> RADIATION_ENTITY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
}