package mekanism.common.registration.impl;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import mekanism.api.security.SecurityMode;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes.AttributeRedstone;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registration.DoubleDeferredRegister;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockDeferredRegister extends DoubleDeferredRegister<Block, Item> {

    public BlockDeferredRegister(String modid) {
        //Note: We use our own deferred register so that we also can automatically attach any capability aware items we register
        super(modid, Registries.BLOCK, new ItemDeferredRegister(modid));
    }

    public BlockRegistryObject<Block, BlockItem> register(String name, BlockBehaviour.Properties properties) {
        return register(name, () -> new Block(BlockStateHelper.applyLightLevelAdjustments(properties)), BlockItem::new);
    }

    public <BLOCK extends Block> BlockRegistryObject<BLOCK, BlockItem> register(String name, Supplier<? extends BLOCK> blockSupplier) {
        return register(name, blockSupplier, BlockItem::new);
    }

    public <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerDetails(String name, Supplier<? extends BLOCK> blockSupplier) {
        return register(name, blockSupplier, (block, properties) -> new ItemBlockTooltip<>(block, true, properties));
    }

    public <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> register(String name, Supplier<? extends BLOCK> blockSupplier,
          BiFunction<BLOCK, Item.Properties, ITEM> itemCreator) {
        return registerAdvanced(name, blockSupplier, block -> {
            Item.Properties properties = new Item.Properties();
            if (Attribute.has(block, AttributeSecurity.class)) {
                properties.component(MekanismDataComponents.SECURITY, SecurityMode.PUBLIC);
            }
            if (Attribute.has(block, AttributeRedstone.class)) {
                properties.component(MekanismDataComponents.REDSTONE_CONTROL, RedstoneControl.DISABLED);
            }
            if (Attribute.has(block, AttributeUpgradeSupport.class)) {
                properties.component(MekanismDataComponents.UPGRADES, UpgradeAware.EMPTY);
            }
            return itemCreator.apply(block.get(), properties);
        }, BlockRegistryObject::new);
    }
}