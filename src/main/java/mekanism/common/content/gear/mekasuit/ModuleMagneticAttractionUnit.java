package mekanism.common.content.gear.mekasuit;

import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.network.to_client.PacketLightningRender.LightningPreset;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@ParametersAreNonnullByDefault
public class ModuleMagneticAttractionUnit implements ICustomModule<ModuleMagneticAttractionUnit> {

    private IModuleConfigItem<Range> range;

    @Override
    public void init(IModule<ModuleMagneticAttractionUnit> module, ModuleConfigItemCreator configItemCreator) {
        range = configItemCreator.createConfigItem("range", MekanismLang.MODULE_RANGE,
              new ModuleEnumData<>(Range.class, module.getInstalledCount() + 1, Range.LOW));
    }

    @Override
    public void tickServer(IModule<ModuleMagneticAttractionUnit> module, Player player) {
        if (range.get() != Range.OFF) {
            float size = 4 + range.get().getRange();
            FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageItemAttraction.get().multiply(range.get().getRange());
            boolean free = usage.isZero() || player.isCreative();
            IEnergyContainer energyContainer = free ? null : module.getEnergyContainer();
            if (free || (energyContainer != null && energyContainer.getEnergy().greaterOrEqual(usage))) {
                //If the energy cost is free, or we have enough energy for at least one pull grab all the items that can be picked up.
                //Note: We check distance afterwards so that we aren't having to calculate a bunch of distances when we may run out
                // of energy, and calculating distance is a bit more expensive than just checking if it can be picked up
                List<ItemEntity> items = player.level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(size, size, size), item -> !item.hasPickUpDelay());
                for (ItemEntity item : items) {
                    if (item.distanceTo(player) > 0.001) {
                        if (free) {
                            pullItem(player, item);
                        } else if (module.useEnergy(player, energyContainer, usage, true).isZero()) {
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

    private void pullItem(Player player, ItemEntity item) {
        Vec3 diff = player.position().subtract(item.position());
        Vec3 motionNeeded = new Vec3(Math.min(diff.x, 1), Math.min(diff.y, 1), Math.min(diff.z, 1));
        Vec3 motionDiff = motionNeeded.subtract(player.getDeltaMovement());
        item.setDeltaMovement(motionDiff.scale(0.2));
        Mekanism.packetHandler().sendToAllTrackingAndSelf(new PacketLightningRender(LightningPreset.MAGNETIC_ATTRACTION, Objects.hash(player.getUUID(), item),
              player.position().add(0, 0.2, 0), item.position(), (int) (diff.length() * 4)), player);
    }

    @Override
    public boolean canChangeModeWhenDisabled(IModule<ModuleMagneticAttractionUnit> module) {
        return true;
    }

    @Override
    public void changeMode(IModule<ModuleMagneticAttractionUnit> module, Player player, ItemStack stack, int shift, boolean displayChangeMessage) {
        module.toggleEnabled(player, MekanismLang.MODULE_MAGNETIC_ATTRACTION.translate());
    }

    public enum Range implements IHasTextComponent {
        OFF(0),
        LOW(1F),
        MED(3F),
        HIGH(5),
        ULTRA(10);

        private final float range;
        private final Component label;

        Range(float boost) {
            this.range = boost;
            this.label = TextComponentUtil.getString(Float.toString(boost));
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public float getRange() {
            return range;
        }
    }
}