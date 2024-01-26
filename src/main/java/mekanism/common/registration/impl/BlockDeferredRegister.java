package mekanism.common.registration.impl;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registration.DoubleDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

public class BlockDeferredRegister extends DoubleDeferredRegister<Block, Item> {

    public BlockDeferredRegister(String modid) {
        super(modid, Registries.BLOCK, new ItemDeferredRegister(modid));
    }

    @Override
    public void register(@NotNull IEventBus bus) {
        super.register(bus);
        bus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        Capabilities.registerCapabilityAwareCapabilities(event, getSecondaryEntries());
    }

    public BlockRegistryObject<Block, BlockItem> register(String name, BlockBehaviour.Properties properties) {
        return registerDefaultProperties(name, () -> new Block(BlockStateHelper.applyLightLevelAdjustments(properties)), BlockItem::new);
    }

    public <BLOCK extends Block> BlockRegistryObject<BLOCK, BlockItem> register(String name, Supplier<? extends BLOCK> blockSupplier) {
        return registerDefaultProperties(name, blockSupplier, BlockItem::new);
    }

    public <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> registerDefaultProperties(String name, Supplier<? extends BLOCK> blockSupplier,
          BiFunction<BLOCK, Item.Properties, ITEM> itemCreator) {
        return register(name, blockSupplier, block -> itemCreator.apply(block, new Item.Properties()));
    }

    public <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> register(String name, Supplier<? extends BLOCK> blockSupplier,
          Function<BLOCK, ITEM> itemCreator) {
        return register(name, blockSupplier, itemCreator, BlockRegistryObject::new);
    }
}