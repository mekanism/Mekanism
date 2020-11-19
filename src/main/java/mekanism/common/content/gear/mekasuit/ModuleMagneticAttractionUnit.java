package mekanism.common.content.gear.mekasuit;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.network.PacketLightningRender;
import mekanism.common.network.PacketLightningRender.LightningPreset;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ModuleMagneticAttractionUnit extends ModuleMekaSuit {

    private ModuleConfigItem<Range> range;

    @Override
    public void init() {
        super.init();
        addConfigItem(range = new ModuleConfigItem<>(this, "range", MekanismLang.MODULE_RANGE, new EnumData<>(Range.class, getInstalledCount() + 1), Range.LOW));
    }

    @Override
    public void tickServer(PlayerEntity player) {
        super.tickServer(player);
        if (range.get() != Range.OFF) {
            float size = 4 + range.get().getRange();
            FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageItemAttraction.get().multiply(range.get().getRange());
            boolean free = usage.isZero() || player.isCreative();
            IEnergyContainer energyContainer = free ? null : getEnergyContainer();
            if (free || (energyContainer != null && energyContainer.getEnergy().greaterOrEqual(usage))) {
                //If the energy cost is free or we have enough energy for at least one pull grab all the items that can be picked up.
                //Note: We check distance afterwards so that we aren't having to calculate a bunch of distances when we may run out
                // of energy, and calculating distance is a bit more expensive than just checking if it can be picked up
                List<ItemEntity> items = player.world.getEntitiesWithinAABB(ItemEntity.class, player.getBoundingBox().grow(size, size, size), item -> !item.cannotPickup());
                for (ItemEntity item : items) {
                    if (item.getDistance(player) > 0.001) {
                        if (free) {
                            pullItem(player, item);
                        } else if (useEnergy(player, energyContainer, usage, true).isZero()) {
                            //If we can't actually extract energy, exit
                            break;
                        } else {
                            pullItem(player, item);
                            if (energyContainer.getEnergy().smallerThan(usage)) {
                                //If after using energy, our energy is now smaller than how much we need to use, exit
                                break;
                            }
                        }

                    }
                }
            }
        }
    }

    private void pullItem(PlayerEntity player, ItemEntity item) {
        Vector3d diff = player.getPositionVec().subtract(item.getPositionVec());
        Vector3d motionNeeded = new Vector3d(Math.min(diff.x, 1), Math.min(diff.y, 1), Math.min(diff.z, 1));
        Vector3d motionDiff = motionNeeded.subtract(player.getMotion());
        item.setMotion(motionDiff.scale(0.2));
        Mekanism.packetHandler.sendToAllTrackingAndSelf(new PacketLightningRender(LightningPreset.MAGNETIC_ATTRACTION, Objects.hash(player, item),
              player.getPositionVec().add(0, 0.2, 0), item.getPositionVec(), (int) (diff.length() * 4)), player);
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        toggleEnabled(player, MekanismLang.MODULE_MAGNETIC_ATTRACTION_UNIT.translate());
    }

    public enum Range implements IHasTextComponent {
        OFF(0),
        LOW(1F),
        MED(3F),
        HIGH(5),
        ULTRA(10);

        private final float range;
        private final ITextComponent label;

        Range(float boost) {
            this.range = boost;
            this.label = new StringTextComponent(Float.toString(boost));
        }

        @Override
        public ITextComponent getTextComponent() {
            return label;
        }

        public float getRange() {
            return range;
        }
    }
}