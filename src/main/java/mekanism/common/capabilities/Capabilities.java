package mekanism.common.capabilities;

import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IConfigurable;
import mekanism.api.IEvaporationSolar;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalHandler;
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
import mekanism.common.integration.computer.ComputerCapabilityHelper;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.lib.radiation.capability.RadiationEntity;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.tile.TileEntityBoundingBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities.Energy;
import net.neoforged.neoforge.capabilities.Capabilities.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities.Item;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public class Capabilities {

    private Capabilities() {
    }

    public static final ICapabilityProvider<?, ?, ?> SIMPLE_PROVIDER = (obj, context) -> obj;

    @Deprecated(forRemoval = true)//TODO - 26.1: Remove this
    private record FluidCapability(BlockCapability<IFluidHandler, @Nullable Direction> block,
                                   ItemCapability<IFluidHandlerItem, ItemAccess> item,
                                   EntityCapability<IFluidHandler, @Nullable Direction> entity) implements IMultiTypeCapability<IFluidHandler, IFluidHandlerItem> {
    }

    public static final MultiTypeCapability<EnergyHandler> ENERGY = new MultiTypeCapability<>(Energy.BLOCK, Energy.ITEM, Energy.ENTITY);
    public static final MultiTypeCapability<ResourceHandler<FluidResource>> FLUID = new MultiTypeCapability<>(Fluid.BLOCK, Fluid.ITEM, Fluid.ENTITY);
    //Note: We intentionally don't use the entity automation capability, as we want to be able to target player inventories and the like
    public static final MultiTypeCapability<ResourceHandler<ItemResource>> ITEM = new MultiTypeCapability<>(Item.BLOCK, Item.ITEM, Item.ENTITY);
    @Deprecated(forRemoval = true)//TODO - 26.1: Replace this with the above fluid cap
    public static final IMultiTypeCapability<IFluidHandler, IFluidHandlerItem> FLUID_LEGACY = new FluidCapability(
          BlockCapability.createSided(Mekanism.rl("legacy_fluid"), IFluidHandler.class),
          ItemCapability.create(Mekanism.rl("legacy_fluid"), IFluidHandlerItem.class, ItemAccess.class),
          EntityCapability.createSided(Mekanism.rl("legacy_fluid"), IFluidHandler.class));

    public static final MultiTypeCapability<ResourceHandler<ChemicalResource>> CHEMICAL = new MultiTypeCapability<>(Mekanism.rl("chemical_handler"), ResourceHandler.asClass());
    @Deprecated(forRemoval = true)//TODO - 26.1: Remove this
    public static final MultiTypeCapability<IChemicalHandler> CHEMICAL_LEGACY = new MultiTypeCapability<>(Mekanism.rl("legacy_chemical"), IChemicalHandler.class);


    public static final BlockCapability<IHeatHandler, @Nullable Direction> HEAT = BlockCapability.createSided(Mekanism.rl("heat_handler"), IHeatHandler.class);

    public static final MultiTypeCapability<IStrictEnergyHandler> STRICT_ENERGY = new MultiTypeCapability<>(Mekanism.rl("strict_energy_handler"), IStrictEnergyHandler.class);

    public static final BlockCapability<IConfigurable, @Nullable Direction> CONFIGURABLE = BlockCapability.createSided(Mekanism.rl("configurable"), IConfigurable.class);

    public static final BlockCapability<IAlloyInteraction, @Nullable Direction> ALLOY_INTERACTION = BlockCapability.createSided(Mekanism.rl("alloy_interaction"), IAlloyInteraction.class);

    public static final BlockCapability<IConfigCardAccess, @Nullable Direction> CONFIG_CARD = BlockCapability.createSided(Mekanism.rl("config_card"), IConfigCardAccess.class);

    public static final BlockCapability<IEvaporationSolar, Void> EVAPORATION_SOLAR = BlockCapability.createVoid(Mekanism.rl("evaporation_solar"), IEvaporationSolar.class);

    public static final BlockCapability<ILaserReceptor, @Nullable Direction> LASER_RECEPTOR = BlockCapability.createSided(Mekanism.rl("laser_receptor"), ILaserReceptor.class);

    public static final ItemCapability<ILaserDissipation, Void> LASER_DISSIPATION = ItemCapability.createVoid(Mekanism.rl("laser_dissipation"), ILaserDissipation.class);

    public static final ItemCapability<IRadiationShielding, Void> RADIATION_SHIELDING = ItemCapability.createVoid(Mekanism.rl("radiation_shielding"), IRadiationShielding.class);

    public static final EntityCapability<IRadiationEntity, Void> RADIATION_ENTITY = EntityCapability.createVoid(Mekanism.rl("radiation"), IRadiationEntity.class);

    public static final Identifier OWNER_OBJECT_NAME = Mekanism.rl("owner_object");
    public static final Identifier SECURITY_OBJECT_NAME = Mekanism.rl("security_object");

    public static void registerProxyableCapabilities(RegisterCapabilitiesEvent event) {
        event.setProxyable(CHEMICAL_LEGACY.block());
        event.setProxyable(STRICT_ENERGY.block());
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        Mekanism.hooks.hookCapabilityRegistration(event);

        EntityType<EntityRobit> robitEntityType = MekanismEntityTypes.ROBIT.get();
        event.registerEntity(IEntitySecurityUtils.INSTANCE.ownerCapability(), robitEntityType, (robit, ctx) -> robit);
        event.registerEntity(IEntitySecurityUtils.INSTANCE.securityCapability(), robitEntityType, (robit, ctx) -> robit);
        EnergyCompatUtils.registerEntityCapabilities(event, robitEntityType, (robit, ctx) -> robit);

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            //Note: The jvm will reuse the lambda between types
            event.registerEntity(RADIATION_ENTITY, entityType, (entity, ctx) -> entity instanceof LivingEntity living ? new RadiationEntity(living) : null);
        }

        //Register bounding block proxies
        TileEntityBoundingBlock.alwaysProxyCapability(event, CONFIG_CARD);
        TileEntityBoundingBlock.alwaysProxyCapability(event, IBlockSecurityUtils.INSTANCE.ownerCapability());
        TileEntityBoundingBlock.alwaysProxyCapability(event, IBlockSecurityUtils.INSTANCE.securityCapability());
        //Capabilities we need to proxy because some sub implementations use them
        ComputerCapabilityHelper.addBoundingComputerCapabilities(event);
        TileEntityBoundingBlock.proxyCapability(event, ITEM.block());
        for (BlockCapability<?, @Nullable Direction> capability : EnergyCompatUtils.getLoadedEnergyCapabilities()) {
            TileEntityBoundingBlock.proxyCapability(event, capability);
        }
        //Note: Common caps we may eventually want to proxy but currently have no use for doing so
        TileEntityBoundingBlock.proxyCapability(event, FLUID.block());
        TileEntityBoundingBlock.proxyCapability(event, CHEMICAL_LEGACY.block());
        TileEntityBoundingBlock.proxyCapability(event, HEAT);
    }
}