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
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.lib.radiation.capability.RadiationEntity;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.tile.TileEntityBoundingBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class Capabilities {

    private Capabilities() {
    }

    public static ICapabilityProvider<?, ?, ?> SIMPLE_PROVIDER = (obj, context) -> obj;

    //TODO - 1.20.2: Do we want to somehow proxy the fluid ones? Would be more difficult given the different types
    public static final MultiTypeCapability<IEnergyStorage> ENERGY = new MultiTypeCapability<>(EnergyStorage.BLOCK, EnergyStorage.ITEM, EnergyStorage.ENTITY);
    //Note: We intentionally don't use the entity automation capability, as we want to be able to target player inventories and the like
    public static final MultiTypeCapability<IItemHandler> ITEM = new MultiTypeCapability<>(ItemHandler.BLOCK, ItemHandler.ITEM, ItemHandler.ENTITY);

    public static final MultiTypeCapability<IGasHandler> GAS = new MultiTypeCapability<>(Mekanism.rl("gas_handler"), IGasHandler.class);
    public static final MultiTypeCapability<IInfusionHandler> INFUSION = new MultiTypeCapability<>(Mekanism.rl("infusion_handler"), IInfusionHandler.class);
    public static final MultiTypeCapability<IPigmentHandler> PIGMENT = new MultiTypeCapability<>(Mekanism.rl("pigment_handler"), IPigmentHandler.class);
    public static final MultiTypeCapability<ISlurryHandler> SLURRY = new MultiTypeCapability<>(Mekanism.rl("slurry_handler"), ISlurryHandler.class);

    public static final BlockCapability<IHeatHandler, @Nullable Direction> HEAT = BlockCapability.createSided(Mekanism.rl("heat_handler"), IHeatHandler.class);

    public static final MultiTypeCapability<IStrictEnergyHandler> STRICT_ENERGY = new MultiTypeCapability<>(Mekanism.rl("strict_energy_handler"), IStrictEnergyHandler.class);

    public static final BlockCapability<IConfigurable, @Nullable Direction> CONFIGURABLE = BlockCapability.createSided(Mekanism.rl("configurable"), IConfigurable.class);

    public static final BlockCapability<IAlloyInteraction, @Nullable Direction> ALLOY_INTERACTION = BlockCapability.createSided(Mekanism.rl("alloy_interaction"), IAlloyInteraction.class);

    //TODO: Should some of these be a void context? Does that even play nicely with block capabilities?
    public static final BlockCapability<IConfigCardAccess, @Nullable Direction> CONFIG_CARD = BlockCapability.createSided(Mekanism.rl("config_card"), IConfigCardAccess.class);

    public static final BlockCapability<IEvaporationSolar, @Nullable Direction> EVAPORATION_SOLAR = BlockCapability.createSided(Mekanism.rl("evaporation_solar"), IEvaporationSolar.class);

    public static final BlockCapability<ILaserReceptor, @Nullable Direction> LASER_RECEPTOR = BlockCapability.createSided(Mekanism.rl("laser_receptor"), ILaserReceptor.class);

    public static final ItemCapability<ILaserDissipation, Void> LASER_DISSIPATION = ItemCapability.createVoid(Mekanism.rl("laser_dissipation"), ILaserDissipation.class);

    public static final ItemCapability<IRadiationShielding, Void> RADIATION_SHIELDING = ItemCapability.createVoid(Mekanism.rl("radiation_shielding"), IRadiationShielding.class);

    public static final EntityCapability<IRadiationEntity, Void> RADIATION_ENTITY = EntityCapability.createVoid(Mekanism.rl("radiation"), IRadiationEntity.class);

    public static final ResourceLocation OWNER_OBJECT_NAME = Mekanism.rl("owner_object");
    public static final ResourceLocation SECURITY_OBJECT_NAME = Mekanism.rl("security_object");

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        Mekanism.hooks.hookCapabilityRegistration();

        EntityType<EntityRobit> robitEntityType = MekanismEntityTypes.ROBIT.get();
        event.registerEntity(IEntitySecurityUtils.INSTANCE.ownerCapability(), robitEntityType, (robit, ctx) -> robit);
        event.registerEntity(IEntitySecurityUtils.INSTANCE.securityCapability(), robitEntityType, (robit, ctx) -> robit);
        EnergyCompatUtils.registerEntityCapabilities(event, robitEntityType, (robit, ctx) -> robit);

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            //Note: The jvm will reuse the lambda between types
            event.registerEntity(RADIATION_ENTITY, entityType, (entity, ctx) -> entity instanceof LivingEntity living ? new RadiationEntity(living) : null);
        }
        //TODO - 1.20.2: We could loop all mek items to allow for them to provide caps that way... but
        // then we need to make sure we do that for other modules as well
        // (unless we have a custom interface and then loop all items and check for it, but that seems less efficient)
        for (Item item : BuiltInRegistries.ITEM) {
            //TODO - 1.20.2: Should we only loop our items and blocks and make each sub module also do theirs?
            // If so look at how we are doing tiles via an event per register wrapper
            if (item instanceof ICapabilityAware capabilityAware) {
                capabilityAware.attachCapabilities(event);
            }
        }

        //Register bounding block proxies
        TileEntityBoundingBlock.alwaysProxyCapability(event, CONFIG_CARD);
        TileEntityBoundingBlock.alwaysProxyCapability(event, IBlockSecurityUtils.INSTANCE.ownerCapability());
        TileEntityBoundingBlock.alwaysProxyCapability(event, IBlockSecurityUtils.INSTANCE.securityCapability());
        //Capabilities we need to proxy because some sub implementations use them
        TileEntityBoundingBlock.proxyCapability(event, ITEM.block());
        for (BlockCapability<?, @Nullable Direction> capability : EnergyCompatUtils.getLoadedEnergyCapabilities()) {
            TileEntityBoundingBlock.proxyCapability(event, capability);
        }
        //Note: Common caps we may eventually want to proxy but currently have no use for doing so
        /*TileEntityBoundingBlock.proxyCapability(event, FluidHandler.BLOCK);
        TileEntityBoundingBlock.proxyCapability(event, GAS_HANDLER.block());
        TileEntityBoundingBlock.proxyCapability(event, INFUSION_HANDLER.block());
        TileEntityBoundingBlock.proxyCapability(event, PIGMENT_HANDLER.block());
        TileEntityBoundingBlock.proxyCapability(event, SLURRY_HANDLER.block());
        TileEntityBoundingBlock.proxyCapability(event, HEAT_HANDLER.block());*/
    }
}