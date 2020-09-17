package mekanism.common.tile.multiblock;

import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.FluidUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityDynamicTank extends TileEntityMultiblock<TankMultiblockData> implements IFluidContainerManager {

    public TileEntityDynamicTank() {
        this(MekanismBlocks.DYNAMIC_TANK);
        //Disable item handler caps if we are the dynamic tank, don't disable it for the subclassed valve though
        addDisabledCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    public TileEntityDynamicTank(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (!player.isSneaking()) {
            TankMultiblockData multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                if (manageInventory(multiblock, player, hand, stack)) {
                    player.inventory.markDirty();
                    return ActionResultType.SUCCESS;
                }
                return openGui(player);
            }
        }
        return ActionResultType.PASS;
    }

    @Nonnull
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
        multiblock.editMode = multiblock.editMode.getNext();
    }

    private boolean manageInventory(TankMultiblockData multiblock, PlayerEntity player, Hand hand, ItemStack itemStack) {
        if (multiblock.isFormed()) {
            return FluidUtils.handleTankInteraction(player, hand, itemStack, multiblock.getFluidTank());
        }
        return false;
    }
}