package mekanism.common.content.gear.mekasuit;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.base.target.EnergySaveTarget;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.BooleanData;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleLocomotiveBoostingUnit.SprintBoost;
import mekanism.common.integration.EnergyCompatUtils;
import mekanism.common.item.gear.ItemCanteen;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
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
            useEnergy(MekanismConfig.general.mekaSuitEnergyUsageVisionEnhancement.get());
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
            super.init();
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
            Set<EnergySaveTarget> saveTargets = new ObjectOpenHashSet<>();
            FloatingLong total = FloatingLong.ZERO;
            for (ItemStack stack : player.inventory.armorInventory) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer != null) {
                    EnergySaveTarget saveTarget = new EnergySaveTarget();
                    saveTarget.addHandler(Direction.NORTH, energyContainer);
                    saveTargets.add(saveTarget);
                    total = total.plusEqual(energyContainer.getEnergy());
                }
            }
            EmitUtils.sendToAcceptors(saveTargets, saveTargets.size(), total.copy());
            for (EnergySaveTarget saveTarget : saveTargets) {
                saveTarget.save(Direction.NORTH);
            }
        }

        private void chargeInventory(PlayerEntity player) {
            FloatingLong toCharge = MekanismConfig.general.mekaSuitInventoryChargeRate.get();
            // first try to charge mainhand/offhand item
            toCharge = charge(player.getHeldItemMainhand(), toCharge);
            toCharge = charge(player.getHeldItemOffhand(), toCharge);

            for (ItemStack stack : player.inventory.mainInventory) {
                if (stack == player.getHeldItemMainhand() || stack == player.getHeldItemOffhand()) {
                    continue;
                }
                if (toCharge.isZero()) {
                    break;
                }
                toCharge = charge(stack, toCharge);
            }
        }

        /** return rejects */
        private FloatingLong charge(ItemStack stack, FloatingLong amount) {
            IStrictEnergyHandler handler = EnergyCompatUtils.getStrictEnergyHandler(stack);
            if (handler != null) {
                return handler.insertEnergy(useEnergy(amount), Action.EXECUTE);
            }
            return amount;
        }
    }

    public static class ModuleLocomotiveBoostingUnit extends ModuleMekaSuit {
        private ModuleConfigItem<SprintBoost> sprintBoost;

        @Override
        public void init() {
            super.init();
            addConfigItem(sprintBoost = new ModuleConfigItem<>(this, "sprint_boost", MekanismLang.MODULE_SPRINT_BOOST, new EnumData<>(SprintBoost.class).withScale(0.65F), SprintBoost.LOW));
        }

        @Override
        public void tickServer(PlayerEntity player) {
            super.tickServer(player);

            if (canFunction(player)) {
                float boost = getBoost();
                if (!player.onGround) boost /= 5F; // throttle if we're in the air
                if (player.isInWater()) boost /= 2F; // throttle if we're in the water
                player.moveRelative(boost, new Vec3d(0, 0, 1));
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
            addConfigItem(jumpBoost = new ModuleConfigItem<>(this, "jump_boost", MekanismLang.MODULE_JUMP_BOOST, new EnumData<>(JumpBoost.class).withScale(0.65F), JumpBoost.LOW));
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

    public static class ModuleSolarRechargingUnit extends ModuleMekaSuit {
        @Override
        public void tickServer(PlayerEntity player) {
            super.tickServer(player);
            IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(getContainer(), 0);
            if (energyContainer != null && !energyContainer.getNeeded().isZero() && player.world.isDaytime() &&
                player.world.canSeeSky(new BlockPos(player)) && !player.world.isRaining() && !player.world.getDimension().isNether()) {
                FloatingLong rate = MekanismConfig.general.mekaSuitSolarRechargingRate.get().multiply(getInstalledCount());
                energyContainer.insert(rate, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
    }

    public static class ModuleNutritionalInjectionUnit extends ModuleMekaSuit {
        @Override
        public void tickServer(PlayerEntity player) {
            super.tickServer(player);
            FloatingLong usage = MekanismConfig.general.mekaSuitEnergyUsageNutritionalInjection.get();
            if (!player.isCreative() && player.canEat(false) && getContainerEnergy().greaterOrEqual(usage)) {
                ItemMekaSuitArmor item = (ItemMekaSuitArmor) getContainer().getItem();
                int toFeed = Math.min(1, item.getContainedGas(getContainer(), MekanismGases.NUTRITIONAL_PASTE.get()).getAmount() / ItemCanteen.MB_PER_FOOD);
                if (toFeed > 0) {
                    useEnergy(usage.multiply(toFeed));
                    item.useGas(getContainer(), MekanismGases.NUTRITIONAL_PASTE.get(), toFeed * ItemCanteen.MB_PER_FOOD);
                    player.getFoodStats().addStats(1, ItemCanteen.SATURATION);
                }
            }
        }
        @Override
        public void addHUDStrings(List<ITextComponent> list) {
            if (!isEnabled()) return;
            GasStack stored = ((ItemMekaSuitArmor) getContainer().getItem()).getContainedGas(getContainer(), MekanismGases.NUTRITIONAL_PASTE.get());
            list.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.DARK_GRAY, MekanismGases.NUTRITIONAL_PASTE, EnumColor.PINK, stored.getAmount()));
        }
    }
}
