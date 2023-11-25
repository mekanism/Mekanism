package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeComputerIntegration;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.ComputerCapabilityHelper;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.integration.energy.IEnergyCompat;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityTypeDeferredRegister extends MekanismDeferredRegister<BlockEntityType<?>> {

    //TODO: Do we want to use these for transmitters and then just add the handler in the constructor again?
    private static final ICapabilityProvider<? super TileEntityMekanism, @Nullable Direction, IGasHandler> GAS_HANDLER_PROVIDER = (tile, side) -> tile.getCapability(Capabilities.GAS_HANDLER.block(), side);
    private static final ICapabilityProvider<? super TileEntityMekanism, @Nullable Direction, IInfusionHandler> INFUSION_HANDLER_PROVIDER = (tile, side) -> tile.getCapability(Capabilities.INFUSION_HANDLER.block(), side);
    private static final ICapabilityProvider<? super TileEntityMekanism, @Nullable Direction, IPigmentHandler> PIGMENT_HANDLER_PROVIDER = (tile, side) -> tile.getCapability(Capabilities.PIGMENT_HANDLER.block(), side);
    private static final ICapabilityProvider<? super TileEntityMekanism, @Nullable Direction, ISlurryHandler> SLURRY_HANDLER_PROVIDER = (tile, side) -> tile.getCapability(Capabilities.SLURRY_HANDLER.block(), side);
    private static final ICapabilityProvider<? super TileEntityMekanism, @Nullable Direction, IHeatHandler> HEAT_HANDLER_PROVIDER = (tile, side) -> tile.getCapability(Capabilities.HEAT_HANDLER.block(), side);
    private static final ICapabilityProvider<? super TileEntityMekanism, @Nullable Direction, IItemHandler> ITEM_HANDLER_PROVIDER = (tile, side) -> tile.getCapability(Capabilities.ITEM.block(), side);
    private static final ICapabilityProvider<? super TileEntityMekanism, @Nullable Direction, IFluidHandler> FLUID_HANDLER_PROVIDER = (tile, side) -> tile.getCapability(FluidHandler.BLOCK, side);

    private final List<TileEntityTypeRegistryObject<?>> allTiles = new ArrayList<>();

    public TileEntityTypeDeferredRegister(String modid) {
        //Note: We intentionally don't pass a more restrictive type for holder creation as we ignore the holder that gets created
        // in favor of one we create ourselves
        super(Registries.BLOCK_ENTITY_TYPE, modid);
    }

    public <BE extends TileEntityMekanism> TileEntityTypeRegistryObject<BE> register(BlockRegistryObject<?, ?> block, BlockEntitySupplier<BE> factory) {
        return mekBuilder(block, factory).build();
    }

    public <BE extends TileEntityMekanism> MekBlockEntityTypeBuilder<BE> mekBuilder(BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory) {
        BooleanSupplier hasSecurity = () -> Attribute.has(block.getBlock(), AttributeSecurity.class);
        MekBlockEntityTypeBuilder<BE> builder = new MekBlockEntityTypeBuilder<BE>(block, factory)
              .clientTicker(TileEntityMekanism::tickClient)
              .serverTicker(TileEntityMekanism::tickServer)
              //Delay the attachment of these and only attach them if we know they should be exposed rather than filtering in the provider itself
              .withSimple(Capabilities.OWNER_OBJECT.block(), hasSecurity)
              .withSimple(Capabilities.SECURITY_OBJECT.block(), hasSecurity);
        //TODO: Evaluate if there is a better way to do this
        builder.with(Capabilities.GAS_HANDLER.block(), GAS_HANDLER_PROVIDER);
        builder.with(Capabilities.INFUSION_HANDLER.block(), INFUSION_HANDLER_PROVIDER);
        builder.with(Capabilities.PIGMENT_HANDLER.block(), PIGMENT_HANDLER_PROVIDER);
        builder.with(Capabilities.SLURRY_HANDLER.block(), SLURRY_HANDLER_PROVIDER);
        builder.with(Capabilities.HEAT_HANDLER.block(), HEAT_HANDLER_PROVIDER);
        builder.with(Capabilities.ITEM.block(), ITEM_HANDLER_PROVIDER);
        builder.with(FluidHandler.BLOCK, FLUID_HANDLER_PROVIDER);
        for (IEnergyCompat energyCompat : EnergyCompatUtils.getCompats()) {
            if (energyCompat.capabilityExists()) {
                addCapability(builder, energyCompat.getCapability().block());
            }
        }
        if (Mekanism.hooks.computerCompatEnabled()) {
            ComputerCapabilityHelper.addComputerCapabilities(builder, () -> Attribute.has(block.getBlock(), AttributeComputerIntegration.class));
        }
        return builder;
    }

    private static <CAP> void addCapability(MekBlockEntityTypeBuilder<?> builder, BlockCapability<CAP, @Nullable Direction> capability) {
        //TODO: Test this and debate if we would be better off instead using something like EnergyCompatUtils#registerBlockCapabilities
        builder.with(capability, TileEntityMekanism.getEnergyCapabilityProvider(capability));
    }

    public <BE extends BlockEntity> BlockEntityTypeBuilder<BE, ?> builder(BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory) {
        return new BlockEntityTypeBuilder<>(block, factory);
    }

    @Override
    public void register(@NotNull IEventBus bus) {
        super.register(bus);
        bus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (TileEntityTypeRegistryObject<?> tileRO : allTiles) {
            tileRO.registerCapabilityProviders(event);
        }
    }

    public class BlockEntityTypeBuilder<BE extends BlockEntity, BUILDER extends BlockEntityTypeBuilder<BE, BUILDER>> {

        private final BlockRegistryObject<?, ?> block;
        private final BlockEntityType.BlockEntitySupplier<? extends BE> factory;
        private final TileEntityTypeRegistryObject<BE> registryObject;

        BlockEntityTypeBuilder(BlockRegistryObject<?, ?> block, BlockEntityType.BlockEntitySupplier<? extends BE> factory) {
            this.block = block;
            this.factory = factory;
            this.registryObject = new TileEntityTypeRegistryObject<>(new ResourceLocation(getNamespace(), block.getName()));
        }

        @SuppressWarnings("unchecked")
        private BUILDER self() {
            return (BUILDER) this;
        }

        public <CAP, CONTEXT> BUILDER withSimple(BlockCapability<CAP, CONTEXT> capability) {
            return withSimple(capability, ConstantPredicates.ALWAYS_TRUE);
        }

        @SuppressWarnings("unchecked")
        public <CAP, CONTEXT> BUILDER withSimple(BlockCapability<CAP, CONTEXT> capability, BooleanSupplier shouldApply) {
            //TODO: Re-evaluate this method and the fact that it makes it so there isn't compile time validation of types??
            return with(capability, (ICapabilityProvider<? super BE, CONTEXT, CAP>) Capabilities.SIMPLE_PROVIDER, shouldApply);
        }

        public <CAP, CONTEXT> BUILDER with(BlockCapability<CAP, CONTEXT> capability, ICapabilityProvider<? super BE, CONTEXT, CAP> provider) {
            return with(capability, provider, ConstantPredicates.ALWAYS_TRUE);
        }

        public <CAP, CONTEXT> BUILDER with(BlockCapability<CAP, CONTEXT> capability, ICapabilityProvider<? super BE, CONTEXT, CAP> provider,
              BooleanSupplier shouldApply) {
            registryObject.addCapability(capability, provider, shouldApply);
            return self();
        }

        public BUILDER clientTicker(BlockEntityTicker<BE> ticker) {
            registryObject.clientTicker(ticker);
            return self();
        }

        public BUILDER serverTicker(BlockEntityTicker<BE> ticker) {
            registryObject.serverTicker(ticker);
            return self();
        }

        public BUILDER commonTicker(BlockEntityTicker<BE> ticker) {
            return clientTicker(ticker)
                  .serverTicker(ticker);
        }

        @SuppressWarnings("ConstantConditions")
        public TileEntityTypeRegistryObject<BE> build() {
            //Register the BE, but don't care about the returned holder as we already made the holder ourselves so that we could add extra data to it
            //Note: There is no data fixer type as forge does not currently have a way exposing data fixers to mods yet
            register(block.getName(), () -> BlockEntityType.Builder.<BE>of(factory, block.getBlock()).build(null));
            allTiles.add(registryObject);
            return registryObject;
        }
    }

    public class MekBlockEntityTypeBuilder<BE extends TileEntityMekanism> extends BlockEntityTypeBuilder<BE, MekBlockEntityTypeBuilder<BE>> {

        //private static ICapabilityProvider<TileEntityMekanism, @Nullable Direction, IConfigCardAccess> CONFIG_CARD_PROVIDER = (be, side) -> be;

        MekBlockEntityTypeBuilder(BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory) {
            super(block, factory);
        }

        public MekBlockEntityTypeBuilder<BE> withConfigCard() {
            return withSimple(Capabilities.CONFIG_CARD);
            //TODO: Test the above works
            //return with(Capabilities.CONFIG_CARD, CONFIG_CARD_PROVIDER);
        }

        //TODO:
        /*public MekBlockEntityTypeBuilder<BE> configurable() {
            return with(Capabilities.CONFIGURABLE, CONFIG_CARD_PROVIDER);
        }*/
    }
}