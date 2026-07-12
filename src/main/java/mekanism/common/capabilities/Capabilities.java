package mekanism.common.capabilities;

import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IConfigurable;
import mekanism.api.IEvaporationSolar;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.lasers.ILaserDissipation;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.api.radiation.capability.IRadiationShielding;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class Capabilities {

    private Capabilities() {
    }

    public static final ICapabilityProvider<?, ?, ?> SIMPLE_PROVIDER = (obj, _) -> obj;

    public static final MultiTypeCapability<EnergyHandler> ENERGY = new MultiTypeCapability<>(Energy.BLOCK, Energy.ITEM, Energy.ENTITY);
    public static final MultiTypeCapability<ResourceHandler<FluidResource>> FLUID = new MultiTypeCapability<>(Fluid.BLOCK, Fluid.ITEM, Fluid.ENTITY);
    //Note: We intentionally don't use the entity automation capability, as we want to be able to target player inventories and the like
    public static final MultiTypeCapability<ResourceHandler<ItemResource>> ITEM = new MultiTypeCapability<>(Item.BLOCK, Item.ITEM, Item.ENTITY);

    public static final MultiTypeCapability<ResourceHandler<ChemicalResource>> CHEMICAL = new MultiTypeCapability<>(Mekanism.rl("chemical_handler"), ResourceHandler.asClass());

    public static final BlockCapability<IHeatHandler, @Nullable Direction> HEAT = BlockCapability.createSided(Mekanism.rl("heat_handler"), IHeatHandler.class);

    public static final BlockCapability<IConfigurable, @Nullable Direction> CONFIGURABLE = BlockCapability.createSided(Mekanism.rl("configurable"), IConfigurable.class);

    public static final BlockCapability<IAlloyInteraction, @Nullable Direction> ALLOY_INTERACTION = BlockCapability.createSided(Mekanism.rl("alloy_interaction"), IAlloyInteraction.class);

    public static final BlockCapability<IConfigCardAccess, @Nullable Direction> CONFIG_CARD = BlockCapability.createSided(Mekanism.rl("config_card"), IConfigCardAccess.class);

    public static final BlockCapability<IEvaporationSolar, @Nullable Void> EVAPORATION_SOLAR = BlockCapability.createVoid(Mekanism.rl("evaporation_solar"), IEvaporationSolar.class);

    public static final BlockCapability<ILaserReceptor, @Nullable Direction> LASER_RECEPTOR = BlockCapability.createSided(Mekanism.rl("laser_receptor"), ILaserReceptor.class);

    public static final ItemCapability<ILaserDissipation, @Nullable Void> LASER_DISSIPATION = ItemCapability.createVoid(Mekanism.rl("laser_dissipation"), ILaserDissipation.class);

    public static final ItemCapability<IRadiationShielding, @Nullable Void> RADIATION_SHIELDING = ItemCapability.createVoid(Mekanism.rl("radiation_shielding"), IRadiationShielding.class);

    public static final EntityCapability<IRadiationEntity, @Nullable Void> RADIATION_ENTITY = EntityCapability.createVoid(Mekanism.rl("radiation"), IRadiationEntity.class);

    public static final Identifier OWNER_OBJECT_NAME = Mekanism.rl("owner_object");
    public static final Identifier SECURITY_OBJECT_NAME = Mekanism.rl("security_object");

    public static void registerProxyableCapabilities(RegisterCapabilitiesEvent event) {
        event.setProxyable(CHEMICAL.block());
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        Mekanism.hooks.hookCapabilityRegistration(event);

        EntityType<EntityRobit> robitEntityType = MekanismEntityTypes.ROBIT.get();
        event.registerEntity(IEntitySecurityUtils.INSTANCE.ownerCapability(), robitEntityType, (robit, _) -> robit);
        event.registerEntity(IEntitySecurityUtils.INSTANCE.securityCapability(), robitEntityType, (robit, _) -> robit);
        event.registerEntity(ENERGY.entity(), robitEntityType, (robit, _) -> robit.getEnergyContainer());

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            //Note: The jvm will reuse the lambda between types
            event.registerEntity(RADIATION_ENTITY, entityType, (entity, _) -> entity instanceof LivingEntity living ? new RadiationEntity(living) : null);
        }

        //Register bounding block proxies
        TileEntityBoundingBlock.alwaysProxyCapability(event, CONFIG_CARD);
        TileEntityBoundingBlock.alwaysProxyCapability(event, IBlockSecurityUtils.INSTANCE.ownerCapability());
        TileEntityBoundingBlock.alwaysProxyCapability(event, IBlockSecurityUtils.INSTANCE.securityCapability());
        //Capabilities we need to proxy because some sub implementations use them
        TileEntityBoundingBlock.proxyCapability(event, ITEM.block());
        TileEntityBoundingBlock.proxyCapability(event, ENERGY.block());
        //Note: Common caps we may eventually want to proxy but currently have no use for doing so
        TileEntityBoundingBlock.proxyCapability(event, FLUID.block());
        TileEntityBoundingBlock.proxyCapability(event, CHEMICAL.block());
        TileEntityBoundingBlock.proxyCapability(event, HEAT);
    }
}