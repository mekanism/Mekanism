package mekanism.common.capabilities;

import java.util.function.BooleanSupplier;
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
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.lib.radiation.capability.RadiationEntity;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Capabilities {//TODO - 1.20.2: Figure out which of these types actually need to be multi type

    private Capabilities() {
    }

    public static ICapabilityProvider<?, ?, ?> SIMPLE_PROVIDER = (obj, context) -> obj;

    //TODO - 1.20.2: Do we want to somehow proxy the fluid ones? Would be more difficult given the different types
    public static final MultiTypeCapability<IEnergyStorage> ENERGY = new MultiTypeCapability<>(EnergyStorage.BLOCK, EnergyStorage.ITEM, EnergyStorage.ENTITY);
    //Note: We intentionally don't use the entity automation capability, as we want to be able to target player inventories and the like
    public static final MultiTypeCapability<IItemHandler> ITEM = new MultiTypeCapability<>(ItemHandler.BLOCK, ItemHandler.ITEM, ItemHandler.ENTITY);

    //TODO - 1.20.2: Remove handler from the field names? Or add it to the proxied ones
    public static final MultiTypeCapability<IGasHandler> GAS_HANDLER = new MultiTypeCapability<>(Mekanism.rl("gas_handler"), IGasHandler.class);
    public static final MultiTypeCapability<IInfusionHandler> INFUSION_HANDLER = new MultiTypeCapability<>(Mekanism.rl("infusion_handler"), IInfusionHandler.class);
    public static final MultiTypeCapability<IPigmentHandler> PIGMENT_HANDLER = new MultiTypeCapability<>(Mekanism.rl("pigment_handler"), IPigmentHandler.class);
    public static final MultiTypeCapability<ISlurryHandler> SLURRY_HANDLER = new MultiTypeCapability<>(Mekanism.rl("slurry_handler"), ISlurryHandler.class);

    public static final MultiTypeCapability<IHeatHandler> HEAT_HANDLER = new MultiTypeCapability<>(Mekanism.rl("heat_handler"), IHeatHandler.class);

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

    //TODO - 1.20.2: Listen to this event
    // Also should this be in its own class?
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        Mekanism.hooks.hookCapabilityRegistration();
        //TODO - 1.20.2: Do we want robits to expose an energy cap
        event.registerEntity(IEntitySecurityUtils.INSTANCE.ownerCapability(), MekanismEntityTypes.ROBIT.get(), (robit, ctx) -> robit);
        event.registerEntity(IEntitySecurityUtils.INSTANCE.securityCapability(), MekanismEntityTypes.ROBIT.get(), (robit, ctx) -> robit);

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            //TODO - 1.20.2: Can this lambda be shared between entity types?
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
        //TODO: Register bounding block proxies
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

    public record MultiTypeCapability<HANDLER>(BlockCapability<HANDLER, @Nullable Direction> block,
                                               ItemCapability<HANDLER, Void> item,
                                               EntityCapability<HANDLER, ?> entity) {

        public MultiTypeCapability(ResourceLocation name, Class<HANDLER> handlerClass) {
            this(
                  BlockCapability.createSided(name, handlerClass),
                  ItemCapability.createVoid(name, handlerClass),
                  EntityCapability.createVoid(name, handlerClass)
            );
        }

        public boolean is(BlockCapability<?, ?> capability) {
            return capability == block();
        }

        @Nullable
        public HANDLER getCapability(ItemStack stack) {
            //Note: Safety handling of empty stack is done when looking up the provider inside getCapability's implementation
            return stack.getCapability(item());
        }

        /**
         * @apiNote Only use this helper if you don't actually need the capability, otherwise prefer using {@link #getCapability(ItemStack)} and null checking.
         */
        public boolean hasCapability(ItemStack stack) {
            return getCapability(stack) != null;
        }

        @Nullable
        public HANDLER getCapability(@Nullable Entity entity) {
            return entity == null ? null : entity.getCapability(entity(), null);
        }

        @Nullable
        public HANDLER getCapability(@NotNull Level level, @NotNull BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity tile, @Nullable Direction side) {
            //TODO: Should this use the ifLoaded variant?
            return level.getCapability(block(), pos, state, tile, side);
        }

        @Nullable
        public HANDLER getCapabilityIfLoaded(@Nullable Level level, @NotNull BlockPos pos, @Nullable Direction side) {
            return getCapabilityIfLoaded(level, pos, null, null, side);
        }

        @Nullable
        public HANDLER getCapabilityIfLoaded(@Nullable Level level, @NotNull BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity,
              @Nullable Direction side) {
            return WorldUtils.getCapability(level, block(), pos, state, blockEntity, side);
        }

        @Nullable
        public HANDLER getCapability(@Nullable BlockEntity blockEntity, @Nullable Direction side) {
            //TODO - 1.20.2: Is there actually a use for this or do we only want block specific ones
            // There is a decent chance we should transition a good number of usages of this to not using it
            if (blockEntity != null) {
                return getCapabilityIfLoaded(blockEntity.getLevel(), blockEntity.getBlockPos(), null, blockEntity, side);
            }
            return null;
        }

        public BlockCapabilityCache<HANDLER, @Nullable Direction> createCache(ServerLevel level, BlockPos pos, @Nullable Direction context, BooleanSupplier isValid,
              Runnable invalidationListener) {
            return BlockCapabilityCache.create(block(), level, pos, context, isValid, invalidationListener);
        }
    }
}