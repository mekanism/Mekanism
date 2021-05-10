package mekanism.common.item.gear;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.IModule;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.client.render.armor.CustomArmor;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.common.Mekanism;
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
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemMekaSuitArmor extends ItemSpecialArmor implements IModuleContainerItem, IModeItem {

    // TODO separate these into individual modules maybe (specifically fire-related - on_fire, in_fire, lava)
    private static final Set<DamageSource> ALWAYS_SUPPORTED_SOURCES = new HashSet<>(Arrays.asList(
          DamageSource.ANVIL, DamageSource.CACTUS, DamageSource.CRAMMING, DamageSource.DRAGON_BREATH, DamageSource.DRY_OUT,
          DamageSource.FALL, DamageSource.FALLING_BLOCK, DamageSource.FLY_INTO_WALL, DamageSource.GENERIC,
          DamageSource.HOT_FLOOR, DamageSource.IN_FIRE, DamageSource.IN_WALL, DamageSource.LAVA, DamageSource.LIGHTNING_BOLT,
          DamageSource.ON_FIRE, DamageSource.SWEET_BERRY_BUSH, DamageSource.WITHER));

    private static final MekaSuitMaterial MEKASUIT_MATERIAL = new MekaSuitMaterial();

    private final Set<GasTankSpec> gasTankSpecs = new HashSet<>();
    private float absorption;

    public ItemMekaSuitArmor(EquipmentSlotType slot, Properties properties) {
        super(MEKASUIT_MATERIAL, slot, properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1));
        if (slot == EquipmentSlotType.HEAD) {
            gasTankSpecs.add(GasTankSpec.createFillOnly(MekanismConfig.gear.mekaSuitNutritionalTransferRate, MekanismConfig.gear.mekaSuitNutritionalMaxStorage,
                  gas -> gas == MekanismGases.NUTRITIONAL_PASTE.get()));
            absorption = 0.15F;
        } else if (slot == EquipmentSlotType.CHEST) {
            gasTankSpecs.add(GasTankSpec.createFillOnly(MekanismConfig.gear.mekaSuitJetpackTransferRate, MekanismConfig.gear.mekaSuitJetpackMaxStorage,
                  gas -> gas == MekanismGases.HYDROGEN.get()));
            absorption = 0.4F;
        } else if (slot == EquipmentSlotType.LEGS) {
            absorption = 0.3F;
        } else if (slot == EquipmentSlotType.FEET) {
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
    public void appendHoverText(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, true);
            if (!gasTankSpecs.isEmpty()) {
                StorageUtils.addStoredGas(stack, tooltip, true, false);
            }
            tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @Override
    public boolean makesPiglinsNeutral(@Nonnull ItemStack stack, @Nonnull LivingEntity wearer) {
        return true;
    }

    @Override
    public boolean isEnderMask(@Nonnull ItemStack stack, @Nonnull PlayerEntity player, @Nonnull EndermanEntity enderman) {
        return getSlot() == EquipmentSlotType.HEAD;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        //TODO: Eventually look into making it so that we can have a "secondary durability" bar for rendering things like stored gas
        return StorageUtils.getEnergyDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    public void fillItemCategory(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
        if (allowdedIn(group)) {
            ItemStack stack = new ItemStack(this);
            items.add(StorageUtils.getFilledEnergyVariant(stack, getMaxEnergy(stack)));
        }
    }

    @Override
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        super.onArmorTick(stack, world, player);
        for (Module<?> module : getModules(stack)) {
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
        ItemCapabilityWrapper wrapper = new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(() -> getChargeRate(stack), () -> getMaxEnergy(stack),
              BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue),
              RadiationShieldingHandler.create(item -> isModuleEnabled(item, MekanismModules.RADIATION_SHIELDING_UNIT) ? ItemHazmatSuitArmor.getShieldingByArmor(slot) : 0));
        if (!gasTankSpecs.isEmpty()) {
            wrapper.add(RateLimitMultiTankGasHandler.create(gasTankSpecs));
        }
        return wrapper;
    }

    @Nonnull
    public GasStack useGas(ItemStack stack, Gas type, long amount) {
        Optional<IGasHandler> capability = stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).resolve();
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            return gasHandlerItem.extractChemical(new GasStack(type, amount), Action.EXECUTE);
        }
        return GasStack.EMPTY;
    }

    public GasStack getContainedGas(ItemStack stack, Gas type) {
        Optional<IGasHandler> capability = stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).resolve();
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            for (int i = 0; i < gasHandlerItem.getTanks(); i++) {
                GasStack gasInTank = gasHandlerItem.getChemicalInTank(i);
                if (gasInTank.getType() == type) {
                    return gasInTank;
                }
            }
        }
        return GasStack.EMPTY;
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        for (Module<?> module : getModules(stack)) {
            if (module.handlesModeChange()) {
                module.changeMode(player, stack, shift, displayChangeMessage);
                return;
            }
        }
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @Nonnull EquipmentSlotType slotType) {
        return slotType == getSlot() && getModules(stack).stream().anyMatch(Module::handlesModeChange);
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public CustomArmor getGearModel() {
        switch (getSlot()) {
            case HEAD:
                return MekaSuitArmor.HELMET;
            case CHEST:
                return MekaSuitArmor.BODYARMOR;
            case LEGS:
                return MekaSuitArmor.PANTS;
            default:
                return MekaSuitArmor.BOOTS;
        }
    }

    private FloatingLong getMaxEnergy(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        return module == null ? MekanismConfig.gear.mekaSuitBaseEnergyCapacity.get() : module.getCustomInstance().getEnergyCapacity(module);
    }

    private FloatingLong getChargeRate(ItemStack stack) {
        IModule<ModuleEnergyUnit> module = getModule(stack, MekanismModules.ENERGY_UNIT);
        return module == null ? MekanismConfig.gear.mekaSuitBaseChargeRate.get() : module.getCustomInstance().getChargeRate(module);
    }

    public float getDamageAbsorbed(ItemStack stack, DamageSource source, float amount) {
        // don't handle magic as it's handled by inhalation purification
        // don't handle starving as player should have nutritional injection
        // don't handle out of world (ever)
        if (!ALWAYS_SUPPORTED_SOURCES.contains(source) && source.isBypassArmor()) {
            return 0;
        }
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer != null && amount > 0) {
            float toAbsorb = amount * absorption;
            if (toAbsorb > 0) {
                FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageDamage.get().multiply(toAbsorb);
                if (usage.isZero()) {
                    //No energy is actually needed to absorb the damage, either because of the config
                    // or how small the amount to absorb is
                    return absorption;
                }
                return absorption * energyContainer.extract(usage, Action.EXECUTE, AutomationType.MANUAL).divide(usage).floatValue();
            }
        }
        return 0;
    }

    // This is unused for the most part; toughness / damage reduction is handled manually
    protected static class MekaSuitMaterial extends BaseSpecialArmorMaterial {

        @Override
        public float getKnockbackResistance() {
            return 0.1F;
        }

        @Nonnull
        @Override
        public String getName() {
            return Mekanism.MODID + ":mekasuit";
        }
    }
}
