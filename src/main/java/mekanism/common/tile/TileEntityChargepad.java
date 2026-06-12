package mekanism.common.tile;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.single.BasicSingleHolder;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnergyUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class TileEntityChargepad extends TileEntityMekanism {

    private static final Predicate<LivingEntity> CHARGE_PREDICATE = entity -> !entity.isSpectator() && (entity instanceof Player || entity instanceof EntityRobit);

    @UnknownNullability//Initialized via getInitialEnergyContainer
    private MachineEnergyContainer<TileEntityChargepad> energyContainer;

    public TileEntityChargepad(BlockPos pos, BlockState state) {
        super(MekanismBlocks.CHARGEPAD, pos, state);
    }

    @Override
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = MachineEnergyContainer.input(this, listener);
        return new BasicSingleHolder<>(energyContainer, facingSupplier, Set.of(RelativeSide.BACK, RelativeSide.BOTTOM));
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        boolean active = false;
        if (!energyContainer.isEmpty()) {
            //Use 0.4 for y to catch entities that are partially standing on the back pane
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                  worldPosition.getX() + 1, worldPosition.getY() + 0.4, worldPosition.getZ() + 1), CHARGE_PREDICATE);
            try (Transaction transaction = Transaction.openRoot()) {
                for (LivingEntity entity : entities) {
                    if (energyContainer.isEmpty()) {
                        //If we run out of energy, stop checking the remaining entities
                        break;
                    } else if (entity instanceof Player) {
                        if (chargeHandler(Capabilities.ITEM.getCapability(entity), transaction)) {
                            active = true;
                        } else if (Mekanism.hooks.curios.isLoaded()) {
                            //If we didn't charge anything in the inventory and curios is loaded try charging things in the curios slots
                            if (chargeHandler(CuriosIntegration.getCuriosInventory(entity), transaction)) {
                                active = true;
                            }
                        }
                    } else if (provideEnergy(Capabilities.ENERGY.getCapability(entity), transaction)) {
                        //Note: Robits are handled by this path
                        active = true;
                    }
                }
                if (active) {
                    transaction.commit();
                }
            }
        }
        setActive(active);
        return sendUpdatePacket;
    }

    private boolean chargeHandler(@Nullable ResourceHandler<ItemResource> itemHandler, TransactionContext transaction) {
        //Ensure that we have an item handler capability, because if for example the player is dead we will not
        if (itemHandler != null) {
            //TODO - 26.1: We are using this as a energy per target per tick limit rather than an overall transfer rate limit.
            // Do we want to somehow document that fact for the chargepad's limit
            int energyToGive = energyContainer.getEnergyPerTick();
            for (int slot = 0, slots = itemHandler.size(); slot < slots; slot++) {
                //Note: We don't use strict here as we want to allow the item to move to an empty slot if it has to in order to be charged
                int inserted = EnergyUtils.charge(energyContainer, ItemAccess.forHandlerIndex(itemHandler, slot), energyToGive, transaction);
                if (inserted > 0) {
                    //Only allow charging one item per player each check of the chargepad
                    return true;
                }
            }
        }
        return false;
    }

    private boolean provideEnergy(@Nullable EnergyHandler energyHandler, TransactionContext transaction) {
        return EnergyUtils.charge(energyContainer, energyHandler, energyContainer.getEnergyPerTick(), transaction) > 0;
    }

    @Override
    public void setActive(boolean active) {
        boolean wasActive = getActive();
        super.setActive(active);
        if (wasActive != active) {
            //If the state changed play pressure plate sound
            SoundEvent sound;
            float pitch;
            if (active) {
                sound = SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON;
                pitch = 0.8F;
            } else {
                sound = SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF;
                pitch = 0.7F;
            }
            level.playSound(null, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.1, getBlockPos().getZ() + 0.5, sound, SoundSource.BLOCKS, 0.3F, pitch);
        }
    }
}