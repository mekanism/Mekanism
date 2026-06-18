package mekanism.additions.client;

import java.util.List;
import java.util.Map;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.block.BlockGlowPanel;
import mekanism.additions.common.block.plastic.BlockPlasticFenceGate;
import mekanism.additions.common.block.plastic.BlockPlasticStairs;
import mekanism.additions.common.item.ItemBalloon;
import mekanism.additions.common.item.ItemWalkieTalkie;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.additions.common.registries.AdditionsDataComponents;
import mekanism.additions.common.registries.AdditionsItems;
import mekanism.api.text.EnumColor;
import mekanism.client.model.BaseModelProvider;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;

public class AdditionsModelProvider extends BaseModelProvider {

    public AdditionsModelProvider(PackOutput output, ResourceManager clientResources) {
        super(output, MekanismAdditions.MODID, clientResources);
    }

    private static ItemModel.Unbaked tintedModel(Identifier location, EnumColor color) {
        return ItemModelUtils.tintedModel(location, new Constant(color.getPackedColor()));
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

        //Identifier balloonModelLoc = modLocation("item/balloon");
        //ModelTemplate balloonParent = new ModelTemplate(Optional.of(balloonModelLoc), Optional.empty());
        Identifier latchedBalloonLoc = modLocation("item/balloon_latched");
        Identifier guiBalloonLoc = modLocation("item/balloon_gui");
        Identifier fixedBalloonLoc = modLocation("item/balloon_fixed");
        for (Map.Entry<EnumColor, ItemRegistryObject<ItemBalloon>> entry : AdditionsItems.BALLOONS.entrySet()) {
            EnumColor color = entry.getKey();
            Item balloon = entry.getValue().value();
            //Identifier generatedModel = balloonParent.create(ModelLocationUtils.getModelLocation(balloon), new TextureMapping(), itemModels.modelOutput);
            //TODO - 26.2: does this work, or does it need to define child model with parent? (previous line). check other usages if so
            //tintedItem(itemModels, balloon, balloonModelLoc, color);
            ItemModel.Unbaked modelToRegister = ItemModelUtils.select(
                  new DisplayContext(),
                  tintedModel(latchedBalloonLoc, color),
                  ItemModelUtils.when(ItemDisplayContext.GUI, tintedModel(guiBalloonLoc, color)),
                  ItemModelUtils.when(List.of(ItemDisplayContext.GROUND, ItemDisplayContext.FIXED), tintedModel(fixedBalloonLoc, color))
            );
            itemModels.itemModelOutput.accept(balloon, modelToRegister);
        }

        for (ItemRegistryObject<SpawnEggItem> babySpawnEgg : AdditionsItems.BABY_SPAWN_EGGS.values()) {
            itemModels.generateFlatItem(babySpawnEgg.value(), ModelTemplates.FLAT_ITEM);
        }

        Item walkieTalkie = AdditionsItems.WALKIE_TALKIE.value();
        ItemModel.Unbaked baseWalkie = ItemModelUtils.plainModel(itemModels.createFlatItemModel(walkieTalkie, ModelTemplates.FLAT_ITEM));
        itemModels.itemModelOutput.accept(
              walkieTalkie,
              ItemModelUtils.select(
                    new ComponentContents<>(AdditionsDataComponents.WALKIE_DATA.get()),
                    baseWalkie,
                    ItemWalkieTalkie.WalkieData.runningChannels()
                          .map(walkieData ->
                                new SelectItemModel.SwitchCase<>(
                                      List.of(walkieData),
                                      ItemModelUtils.plainModel(itemModels.createFlatItemModel(walkieTalkie, "_ch" + walkieData.channel(), ModelTemplates.FLAT_ITEM))
                                )
                          )
                          .toList()
              )
        );

        glowPanels(blockModels);
        coloredBlocks(blockModels, AdditionsBlocks.PLASTIC_BLOCKS, "block");
        coloredBlocks(blockModels, AdditionsBlocks.SLICK_PLASTIC_BLOCKS, "slick");
        coloredBlocks(blockModels, AdditionsBlocks.PLASTIC_GLOW_BLOCKS, "glow");
        coloredBlocks(blockModels, AdditionsBlocks.REINFORCED_PLASTIC_BLOCKS, "reinforced");
        coloredBlocks(blockModels, AdditionsBlocks.PLASTIC_ROADS, "road");
        coloredBlocks(blockModels, AdditionsBlocks.TRANSPARENT_PLASTIC_BLOCKS, "transparent");
        coloredSlabs(blockModels, AdditionsBlocks.PLASTIC_SLABS, "", "block");
        coloredStairs(blockModels, AdditionsBlocks.PLASTIC_STAIRS, "");
        coloredFences(blockModels, AdditionsBlocks.PLASTIC_FENCES, "");
        coloredFenceGates(blockModels, AdditionsBlocks.PLASTIC_FENCE_GATES, "");
        coloredSlabs(blockModels, AdditionsBlocks.PLASTIC_GLOW_SLABS, "glow_", "glow");
        coloredStairs(blockModels, AdditionsBlocks.PLASTIC_GLOW_STAIRS, "glow_");
        coloredSlabs(blockModels, AdditionsBlocks.TRANSPARENT_PLASTIC_SLABS, "transparent_", "transparent");
        coloredStairs(blockModels, AdditionsBlocks.TRANSPARENT_PLASTIC_STAIRS, "transparent_");

        markManualBlockState(AdditionsBlocks.OBSIDIAN_TNT);
    }

    private static void tintedItem(ItemModelGenerators itemModels, Item item, Identifier modelLoc, EnumColor color) {
        tintedItem(itemModels.itemModelOutput, item, modelLoc, color);
    }

