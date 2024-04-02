package mekanism.common.item.block;

import java.util.List;
import java.util.Optional;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.BlockData;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

public class ItemBlockCardboardBox extends ItemBlockMekanism<BlockCardboardBox> {

    public ItemBlockCardboardBox(BlockCardboardBox block) {
        super(block, new Item.Properties().stacksTo(16));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        Optional<BlockData> existingData = stack.getExistingData(MekanismAttachmentTypes.BLOCK_DATA);
        tooltip.add(MekanismLang.BLOCK_DATA.translateColored(EnumColor.INDIGO, YesNo.of(existingData.isPresent(), true)));
        //noinspection OptionalIsPresent - Capturing lambda
        if (existingData.isPresent()) {
            existingData.get().addToTooltip(tooltip::add);
        }
    }

    private static boolean canReplace(Level world, Player player, BlockPos pos, Direction sideClicked, BlockState state, ItemStack stack) {
        //Check if the player is allowed to use the cardboard box in the given position
        if (world.mayInteract(player, pos) && player.mayUseItemAt(pos.relative(sideClicked), sideClicked, stack)) {
            //If they are then check if they can "break" the block that is in that spot
            if (!NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, state, player)).isCanceled()) {
                //If they can then we need to see if they are allowed to "place" the cardboard box in the given position
                //TODO: Once forge fixes https://github.com/MinecraftForge/MinecraftForge/issues/7609 use block snapshots
                // and fire a place event to see if the player is able to "place" the cardboard box
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (stack.isEmpty() || player == null) {
            return InteractionResult.PASS;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!stack.hasData(MekanismAttachmentTypes.BLOCK_DATA) && !player.isShiftKeyDown()) {
            BlockState state = world.getBlockState(pos);
            if (!state.isAir() && state.getDestroySpeed(world, pos) != -1) {
                if (state.is(MekanismTags.Blocks.CARDBOARD_BLACKLIST) ||
                    MekanismConfig.general.cardboardModBlacklist.get().contains(RegistryUtils.getNamespace(state.getBlock())) ||
                    !canReplace(world, player, pos, context.getClickedFace(), state, stack)) {
                    return InteractionResult.FAIL;
                }
                BlockEntity tile = WorldUtils.getTileEntity(world, pos);
                if (tile != null && RegistryUtils.getBEHolder(tile.getType()).is(MekanismTags.TileEntityTypes.CARDBOARD_BLACKLIST)) {
                    //If the tile is in the tile entity type blacklist don't allow them to pick it up with a cardboard box
                    return InteractionResult.FAIL;
                }
                if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                    //If the tile is in the tile entity type blacklist or the player cannot access the tile
                    // don't allow them to pick it up with a cardboard box
                    return InteractionResult.FAIL;
                }
                if (!world.isClientSide) {
                    BlockData data = BlockData.create(state, tile);
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    //Mark that we are monitoring item drops that might have been created due to using the cardboard box
                    // and then replace the block with the cardboard box, which will cause items to drop and then get
                    // cancelled by our listener in CommonWorldTickHandler
                    CommonWorldTickHandler.monitoringCardboardBox = true;
                    world.setBlockAndUpdate(pos, getBlock().defaultBlockState().setValue(BlockStateHelper.storageProperty, true));
                    CommonWorldTickHandler.monitoringCardboardBox = false;
                    TileEntityCardboardBox box = WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
                    if (box != null) {
                        box.setData(MekanismAttachmentTypes.BLOCK_DATA, data);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}