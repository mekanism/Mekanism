package mekanism.common.tile;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityChargepad extends TileEntityMekanism {

    private static final Predicate<LivingEntity> CHARGE_PREDICATE = entity -> !entity.isSpectator() && (entity instanceof Player || entity instanceof EntityRobit);

    private MachineEnergyContainer<TileEntityChargepad> energyContainer;

    public TileEntityChargepad(BlockPos pos, BlockState state) {
        super(MekanismBlocks.CHARGEPAD, pos, state);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(facingSupplier);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener), RelativeSide.BACK, RelativeSide.BOTTOM);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        boolean active = false;
        if (!energyContainer.isEmpty()) {
            //Use 0.4 for y to catch entities that are partially standing on the back pane
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                  worldPosition.getX() + 1, worldPosition.getY() + 0.4, worldPosition.getZ() + 1), CHARGE_PREDICATE);
            for (LivingEntity entity : entities) {
                if (energyContainer.isEmpty()) {
                    //If we run out of energy, stop checking the remaining entities
                    break;
                } else if (entity instanceof Player) {
                    IItemHandler itemHandler = Capabilities.ITEM.getCapability(entity);
                    if (chargeHandler(itemHandler)) {
                        active = true;
                    } else if (Mekanism.hooks.curios.isLoaded()) {
                        //If we didn't charge anything in the inventory and curios is loaded try charging things in the curios slots
                        if (chargeHandler(CuriosIntegration.getCuriosInventory(entity))) {
                            active = true;
                        }
                    }
                } else if (provideEnergy(EnergyCompatUtils.getStrictEnergyHandler(entity))) {
                    //Note: Robits are handled by this path
                    active = true;
                }
            }
        }
        if (active != getActive()) {
            setActive(active);
        }
        return sendUpdatePacket;
    }

    private boolean chargeHandler(@Nullable IItemHandler itemHandler) {
        //Ensure that we have an item handler capability, because if for example the player is dead we will not
        if (itemHandler != null) {
            int slots = itemHandler.getSlots();
            for (int slot = 0; slot < slots; slot++) {
                ItemStack stack = itemHandler.getStackInSlot(slot);
                if (!stack.isEmpty() && provideEnergy(EnergyCompatUtils.getStrictEnergyHandler(stack))) {
                    //Only allow charging one item per player each check
                    return true;
                }
            }
        }
        return false;
    }

    private boolean provideEnergy(@Nullable IStrictEnergyHandler energyHandler) {
        if (energyHandler == null) {
            return false;
        }
        long energyToGive = energyContainer.getEnergyPerTick();
        long simulatedRemainder = energyHandler.insertEnergy(energyToGive, Action.SIMULATE);
        if (simulatedRemainder < energyToGive) {
            //We are able to fit at least some energy from our container into the item
            long extractedEnergy = energyContainer.extract(energyToGive - simulatedRemainder, Action.EXECUTE, AutomationType.INTERNAL);
            if (extractedEnergy > 0L) {
                //If we were able to actually extract it from our energy container, then insert it into the item
                MekanismUtils.logExpectedZero(energyHandler.insertEnergy(extractedEnergy, Action.EXECUTE));
                return true;
            }
        }
        return false;
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