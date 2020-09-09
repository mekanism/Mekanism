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
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.entity.EntityRobit;
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
        //Use 0.4 for y so as to catch entities that are partially standing on the back pane
        List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(),
              pos.getX() + 1, pos.getY() + 0.4, pos.getZ() + 1), CHARGE_PREDICATE);
        for (LivingEntity entity : entities) {
            active = !energyContainer.isEmpty();
            if (!active) {
                //If we run out of energy, stop checking the remaining entities
                break;
            } else if (entity instanceof EntityRobit) {
                provideEnergy((EntityRobit) entity);
            } else if (entity instanceof PlayerEntity) {
                Optional<IItemHandler> itemHandlerCap = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve();
                //Ensure that we have an item handler capability, because if for example the player is dead we will not
                if (itemHandlerCap.isPresent()) {
                    IItemHandler itemHandler = itemHandlerCap.get();
                    int slots = itemHandler.getSlots();
                    for (int slot = 0; slot < slots; slot++) {
                        ItemStack stack = itemHandler.getStackInSlot(slot);
                        if (!stack.isEmpty() && provideEnergy(EnergyCompatUtils.getStrictEnergyHandler(stack))) {
                            //Only allow charging one item per player each check
                            break;
                        }
                    }
                }
            }
        }
        if (active != getActive()) {
            setActive(active);
        }
    }

    private boolean provideEnergy(@Nullable IStrictEnergyHandler energyHandler) {
        if (energyHandler == null) {
            return false;
        }
        FloatingLong energyToGive = energyContainer.getEnergy();
        FloatingLong simulatedRemainder = energyHandler.insertEnergy(energyToGive, Action.SIMULATE);
        if (simulatedRemainder.smallerThan(energyToGive)) {
            //We are able to fit at least some of the energy from our container into the item
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
            world.addParticle(RedstoneParticleData.REDSTONE_DUST, getPos().getX() + world.rand.nextDouble(), getPos().getY() + 0.15,
                  getPos().getZ() + world.rand.nextDouble(), 0, 0, 0);
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
                sound = SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON;
                pitch = 0.8F;
            } else {
                sound = SoundEvents.BLOCK_STONE_PRESSURE_PLATE_CLICK_OFF;
                pitch = 0.7F;
            }
            world.playSound(null, getPos().getX() + 0.5, getPos().getY() + 0.1, getPos().getZ() + 0.5, sound, SoundCategory.BLOCKS, 0.3F, pitch);
        }
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }
}