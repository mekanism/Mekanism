package mekanism.common.item.gear;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.item.RateLimitMultiTankGasHandler;
import mekanism.common.capabilities.chemical.item.RateLimitMultiTankGasHandler.GasTankSpec;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.IItemHUDProvider;
import mekanism.common.item.IModeItem;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemMekaSuitArmor extends ArmorItem implements IModuleContainerItem, IModeItem, IItemHUDProvider/*, ISpecialGear*/ {

    // TODO separate these into individual modules maybe (specifically fire-related - on_fire, in_fire, lava)
    private static final Set<DamageSource> ALWAYS_SUPPORTED_SOURCES = new HashSet<>(Arrays.asList(
        DamageSource.ANVIL, DamageSource.CACTUS, DamageSource.CRAMMING, DamageSource.DRAGON_BREATH, DamageSource.DRYOUT,
        DamageSource.FALL, DamageSource.FALLING_BLOCK, DamageSource.FIREWORKS, DamageSource.FLY_INTO_WALL, DamageSource.GENERIC,
        DamageSource.HOT_FLOOR, DamageSource.IN_FIRE, DamageSource.IN_WALL, DamageSource.LAVA, DamageSource.LIGHTNING_BOLT,
        DamageSource.ON_FIRE, DamageSource.SWEET_BERRY_BUSH, DamageSource.WITHER));

    private static final MekaSuitMaterial MEKASUIT_MATERIAL = new MekaSuitMaterial();
    private static final int GAS_TRANSFER_RATE = 256;

    private Set<GasTankSpec> gasTankSpecs = new HashSet<>();
    private float absorption;

    public ItemMekaSuitArmor(EquipmentSlotType slot, Properties properties) {
        super(MEKASUIT_MATERIAL, slot, properties.setNoRepair().maxStackSize(1));
        Modules.setSupported(this, Modules.ENERGY_UNIT, Modules.RADIATION_SHIELDING_UNIT);

        if (slot == EquipmentSlotType.HEAD) {
            Modules.setSupported(this, Modules.ELECTROLYTIC_BREATHING_UNIT, Modules.INHALATION_PURIFICATION_UNIT, Modules.VISION_ENHANCEMENT_UNIT,
                Modules.SOLAR_RECHARGING_UNIT, Modules.NUTRITIONAL_INJECTION_UNIT);
            gasTankSpecs.add(GasTankSpec.createFillOnly(() -> GAS_TRANSFER_RATE, () -> 16_000, gas -> gas == MekanismGases.NUTRITIONAL_PASTE.get()));
            absorption = 0.15F;
        } else if (slot == EquipmentSlotType.CHEST) {
            Modules.setSupported(this, Modules.JETPACK_UNIT, Modules.GRAVITATIONAL_MODULATING_UNIT, Modules.CHARGE_DISTRIBUTION_UNIT, Modules.DOSIMETER_UNIT);
            gasTankSpecs.add(GasTankSpec.createFillOnly(() -> GAS_TRANSFER_RATE, () -> 24_000, gas -> gas == MekanismGases.HYDROGEN.get()));
            absorption = 0.4F;
        } else if (slot == EquipmentSlotType.LEGS) {
            Modules.setSupported(this, Modules.LOCOMOTIVE_BOOSTING_UNIT);
            absorption = 0.3F;
        } else if (slot == EquipmentSlotType.FEET) {
            Modules.setSupported(this, Modules.HYDRAULIC_PROPULSION_UNIT, Modules.MAGNETIC_ATTRACTION_UNIT);
            absorption = 0.15F;
        }
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        // safety check
        return 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.detailsKey)) {
            for (Module module : Modules.loadAll(stack)) {
                ITextComponent component = module.getData().getLangEntry().translateColored(EnumColor.GRAY);
                if (module.getInstalledCount() > 1) {
                    ITextComponent t = MekanismLang.GENERIC_FRACTION.translateColored(EnumColor.GRAY, module.getInstalledCount(), module.getData().getMaxStackSize());
                    component.appendSibling(MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.GRAY, "", t));
                }
                tooltip.add(component);
            }
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, true);
            if (!gasTankSpecs.isEmpty()) {
                StorageUtils.addStoredGas(stack, tooltip, true);
            }
            tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getLocalizedName()));
        }
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return super.getDisplayName(stack).applyTextStyle(EnumColor.PURPLE.textFormatting);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return StorageUtils.getDurabilityForDisplay(stack);
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (isInGroup(group)) {
            ItemStack stack = new ItemStack(this);
            items.add(StorageUtils.getFilledEnergyVariant(stack, getMaxEnergy(stack)));
        }
    }

    @Override
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        super.onArmorTick(stack, world, player);
        for (Module module : Modules.loadAll(stack)) {
            module.tick(player);
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        if (stack.getTag() == null) {
            stack.setTag(new CompoundNBT());
        }
        stack.getTag().putInt("HideFlags", 2);
        //Note: We interact with this capability using "manual" as the automation type, to ensure we can properly bypass the energy limit for extracting
        // Internal is used by the "null" side, which is what will get used for most items
        ItemCapabilityWrapper wrapper = new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(() -> getChargeRate(stack), () -> getMaxEnergy(stack), BasicEnergyContainer.notExternal, BasicEnergyContainer.alwaysTrue),
            RadiationShieldingHandler.create(item -> isModuleEnabled(item, Modules.RADIATION_SHIELDING_UNIT) ? ItemHazmatSuitArmor.getShieldingByArmor(slot) : 0));
        if (!gasTankSpecs.isEmpty()) {
            wrapper.add(RateLimitMultiTankGasHandler.create(gasTankSpecs));
        }
        return wrapper;
    }

    @Nonnull
    public GasStack useGas(ItemStack stack, Gas type, long amount) {
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            return gasHandlerItem.extractGas(new GasStack(type, amount), Action.EXECUTE);
        }
        return GasStack.EMPTY;
    }

    public GasStack getContainedGas(ItemStack stack, Gas type) {
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            for (int i = 0; i < gasHandlerItem.getGasTankCount(); i++) {
                if (gasHandlerItem.getGasInTank(i).getType() == type) {
                    return gasHandlerItem.getGasInTank(i);
                }
            }
        }
        return GasStack.EMPTY;
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list, ItemStack stack, EquipmentSlotType slotType) {
        if (slotType == getEquipmentSlot()) {
            list.add(MekanismLang.GENERIC_PRE_STORED.translateColored(EnumColor.GRAY, EnumColor.GRAY, MekanismLang.get(slotType),
                EnumColor.GRAY, APILang.TRANSMISSION_TYPE_ENERGY, StorageUtils.getEnergyPercent(stack)));
            for (Module module : Modules.loadAll(stack)) {
                if (module.renderHUD()) {
                    module.addHUDStrings(list);
                }
            }
        }
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        for (Module module : Modules.loadAll(stack)) {
            if (module.handlesModeChange()) {
                module.changeMode(player, stack, shift, displayChangeMessage);
                return;
            }
        }
    }

    @Override
    public boolean supportsSlotType(@Nonnull EquipmentSlotType slotType) {
        return slotType == getEquipmentSlot();
    }

    @Nullable
    @Override
    public ITextComponent getScrollTextComponent(@Nonnull ItemStack stack) {
        return null;
    }

    private FloatingLong getMaxEnergy(ItemStack stack) {
        ModuleEnergyUnit module = Modules.load(stack, Modules.ENERGY_UNIT);
        return module != null ? module.getEnergyCapacity() : MekanismConfig.gear.mekaToolBaseEnergyCapacity.get();
    }

    private FloatingLong getChargeRate(ItemStack stack) {
        ModuleEnergyUnit module = Modules.load(stack, Modules.ENERGY_UNIT);
        return module != null ? module.getChargeRate() : MekanismConfig.gear.mekaToolBaseChargeRate.get();
    }

    public float getDamageAbsorbed(ItemStack stack, DamageSource source, float amount) {
        // don't handle magic as it's handled by inhalation purification
        // don't handle starving as player should have nutritional injection
        // don't handle out of world (ever)
        if (!ALWAYS_SUPPORTED_SOURCES.contains(source) && source.isUnblockable()) {
            return 0;
        }
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null && amount > 0) {
            float toAbsorb = amount * absorption;
            FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageDamage.get().multiply(toAbsorb);
            return absorption * energyContainer.extract(usage, Action.EXECUTE, AutomationType.MANUAL).divide(usage).floatValue();
        }
        return 0;
    }

    // This is unused for the most part; toughness / damage reduction is handled manually
    protected static class MekaSuitMaterial extends BaseSpecialArmorMaterial {
        @Override
        public int getDamageReductionAmount(EquipmentSlotType slotType) { return 0; }
        @Override
        public float getToughness() { return 0; }

        @Override
        public String getName() {
            return "mekasuit";
        }
    }
}
