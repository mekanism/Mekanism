package mekanism.common.tile.multiblock;

import mekanism.common.Mekanism;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.FluidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityDynamicTank extends TileEntityMultiblock<TankMultiblockData> implements IFluidContainerManager {

    public TileEntityDynamicTank(BlockPos pos, BlockState state) {
        this(MekanismBlocks.DYNAMIC_TANK, pos, state);
    }

    public TileEntityDynamicTank(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public ItemInteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack) {
        if (!player.isShiftKeyDown()) {
            TankMultiblockData multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                if (manageInventory(multiblock, player, hand, stack)) {
                    player.getInventory().setChanged();
                    return ItemInteractionResult.SUCCESS;
                }
                InteractionResult result = openGui(player);
                return switch (result) {
                    case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
                    case CONSUME -> ItemInteractionResult.CONSUME;
                    case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
                    case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    case FAIL -> ItemInteractionResult.FAIL;
                };
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @NotNull
    @Override
    public TankMultiblockData createMultiblock() {
        return new TankMultiblockData(this);
    }

    @Override
    public MultiblockManager<TankMultiblockData> getManager() {
        return Mekanism.tankManager;
    }

    @Override
    public ContainerEditMode getContainerEditMode() {
        return getMultiblock().editMode;
    }

    @Override
    public void nextMode() {
        TankMultiblockData multiblock = getMultiblock();
        multiblock.setContainerEditMode(multiblock.editMode.getNext());
    }

    @Override
    public void previousMode() {
        TankMultiblockData multiblock = getMultiblock();
        multiblock.setContainerEditMode(multiblock.editMode.getPrevious());
    }

    private boolean manageInventory(TankMultiblockData multiblock, Player player, InteractionHand hand, ItemStack itemStack) {
        if (multiblock.isFormed()) {
            return FluidUtils.handleTankInteraction(player, hand, itemStack, multiblock.getFluidTank());
        }
        return false;
    }
}