package mekanism.common.registration.impl;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
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
import mekanism.common.registration.impl.TileEntityTypeRegistryObject.CapabilityData;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityTypeDeferredRegister extends MekanismDeferredRegister<BlockEntityType<?>> {

    public TileEntityTypeDeferredRegister(String modid) {
        super(Registries.BLOCK_ENTITY_TYPE, modid, TileEntityTypeRegistryObject::new);
    }

    public <BE extends TileEntityMekanism> BlockEntityTypeBuilder<BE> mekBuilder(DeferredHolder<Block, ?> block, BlockEntitySupplier<? extends BE> factory) {
        BooleanSupplier hasSecurity = () -> Attribute.has(block, AttributeSecurity.class);
        BlockEntityTypeBuilder<BE> builder = this.<BE>builder(block, factory)
              //Delay the attachment of these and only attach them if we know they should be exposed rather than filtering in the provider itself
              .withSimple(IBlockSecurityUtils.INSTANCE.ownerCapability(), hasSecurity)
              .withSimple(IBlockSecurityUtils.INSTANCE.securityCapability(), hasSecurity)
              //TODO: Eventually see if we can come up with a way to avoid attaching providers to BEs that can never have one of the following types
              .with(Capabilities.CHEMICAL.block(), CapabilityTileEntity.CHEMICAL_HANDLER_PROVIDER)
              .with(Capabilities.HEAT, CapabilityTileEntity.HEAT_HANDLER_PROVIDER)
              .with(Capabilities.ITEM.block(), CapabilityTileEntity.ITEM_HANDLER_PROVIDER)
              .with(Capabilities.FLUID.block(), CapabilityTileEntity.FLUID_HANDLER_PROVIDER);
        EnergyCompatUtils.addBlockCapabilities(builder);
        if (Mekanism.hooks.computerCompatEnabled()) {
            ComputerCapabilityHelper.addComputerCapabilities(builder, () -> Attribute.has(block, AttributeComputerIntegration.class));
        }
        return builder;
    }

    public <BE extends BlockEntity> BlockEntityTypeBuilder<BE> builder(DeferredHolder<Block, ?> block, BlockEntitySupplier<? extends BE> factory) {
        return new BlockEntityTypeBuilder<>(block, factory);
    }

    @SuppressWarnings("unchecked")
    private <BE extends BlockEntity> TileEntityTypeRegistryObject<BE> registerMek(String name, Supplier<? extends BlockEntityType<BE>> sup) {
        return (TileEntityTypeRegistryObject<BE>) super.register(name, sup);
    }

    @Override
    public void register(@NotNull IEventBus bus) {
        super.register(bus);
        bus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Holder<BlockEntityType<?>> entry : getEntries()) {
            //Note: All entries should be of this type
            if (entry instanceof TileEntityTypeRegistryObject<?> tileRO) {
                tileRO.registerCapabilityProviders(event);
            } else if (!FMLEnvironment.production) {
                throw new IllegalStateException("Expected entry to be a TileEntityTypeRegistryObject");
            }
        }
    }

    public class BlockEntityTypeBuilder<BE extends BlockEntity> {

        private final DeferredHolder<Block, ?> block;
        private final BlockEntityType.BlockEntitySupplier<? extends BE> factory;
        private final List<CapabilityData<BE, ?, ?>> capabilityProviders = new ArrayList<>();
        @Nullable
        private BlockEntityTicker<BE> clientTicker;
        @Nullable
        private BlockEntityTicker<BE> serverTicker;

        BlockEntityTypeBuilder(DeferredHolder<Block, ?> block, BlockEntityType.BlockEntitySupplier<? extends BE> factory) {
            this.block = block;
            this.factory = factory;
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
            capabilityProviders.add(new CapabilityData<>(capability, provider, shouldApply));
            return this;
        }

        public BlockEntityTypeBuilder<BE> without(BlockCapability<?, ?>... capabilities) {
            for (BlockCapability<?, ?> capability : capabilities) {
                //noinspection Java8CollectionRemoveIf - We can't replace it with removeIf as it has a capturing lambda
                for (Iterator<CapabilityData<BE, ?, ?>> iterator = capabilityProviders.iterator(); iterator.hasNext(); ) {
                    if (iterator.next().capability() == capability) {
                        iterator.remove();
                    }
                }
            }
            return this;
        }

        public BlockEntityTypeBuilder<BE> without(Collection<? extends BlockCapability<?, ?>> capabilities) {
            //noinspection Java8CollectionRemoveIf - We can't replace it with removeIf as it has a capturing lambda
            for (Iterator<CapabilityData<BE, ?, ?>> iterator = capabilityProviders.iterator(); iterator.hasNext(); ) {
                if (capabilities.contains(iterator.next().capability())) {
                    iterator.remove();
                }
            }
            return this;
        }

        public BlockEntityTypeBuilder<BE> clientTicker(BlockEntityTicker<BE> ticker) {
            Preconditions.checkState(clientTicker == null, "Client ticker may only be set once.");
            clientTicker = ticker;
            return this;
        }

        public BlockEntityTypeBuilder<BE> serverTicker(BlockEntityTicker<BE> ticker) {
            Preconditions.checkState(serverTicker == null, "Server ticker may only be set once.");
            serverTicker = ticker;
            return this;
        }

        public BlockEntityTypeBuilder<BE> commonTicker(BlockEntityTicker<BE> ticker) {
            return clientTicker(ticker)
                  .serverTicker(ticker);
        }

        @SuppressWarnings("ConstantConditions")
        public TileEntityTypeRegistryObject<BE> build() {
            //Note: There is no data fixer type as forge does not currently have a way exposing data fixers to mods yet
            TileEntityTypeRegistryObject<BE> holder = registerMek(block.getId().getPath(), () -> BlockEntityType.Builder.<BE>of(factory, block.value()).build(null));
            holder.tickers(clientTicker, serverTicker);
            holder.capabilities(capabilityProviders.isEmpty() ? null : capabilityProviders);
            return holder;
        }
    }
}