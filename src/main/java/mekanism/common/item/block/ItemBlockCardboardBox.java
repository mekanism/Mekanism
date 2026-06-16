package mekanism.common.item.block;

import java.util.function.Consumer;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.MekanismLang;
import mekanism.common.component.BlockData;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

public class ItemBlockCardboardBox extends ItemBlockMekanism<BlockCardboardBox> {

    public ItemBlockCardboardBox(BlockCardboardBox block, Item.Properties properties) {
        super(block, properties.stacksTo(16));
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        BlockData existingData = stack.get(MekanismDataComponents.BLOCK_DATA);
        //TODO - 26.2: Can we somehow move whether it has block data or not to the data component? Likely would require adding a default component for NO block data
        tooltipAdder.accept(MekanismLang.BLOCK_DATA.translateColored(EnumColor.INDIGO, YesNo.of(existingData != null, true)));
        //TODO - 26.2: Make a Neo PR adding a supplier variant of this? so that mods don't have to call get?
        stack.addToTooltip(MekanismDataComponents.BLOCK_DATA.get(), context, tooltipDisplay, tooltipAdder, flag);
    }

    private static boolean canReplace(Level world, Player player, BlockPos pos, Direction sideClicked, BlockState state, ItemStack stack) {
        //Check if the player is allowed to use the cardboard box in the given position
        if (world.mayInteract(player, pos) && player.mayUseItemAt(pos.relative(sideClicked), sideClicked, stack)) {
            //If they are then check if they can "break" the block that is in that spot
            //TODO - 26.2: Check about if we need to fire this on the client as well, or maybe just default mark it as notifying the client?
            if (!NeoForge.EVENT_BUS.post(new BreakBlockEvent(world, pos, state, player)).isCanceled()) {
                //If they can then we need to see if they are allowed to "place" the cardboard box in the given position
                //TODO: Once forge fixes https://github.com/MinecraftForge/MinecraftForge/issues/7609 use block snapshots
                // and fire a place event to see if the player is able to "place" the cardboard box
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (stack.isEmpty() || player == null) {
            return InteractionResult.PASS;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!stack.has(MekanismDataComponents.BLOCK_DATA) && !player.isShiftKeyDown()) {
            BlockState state = world.getBlockState(pos);
            if (!state.isAir() && state.getDestroySpeed(world, pos) != Block.INDESTRUCTIBLE) {
                if (state.is(MekanismTags.Blocks.CARDBOARD_BLACKLIST)) {
                    return InteractionResult.FAIL;
                }
                Identifier stateName = RegistryUtils.getName(state.typeHolder());
                if (stateName == null || MekanismConfig.general.cardboardModBlacklist.get().contains(stateName.getNamespace()) ||
                    !canReplace(world, player, pos, context.getClickedFace(), state, stack)) {
                    return InteractionResult.FAIL;
                }
                BlockEntity tile = WorldUtils.getTileEntity(world, pos);
                if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                    //If the tile is in the tile entity type blacklist or the player cannot access the tile
                    // don't allow them to pick it up with a cardboard box
                    return InteractionResult.FAIL;
                }
                if (!world.isClientSide()) {
                    BlockData data = new BlockData(world.registryAccess(), state, tile);
                    stack.consume(1, player);
                    //Mark that we are monitoring item drops that might have been created due to using the cardboard box
                    // and then replace the block with the cardboard box, which will cause items to drop and then get
                    // cancelled by our listener in CommonWorldTickHandler
                    CommonWorldTickHandler.monitoringCardboardBox = true;
                    world.setBlock(pos, getBlock().defaultBlockState().setValue(BlockStateHelper.storageProperty, true), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                    CommonWorldTickHandler.monitoringCardboardBox = false;
                    TileEntityCardboardBox box = WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
                    if (box != null) {
                        box.setComponents(DataComponentMap.builder()
                              .addAll(box.components())
                              .set(MekanismDataComponents.BLOCK_DATA, data)
                              .build());
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}