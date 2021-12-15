package mekanism.common.tile;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityChargepad extends TileEntityMekanism {

    private static final Predicate<LivingEntity> CHARGE_PREDICATE = entity -> !entity.isSpectator() && (entity instanceof PlayerEntity || entity instanceof EntityRobit);

    private MachineEnergyContainer<TileEntityChargepad> energyContainer;

    public TileEntityChargepad() {
        super(MekanismBlocks.CHARGEPAD);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this), RelativeSide.BACK, RelativeSide.BOTTOM);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        boolean active = false;
        //Use 0.4 for y to catch entities that are partially standing on the back pane
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
              worldPosition.getX() + 1, worldPosition.getY() + 0.4, worldPosition.getZ() + 1), CHARGE_PREDICATE);
        for (LivingEntity entity : entities) {
            active = !energyContainer.isEmpty();
            if (!active) {
                //If we run out of energy, stop checking the remaining entities
                break;
            } else if (entity instanceof EntityRobit) {
                provideEnergy((EntityRobit) entity);
            } else if (entity instanceof PlayerEntity) {
                Optional<IItemHandler> itemHandlerCap = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve();
                if (!chargeHandler(itemHandlerCap) && Mekanism.hooks.CuriosLoaded) {
                    //If we didn't charge anything in the inventory and curios is loaded try charging things in the curios slots
                    chargeHandler(MekanismHooks.getCuriosInventory(entity));
                }
            }
        }
        if (active != getActive()) {
            setActive(active);
        }
    }

    private boolean chargeHandler(Optional<? extends IItemHandler> itemHandlerCap) {
        //Ensure that we have an item handler capability, because if for example the player is dead we will not
        if (itemHandlerCap.isPresent()) {
            IItemHandler itemHandler = itemHandlerCap.get();
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
        FloatingLong energyToGive = energyContainer.getEnergyPerTick();
        FloatingLong simulatedRemainder = energyHandler.insertEnergy(energyToGive, Action.SIMULATE);
        if (simulatedRemainder.smallerThan(energyToGive)) {
            //We are able to fit at least some energy from our container into the item
            FloatingLong extractedEnergy = energyContainer.extract(energyToGive.subtract(simulatedRemainder), Action.EXECUTE, AutomationType.INTERNAL);
            if (!extractedEnergy.isZero()) {
                //If we were able to actually extract it from our energy container, then insert it into the item
                MekanismUtils.logExpectedZero(energyHandler.insertEnergy(extractedEnergy, Action.EXECUTE));
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (getActive()) {
            level.addParticle(RedstoneParticleData.REDSTONE, getBlockPos().getX() + level.random.nextDouble(), getBlockPos().getY() + 0.15,
                  getBlockPos().getZ() + level.random.nextDouble(), 0, 0, 0);
        }
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
            level.playSound(null, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.1, getBlockPos().getZ() + 0.5, sound, SoundCategory.BLOCKS, 0.3F, pitch);
        }
    }
}