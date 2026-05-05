package mekanism.common.registration.impl;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
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
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class BlockDeferredRegister extends DoubleDeferredRegister<Block, Item> {

    public BlockDeferredRegister(String modid) {
        //Note: We use our own deferred register so that we also can automatically attach any capability aware items we register
        super(modid, Registries.BLOCK, new ItemDeferredRegister(modid));
    }

    public BlockRegistryObject<Block, BlockItem> registerSimple(String name, UnaryOperator<BlockBehaviour.Properties> propertyModifier) {
        //TODO: Do we care about always trying to apply light level adjustments here given none of our callers are fluid loggable?
        return register(name, properties -> new Block(BlockStateHelper.applyLightLevelAdjustments(propertyModifier.apply(properties))), BlockItem::new);
    }

    public <BLOCK extends Block> BlockRegistryObject<BLOCK, BlockItem> register(String name, Function<BlockBehaviour.Properties, ? extends BLOCK> blockCreator) {
        return register(name, blockCreator, BlockItem::new);
    }

    public <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerDetails(String name, Function<BlockBehaviour.Properties, ? extends BLOCK> blockCreator) {
        return register(name, blockCreator, (block, properties) -> new ItemBlockTooltip<>(block, true, properties));
    }

    public <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> register(String name, Function<BlockBehaviour.Properties, ? extends BLOCK> blockCreator,
          BiFunction<BLOCK, Item.Properties, ITEM> itemCreator) {
        return register(name, BlockBehaviour.Properties::of, blockCreator, itemCreator);
    }

    public <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> register(String name, Supplier<Properties> baseBlockProperties,
          Function<BlockBehaviour.Properties, ? extends BLOCK> blockCreator,
          BiFunction<BLOCK, Item.Properties, ITEM> itemCreator) {
        return registerAdvanced(name, key -> blockCreator.apply(baseBlockProperties.get().setId(createPrimaryId(key))), (key, block) -> {
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
            return itemCreator.apply(block.get(), properties.setId(createSecondaryId(key)).useBlockDescriptionPrefix());
        }, BlockRegistryObject::new);
    }
}