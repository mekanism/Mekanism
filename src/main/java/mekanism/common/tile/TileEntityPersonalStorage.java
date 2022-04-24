package mekanism.common.tile;

import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NonNull;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityPersonalStorage extends TileEntityMekanism {

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
            TileEntityPersonalStorage.this.onOpen(level, pos, state);
        }

        @Override
        protected void onClose(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
            TileEntityPersonalStorage.this.onClose(level, pos, state);
        }

        @Override
        protected void openerCountChanged(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, int oldCount, int openCount) {
            level.blockEvent(pos, state.getBlock(), 1, openCount);
        }

        @Override
        protected boolean isOwnContainer(@Nonnull Player player) {
            return player.containerMenu instanceof MekanismTileContainer<?> container && container.getTileEntity() == TileEntityPersonalStorage.this;
        }
    };

    protected TileEntityPersonalStorage(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        //Note: We always allow manual interaction (even for insertion), as if a player has the GUI open we treat that as they are allowed to interact with it
        // and if the security mode changes we then boot any players who can't interact with it anymore out of the GUI
        //Note: We can just directly pass ourselves as a security object as we know we are present and that we aren't just an owner item
        BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInteract = (stack, automationType) ->
              automationType == AutomationType.MANUAL || MekanismAPI.getSecurityUtils().getEffectiveSecurityMode(this, isRemote()) == SecurityMode.PUBLIC;
        for (int slotY = 0; slotY < 6; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                //Note: we allow access to the slots from all sides as long as it is public, unlike in 1.12 where we always denied the bottom face
                // We did that to ensure that things like hoppers that could check IInventory did not bypass any restrictions
                builder.addSlot(BasicInventorySlot.at(canInteract, canInteract, listener, 8 + slotX * 18, 18 + slotY * 18));
            }
        }
        return builder.build();
    }

    @Override
    public void open(Player player) {
        super.open(player);
        if (!isRemoved() && !player.isSpectator() && level != null) {
            openersCounter.incrementOpeners(player, level, getBlockPos(), getBlockState());
        }
    }

    @Override
    public void close(Player player) {
        super.close(player);
        if (!isRemoved() && !player.isSpectator() && level != null) {
            openersCounter.decrementOpeners(player, level, getBlockPos(), getBlockState());
        }
    }

    public void recheckOpen() {
        if (!isRemoved() && level != null) {
            openersCounter.recheckOpeners(level, getBlockPos(), getBlockState());
        }
    }

    protected abstract void onOpen(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state);

    protected abstract void onClose(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state);

    protected abstract ResourceLocation getStat();

    @Override
    public InteractionResult openGui(Player player) {
        InteractionResult result = super.openGui(player);
        if (result.consumesAction() && !isRemote()) {
            player.awardStat(Stats.CUSTOM.get(getStat()));
            PiglinAi.angerNearbyPiglins(player, true);
        }
        return result;
    }
}