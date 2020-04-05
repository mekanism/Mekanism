package mekanism.common.content.gear.mekasuit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.BooleanData;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleLocomotiveBoostingUnit.SprintBoost;
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

    public static class ModuleVisionEnhancementUnit extends ModuleMekaSuit {
        @Override
        public void tickServer(PlayerEntity player) {
            super.tickServer(player);
        }

        @Override
        public void addHUDStrings(List<ITextComponent> list) {
            ILangEntry lang = isEnabled() ? MekanismLang.MODULE_ENABLED_LOWER : MekanismLang.MODULE_DISABLED_LOWER;
            list.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.DARK_GRAY, EnumColor.DARK_GRAY, MekanismLang.MODULE_VISION_ENHANCEMENT,
                isEnabled() ? EnumColor.BRIGHT_GREEN : EnumColor.DARK_RED, lang.translate()));
        }

        @Override
        public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
            toggleEnabled(player, MekanismLang.MODULE_VISION_ENHANCEMENT.translate());
        }
    }

    public static class ModuleRadiationShieldingUnit extends ModuleMekaSuit {}

    public static class ModuleGravitationalModulatingUnit extends ModuleMekaSuit {
        // we share with locomotive boosting unit
        private ModuleConfigItem<SprintBoost> speedBoost;

        @Override
        public void init() {
            super.init();
            addConfigItem(speedBoost = new ModuleConfigItem<>(this, "speed_boost", MekanismLang.MODULE_SPEED_BOOST, new EnumData<>(SprintBoost.class), SprintBoost.LOW));
        }


        @Override
        public void addHUDStrings(List<ITextComponent> list) {
            ILangEntry lang = isEnabled() ? MekanismLang.MODULE_ENABLED_LOWER : MekanismLang.MODULE_DISABLED_LOWER;
            list.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.DARK_GRAY, EnumColor.DARK_GRAY, MekanismLang.MODULE_GRAVITATIONAL_MODULATION,
                isEnabled() ? EnumColor.BRIGHT_GREEN : EnumColor.DARK_RED, lang.translate()));
        }

        @Override
        public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
            toggleEnabled(player, MekanismLang.MODULE_GRAVITATIONAL_MODULATION.translate());
        }

        public float getBoost() {
            return speedBoost.get().getBoost();
        }
    }

    public static class ModuleChargeDistributionUnit extends ModuleMekaSuit {
        private ModuleConfigItem<Boolean> chargeSuit;
        private ModuleConfigItem<Boolean> chargeInventory;

        @Override
        public void init() {
            chargeSuit = addConfigItem(new ModuleConfigItem<>(this, "charge_suit", MekanismLang.MODULE_CHARGE_SUIT, new BooleanData(), true));
            chargeInventory = addConfigItem(new ModuleConfigItem<>(this, "charge_inventory", MekanismLang.MODULE_CHARGE_INVENTORY, new BooleanData(), false));
        }

        @Override
        public void tickServer(PlayerEntity player) {
            super.tickServer(player);
            // charge inventory first
            if (chargeInventory.get()) {
                chargeInventory(player);
            }
            // distribute suit charge next
            if (chargeSuit.get()) {
                chargeSuit(player);
            }
        }

        private void chargeSuit(PlayerEntity player) {
            FloatingLong total = FloatingLong.ZERO;
            List<IEnergyContainer> tracking = new ArrayList<>();
            for (ItemStack stack : player.inventory.armorInventory) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer != null) {
                    total = total.plusEqual(energyContainer.getEnergy());
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

        private void chargeInventory(PlayerEntity player) {
            FloatingLong toCharge = MekanismConfig.general.mekaSuitInventoryChargeRate.get();
            // first try to charge mainhand/offhand item
            toCharge = charge(player.getHeldItemMainhand(), toCharge);
            toCharge = charge(player.getHeldItemOffhand(), toCharge);

            for (ItemStack stack : player.inventory.mainInventory) {
                if (toCharge.isZero()) {
                    break;
                }
                toCharge = charge(stack, toCharge);
            }
        }

        /** return rejects */
        private FloatingLong charge(ItemStack stack, FloatingLong amount) {
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
            if (energyContainer != null) {
                return energyContainer.insert(amount, Action.EXECUTE, AutomationType.MANUAL);
            }
            return amount;
        }
    }

    public static class ModuleLocomotiveBoostingUnit extends ModuleMekaSuit {
        private ModuleConfigItem<SprintBoost> sprintBoost;

        @Override
        public void init() {
            super.init();
            addConfigItem(sprintBoost = new ModuleConfigItem<>(this, "sprint_boost", MekanismLang.MODULE_SPRINT_BOOST, new EnumData<>(SprintBoost.class), SprintBoost.LOW));
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
            addConfigItem(jumpBoost = new ModuleConfigItem<>(this, "jump_boost", MekanismLang.MODULE_JUMP_BOOST, new EnumData<>(JumpBoost.class), JumpBoost.LOW));
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

    public static class ModuleSolarRechargingUnit extends ModuleMekaSuit {}
}
