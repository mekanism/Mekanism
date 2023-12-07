package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeComputerIntegration;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.ComputerCapabilityHelper;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

public class TileEntityTypeDeferredRegister extends MekanismDeferredRegister<BlockEntityType<?>> {

    private final List<TileEntityTypeRegistryObject<?>> allTiles = new ArrayList<>();

    public TileEntityTypeDeferredRegister(String modid) {
        //Note: We intentionally don't pass a more restrictive type for holder creation as we ignore the holder that gets created
        // in favor of one we create ourselves
        super(Registries.BLOCK_ENTITY_TYPE, modid);
    }

    public <BE extends TileEntityMekanism> TileEntityTypeRegistryObject<BE> register(BlockRegistryObject<?, ?> block, BlockEntitySupplier<BE> factory) {
        return mekBuilder(block, factory).build();
    }

    public <BE extends TileEntityMekanism> BlockEntityTypeBuilder<BE> caplessMekBuilder(BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory) {
        return new BlockEntityTypeBuilder<BE>(block, factory)
              .clientTicker(TileEntityMekanism::tickClient)
              .serverTicker(TileEntityMekanism::tickServer);
    }

    public <BE extends TileEntityMekanism> BlockEntityTypeBuilder<BE> mekBuilder(BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory) {
        BooleanSupplier hasSecurity = () -> Attribute.has(block.getBlock(), AttributeSecurity.class);
        BlockEntityTypeBuilder<BE> builder = this.<BE>caplessMekBuilder(block, factory)
              //Delay the attachment of these and only attach them if we know they should be exposed rather than filtering in the provider itself
              .withSimple(IBlockSecurityUtils.INSTANCE.ownerCapability(), hasSecurity)
              .withSimple(IBlockSecurityUtils.INSTANCE.securityCapability(), hasSecurity)
              //TODO: Eventually see if we can come up with a way to avoid attaching providers to BEs that can never have one of the following types
              .with(Capabilities.GAS_HANDLER.block(), CapabilityTileEntity.GAS_HANDLER_PROVIDER)
              .with(Capabilities.INFUSION_HANDLER.block(), CapabilityTileEntity.INFUSION_HANDLER_PROVIDER)
              .with(Capabilities.PIGMENT_HANDLER.block(), CapabilityTileEntity.PIGMENT_HANDLER_PROVIDER)
              .with(Capabilities.SLURRY_HANDLER.block(), CapabilityTileEntity.SLURRY_HANDLER_PROVIDER)
              .with(Capabilities.HEAT_HANDLER.block(), CapabilityTileEntity.HEAT_HANDLER_PROVIDER)
              .with(Capabilities.ITEM.block(), CapabilityTileEntity.ITEM_HANDLER_PROVIDER)
              .with(FluidHandler.BLOCK, CapabilityTileEntity.FLUID_HANDLER_PROVIDER);
        EnergyCompatUtils.addBlockCapabilities(builder);
        if (Mekanism.hooks.computerCompatEnabled()) {
            ComputerCapabilityHelper.addComputerCapabilities(builder, () -> Attribute.has(block.getBlock(), AttributeComputerIntegration.class));
        }
        return builder;
    }

    public <BE extends BlockEntity> BlockEntityTypeBuilder<BE> builder(BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory) {
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

    public class BlockEntityTypeBuilder<BE extends BlockEntity> {

        private final BlockRegistryObject<?, ?> block;
        private final BlockEntityType.BlockEntitySupplier<? extends BE> factory;
        private final TileEntityTypeRegistryObject<BE> registryObject;

        BlockEntityTypeBuilder(BlockRegistryObject<?, ?> block, BlockEntityType.BlockEntitySupplier<? extends BE> factory) {
            this.block = block;
            this.factory = factory;
            this.registryObject = new TileEntityTypeRegistryObject<>(new ResourceLocation(getNamespace(), block.getName()));
        }

        public <CAP, CONTEXT> BlockEntityTypeBuilder<BE> withSimple(BlockCapability<CAP, CONTEXT> capability) {
            return withSimple(capability, ConstantPredicates.ALWAYS_TRUE);
        }

        @SuppressWarnings("unchecked")
        public <CAP, CONTEXT> BlockEntityTypeBuilder<BE> withSimple(BlockCapability<CAP, CONTEXT> capability, BooleanSupplier shouldApply) {
            return with(capability, (ICapabilityProvider<? super BE, CONTEXT, CAP>) Capabilities.SIMPLE_PROVIDER, shouldApply);
        }

        public <CAP, CONTEXT> BlockEntityTypeBuilder<BE> with(BlockCapability<CAP, CONTEXT> capability,
              Function<BlockCapability<CAP, CONTEXT>, ICapabilityProvider<? super BE, CONTEXT, CAP>> provider) {
            return with(capability, provider.apply(capability));
        }

        public <CAP, CONTEXT> BlockEntityTypeBuilder<BE> with(BlockCapability<CAP, CONTEXT> capability, ICapabilityProvider<? super BE, CONTEXT, CAP> provider) {
            return with(capability, provider, ConstantPredicates.ALWAYS_TRUE);
        }

        /**
         * @param shouldApply Determines whether the provider actually be attached to this block entity type. Useful for cases when we want to conditionally apply it
         *                    based on loaded mods or a block's attributes.
         */
        public <CAP, CONTEXT> BlockEntityTypeBuilder<BE> with(BlockCapability<CAP, CONTEXT> capability, ICapabilityProvider<? super BE, CONTEXT, CAP> provider,
              BooleanSupplier shouldApply) {
            registryObject.addCapability(capability, provider, shouldApply);
            return this;
        }

        public BlockEntityTypeBuilder<BE> without(BlockCapability<?, ?>... capabilities) {
            for (BlockCapability<?, ?> capability : capabilities) {
                registryObject.removeCapability(capability);
            }
            return this;
        }

        public BlockEntityTypeBuilder<BE> without(Collection<? extends BlockCapability<?, ?>> capabilities) {
            for (BlockCapability<?, ?> capability : capabilities) {
                registryObject.removeCapability(capability);
            }
            return this;
        }

        public BlockEntityTypeBuilder<BE> clientTicker(BlockEntityTicker<BE> ticker) {
            registryObject.clientTicker(ticker);
            return this;
        }

        public BlockEntityTypeBuilder<BE> serverTicker(BlockEntityTicker<BE> ticker) {
            registryObject.serverTicker(ticker);
            return this;
        }

        public BlockEntityTypeBuilder<BE> commonTicker(BlockEntityTicker<BE> ticker) {
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
}