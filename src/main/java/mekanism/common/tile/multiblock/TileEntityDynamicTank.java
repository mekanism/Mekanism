package mekanism.common.tile.multiblock;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.FluidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

public class TileEntityDynamicTank extends TileEntityMultiblock<TankMultiblockData> implements IFluidContainerManager {

    public TileEntityDynamicTank(BlockPos pos, BlockState state) {
        this(MekanismBlocks.DYNAMIC_TANK, pos, state);
        //Disable item handler caps if we are the dynamic tank, don't disable it for the subclassed valve though
        addDisabledCapabilities(ForgeCapabilities.ITEM_HANDLER);
    }

    public TileEntityDynamicTank(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public InteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack) {
        if (!player.isShiftKeyDown()) {
            TankMultiblockData multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                if (manageInventory(multiblock, player, hand, stack)) {
                    player.getInventory().setChanged();
                    return InteractionResult.SUCCESS;
                }
                return openGui(player);
            }
        }
        return InteractionResult.PASS;
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

    private boolean manageInventory(TankMultiblockData multiblock, Player player, InteractionHand hand, ItemStack itemStack) {
        if (multiblock.isFormed()) {
            return FluidUtils.handleTankInteraction(player, hand, itemStack, multiblock.getFluidTank());
        }
        return false;
    }
}