    private static void tintedItem(ItemModelOutput itemModelOutput, Item item, Identifier modelLoc, EnumColor color) {
        itemModelOutput.accept(item, ItemModelUtils.tintedModel(modelLoc, new Constant(color.getPackedColor())));
    }

    private static void tintedItem(BlockModelGenerators blockModels, Block block, Identifier modelLoc, EnumColor color) {
        tintedItem(blockModels.itemModelOutput, block.asItem(), modelLoc, color);
    }

    private void glowPanels(BlockModelGenerators blockModels) {
        Identifier model = modLocation("block/glow_panel");
        for (BlockRegistryObject<BlockGlowPanel, ?> blockRO : AdditionsBlocks.GLOW_PANELS.values()) {
            BlockGlowPanel glowPanel = blockRO.value();
            blockModels.blockStateOutput.accept(
                  MultiVariantGenerator.dispatch(
                              glowPanel,
                              BlockModelGenerators.plainVariant(model)
                        )
                        .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING)
            );
            blockModels.itemModelOutput.accept(blockRO.asItem(), ItemModelUtils.tintedModel(model, new Constant(glowPanel.getColor().getPackedColor())));
        }
    }

    private void coloredBlocks(BlockModelGenerators blockModels, Map<EnumColor, ? extends Holder<Block>> blocks, String modelName) {
        Identifier model = modLocation("block/plastic/" + modelName);
        for (Map.Entry<EnumColor, ? extends Holder<Block>> entry : blocks.entrySet()) {
            Holder<Block> block = entry.getValue();
            blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block.value(), BlockModelGenerators.plainVariant(model)));
            tintedItem(blockModels, block.value(), model, entry.getKey());
        }
    }

    private void coloredSlabs(BlockModelGenerators blockModels, Map<EnumColor, ? extends Holder<Block>> slabs, String existingPrefix, String doubleType) {
        Identifier bottomModel = modLocation("block/plastic/" + existingPrefix + "slab");
        Identifier topModel = modLocation("block/plastic/" + existingPrefix + "slab_top");
        MultiVariant doubleModel = BlockModelGenerators.plainVariant(modLocation("block/plastic/" + doubleType));
        for (Map.Entry<EnumColor, ? extends Holder<Block>> entry : slabs.entrySet()) {
            Block slab = entry.getValue().value();
            MultiVariant top = BlockModelGenerators.plainVariant(topModel);
            blockModels.blockStateOutput
                  .accept(BlockModelGenerators.createSlab(slab, BlockModelGenerators.plainVariant(bottomModel), top, doubleModel));
            tintedItem(blockModels, slab, bottomModel, entry.getKey());
        }
    }

    private void coloredStairs(BlockModelGenerators blockModels, Map<EnumColor, ? extends BlockRegistryObject<? extends BlockPlasticStairs, ?>> stairs, String existingPrefix) {
        Identifier stairsModel = modLocation("block/plastic/" + existingPrefix + "stairs");
        MultiVariant stairsInner = BlockModelGenerators.plainVariant(modLocation("block/plastic/" + existingPrefix + "stairs_inner"));
        MultiVariant stairsOuter = BlockModelGenerators.plainVariant(modLocation("block/plastic/" + existingPrefix + "stairs_outer"));
        for (BlockRegistryObject<? extends BlockPlasticStairs, ?> stair : stairs.values()) {
            blockModels.blockStateOutput
                  .accept(BlockModelGenerators.createStairs(stair.value(), stairsInner, BlockModelGenerators.plainVariant(stairsModel), stairsOuter));
            tintedItem(blockModels, stair.value(), stairsModel, stair.get().getColor());
        }
    }

    private void coloredFences(BlockModelGenerators blockModels, Map<EnumColor, ? extends Holder<Block>> fences, String existingPrefix) {
        MultiVariant post = BlockModelGenerators.plainVariant(modLocation("block/plastic/" + existingPrefix + "fence_post"));
        MultiVariant side = BlockModelGenerators.plainVariant(modLocation("block/plastic/" + existingPrefix + "fence_side"));
        Identifier inventory = modLocation("block/plastic/fence_inventory");
        for (Map.Entry<EnumColor, ? extends Holder<Block>> entry : fences.entrySet()) {
            Block fence = entry.getValue().value();
            blockModels.blockStateOutput.accept(BlockModelGenerators.createFence(fence, post, side));
            tintedItem(blockModels, fence, inventory, entry.getKey());
        }
    }

    private void coloredFenceGates(BlockModelGenerators blockModels, Map<EnumColor, ? extends BlockRegistryObject<? extends BlockPlasticFenceGate, ?>> fenceGates, String existingPrefix) {
        Identifier gate = modLocation("block/plastic/" + existingPrefix + "fence_gate");
        MultiVariant open = BlockModelGenerators.plainVariant(modLocation("block/plastic/" + existingPrefix + "fence_gate_open"));
        MultiVariant closed = BlockModelGenerators.plainVariant(gate);
        MultiVariant openWall = BlockModelGenerators.plainVariant(modLocation("block/plastic/" + existingPrefix + "fence_gate_wall_open"));
        MultiVariant closedWall = BlockModelGenerators.plainVariant(modLocation("block/plastic/" + existingPrefix + "fence_gate_wall"));
        for (Map.Entry<EnumColor, ? extends Holder<Block>> entry : fenceGates.entrySet()) {
            Block fenceGate = entry.getValue().value();
            blockModels.blockStateOutput.accept(BlockModelGenerators.createFenceGate(fenceGate, open, closed, openWall, closedWall, true));
            tintedItem(blockModels, fenceGate, gate, entry.getKey());
        }
    }
}