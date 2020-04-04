package mekanism.common.content.gear.mekasuit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.content.gear.Modules;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public abstract class ModuleMekaSuit extends Module {

    public static class ModuleElectrolyticBreathingUnit extends ModuleMekaSuit {
        @Override
        public void tickServer(PlayerEntity player) {
            FloatingLong usage = MekanismConfig.general.FROM_H2.get().multiply(2);
            int maxRate = Math.min(getMaxRate(), getContainerEnergy().divide(usage).intValue());
            int hydrogenUsed = 0;
            GasStack hydrogenStack = new GasStack(MekanismGases.HYDROGEN.get(), maxRate * 2);
            ItemStack chestStack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            Optional<IGasHandler> capability = MekanismUtils.toOptional(chestStack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (Modules.load(chestStack, Modules.JETPACK_UNIT) != null && capability.isPresent()) {
                hydrogenUsed = maxRate * 2 - capability.get().insertGas(hydrogenStack, Action.EXECUTE).getAmount();
            }
            int oxygenUsed = Math.min(maxRate, player.getMaxAir() - player.getAir());
            int used = Math.max((int) Math.ceil(hydrogenUsed / 2D), oxygenUsed);
            useEnergy(usage.multiply(used));
            player.setAir(player.getAir() + oxygenUsed);
        }

        private int getMaxRate() {
            return (int) Math.pow(2, getInstalledCount());
        }
    }

    public static class ModuleInhalationPurificationUnit extends ModuleMekaSuit {
        @Override
        public void tickServer(PlayerEntity player) {
            for (EffectInstance effect : player.getActivePotionEffects()) {
                if (getContainerEnergy().smallerThan(MekanismConfig.general.mekaSuitEnergyUsagePotionTick.get())) {
                    break;
                }
                useEnergy(MekanismConfig.general.mekaSuitEnergyUsagePotionTick.get());
                for (int i = 0; i < 9; i++) {
                    effect.tick(player, () -> MekanismUtils.onChangedPotionEffect(player, effect, true));
                }
            }
        }
    }

    public static class ModuleVisionEnhancementUnit extends ModuleMekaSuit {}

    public static class ModuleRadiationShieldingUnit extends ModuleMekaSuit {}

    public static class ModuleGravitationalModulatingUnit extends ModuleMekaSuit {}

    public static class ModuleChargeDistributionUnit extends ModuleMekaSuit {
        @Override
        public void tickServer(PlayerEntity player) {
            super.tickServer(player);
            FloatingLong total = FloatingLong.ZERO;
            List<IEnergyContainer> tracking = new ArrayList<>();
            for (ItemStack stack : player.inventory.armorInventory) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer != null) {
                    total.add(energyContainer.getEnergy());
                    tracking.add(energyContainer);
                }
            }
            if (!tracking.isEmpty()) {
                FloatingLong divide = total.divide(tracking.size());
                for (IEnergyContainer energyContainer : tracking) {
                    energyContainer.setEnergy(divide);
                }
            }
        }
    }

    public static class ModuleLocomotiveBoostingUnit extends ModuleMekaSuit {
        private ModuleConfigItem<SprintBoost> sprintBoost;

        @Override
        public void init() {
            super.init();
            addConfigItem(sprintBoost = new ModuleConfigItem<SprintBoost>(this, "sprint_boost", MekanismLang.MODULE_SPRINT_BOOST, new EnumData<>(SprintBoost.class), SprintBoost.LOW));
        }

        @Override
        public void tickServer(PlayerEntity player) {
            super.tickServer(player);

            if (canFunction(player)) {
                player.moveRelative(!player.onGround ? getBoost() / 5F : getBoost(), new Vec3d(0, 0, 1));
                useEnergy(MekanismConfig.general.mekaSuitEnergyUsageSprintBoost.get().multiply(getBoost() / 0.1F));
            }
        }

        @Override
        public void tickClient(PlayerEntity player) {
            super.tickClient(player);

            if (canFunction(player)) {
                player.moveRelative(!player.onGround ? getBoost() / 5F : getBoost(), new Vec3d(0, 0, 1));
                // leave energy usage up to server
            }
        }

        public boolean canFunction(PlayerEntity player) {
            FloatingLong usage = MekanismConfig.general.mekaSuitEnergyUsageSprintBoost.get().multiply(getBoost() / 0.1F);
            return player.isSprinting() && getContainerEnergy().greaterOrEqual(usage);
        }

        public float getBoost() {
            return sprintBoost.get().getBoost();
        }

        public static enum SprintBoost implements IHasTextComponent {
            OFF(0),
            LOW(0.05F),
            MED(0.1F),
            HIGH(0.25F),
            ULTRA(0.5F);
            private float boost;
            private ITextComponent label;
            private SprintBoost(float boost) {
                this.boost = boost;
                this.label = new StringTextComponent(Float.toString(boost));
            }
            @Override
            public ITextComponent getTextComponent() {
                return label;
            }
            public float getBoost() {
                return boost;
            }
        }
    }

    public static class ModuleHydraulicAbsorptionUnit extends ModuleMekaSuit {}

    public static class ModuleHydraulicPropulsionUnit extends ModuleMekaSuit {
        private ModuleConfigItem<JumpBoost> jumpBoost;

        @Override
        public void init() {
            super.init();
            addConfigItem(jumpBoost = new ModuleConfigItem<JumpBoost>(this, "jump_boost", MekanismLang.MODULE_JUMP_BOOST, new EnumData<>(JumpBoost.class), JumpBoost.LOW));
        }

        public float getBoost() {
            return jumpBoost.get().getBoost();
        }

        public static enum JumpBoost implements IHasTextComponent {
            OFF(0),
            LOW(0.5F),
            MED(1F),
            HIGH(3),
            ULTRA(5);
            private float boost;
            private ITextComponent label;
            private JumpBoost(float boost) {
                this.boost = boost;
                this.label = new StringTextComponent(Float.toString(boost));
            }
            @Override
            public ITextComponent getTextComponent() {
                return label;
            }
            public float getBoost() {
                return boost;
            }
        }
    }
}
