package mekanism.common.item.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.datamaps.MekaSuitAbsorption;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ICustomModule.ModuleDamageAbsorbInfo;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData.ExclusiveFlag;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.IAttachmentAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.capabilities.chemical.variable.RateLimitGasTank;
import mekanism.common.capabilities.energy.item.NoClampRateLimitEnergyContainer;
import mekanism.common.capabilities.fluid.item.FluidTankSpec;
import mekanism.common.capabilities.fluid.item.RateLimitFluidTank;
import mekanism.common.capabilities.laser.item.LaserDissipationHandler;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.gear.mekasuit.ModuleElytraUnit;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.content.gear.shared.ModuleEnergyUnit;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismDataMapTypes;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismModules;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemMekaSuitArmor extends ItemSpecialArmor implements IModuleContainerItem, IJetpackItem, IAttributeRefresher, ICustomCreativeTabContents,
      IAttachmentAware {

    private static final AttributeModifier CREATIVE_FLIGHT_MODIFIER = new AttributeModifier(UUID.fromString("018e6622-7fc7-7334-af44-9f13564edb84"), "mekasuit_gravitational_modulation", 1D, Operation.ADDITION);
    private static final MekaSuitMaterial MEKASUIT_MATERIAL = new MekaSuitMaterial();

    private final AttributeCache attributeCache;
    private final AttributeCache attributeCacheWithFlight;
    //TODO: Expand this system so that modules can maybe define needed tanks?
    private final List<ChemicalTankSpec<Gas>> gasTankSpecs = new ArrayList<>();
    private final List<ChemicalTankSpec<Gas>> gasTankSpecsView = Collections.unmodifiableList(gasTankSpecs);
    private final List<FluidTankSpec> fluidTankSpecs = new ArrayList<>();
    private final List<FluidTankSpec> fluidTankSpecsView = Collections.unmodifiableList(fluidTankSpecs);
    private final float absorption;
    //Full laser dissipation causes 3/4 of the energy to be dissipated and the remaining energy to be refracted
    private final double laserDissipation;
    private final double laserRefraction;

    public ItemMekaSuitArmor(ArmorItem.Type armorType, Properties properties) {
        super(MEKASUIT_MATERIAL, armorType, properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1));
        CachedIntValue armorConfig;
        switch (armorType) {
            case HELMET -> {
                fluidTankSpecs.add(FluidTankSpec.createFillOnly(MekanismConfig.gear.mekaSuitNutritionalTransferRate, MekanismConfig.gear.mekaSuitNutritionalMaxStorage,
                      fluid -> fluid.is(MekanismFluids.NUTRITIONAL_PASTE.getFluid()), stack -> hasModule(stack, MekanismModules.NUTRITIONAL_INJECTION_UNIT)));
                absorption = 0.15F;
                laserDissipation = 0.15;
                laserRefraction = 0.2;
                armorConfig = MekanismConfig.gear.mekaSuitHelmetArmor;
            }
            case CHESTPLATE -> {
                gasTankSpecs.add(ChemicalTankSpec.createFillOnly(MekanismConfig.gear.mekaSuitJetpackTransferRate, stack -> {
                    //Note: We intentionally don't require the module to be enabled for purposes of calculating capacity
                    IModule<ModuleJetpackUnit> module = IModuleHelper.INSTANCE.load(stack, MekanismModules.JETPACK_UNIT);
                    return module != null ? MekanismConfig.gear.mekaSuitJetpackMaxStorage.get() * module.getInstalledCount() : 0L;
                }, gas -> gas == MekanismGases.HYDROGEN.get(), stack -> hasModule(stack, MekanismModules.JETPACK_UNIT)));
                absorption = 0.4F;
                laserDissipation = 0.3;
                laserRefraction = 0.4;
                armorConfig = MekanismConfig.gear.mekaSuitBodyArmorArmor;
            }
            case LEGGINGS -> {
                absorption = 0.3F;
                laserDissipation = 0.1875;
                laserRefraction = 0.25;
                armorConfig = MekanismConfig.gear.mekaSuitPantsArmor;
            }
            case BOOTS -> {
                absorption = 0.15F;
                laserDissipation = 0.1125;
                laserRefraction = 0.15;
                armorConfig = MekanismConfig.gear.mekaSuitBootsArmor;
            }
            default -> throw new IllegalArgumentException("Unknown Equipment Slot Type");
        }
        this.attributeCache = new AttributeCache(this, armorConfig, MekanismConfig.gear.mekaSuitToughness, MekanismConfig.gear.mekaSuitKnockbackResistance);
        this.attributeCacheWithFlight = new AttributeCache(builder -> {
            this.addToBuilder(builder);
            builder.put(NeoForgeMod.CREATIVE_FLIGHT.value(), CREATIVE_FLIGHT_MODIFIER);
        }, armorConfig, MekanismConfig.gear.mekaSuitToughness, MekanismConfig.gear.mekaSuitKnockbackResistance);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.mekaSuit());
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        // safety check
        return 0;
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        ModuleHelper.INSTANCE.dropModuleContainerContents(item, damageSource);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, true);
            if (!gasTankSpecs.isEmpty()) {
                StorageUtils.addStoredGas(stack, tooltip, true, false);
            }
            if (!fluidTankSpecs.isEmpty()) {
                StorageUtils.addStoredFluid(stack, tooltip, true);
            }
            tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @Override
    public boolean makesPiglinsNeutral(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return true;
    }

    @Override
    public boolean isEnderMask(@NotNull ItemStack stack, @NotNull Player player, @NotNull EnderMan enderman) {
        return type == ArmorItem.Type.HELMET;
    }

    @Override
    public boolean canWalkOnPowderedSnow(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return type == ArmorItem.Type.BOOTS;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    public boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        //Try to avoid replacing this item if there are any modules currently installed
        return super.isNotReplaceableByPickAction(stack, player, inventorySlot) || hasInstalledModules(stack);
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        //Enchantments in our data
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainerNullable(stack);
        int moduleLevel = container == null ? 0 : container.getModuleEnchantmentLevel(enchantment);
        return Math.max(moduleLevel, super.getEnchantmentLevel(stack, enchantment));
    }

    @Override
    public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = super.getAllEnchantments(stack);
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainerNullable(stack);
        if (container != null) {
            for (Entry<Enchantment, Integer> entry : container.moduleBasedEnchantments().entrySet()) {
                enchantments.merge(entry.getKey(), entry.getValue(), Math::max);
            }
        }
        return enchantments;
    }

    @Override
    public void addItems(CreativeModeTab.Output tabOutput) {
        tabOutput.accept(StorageUtils.getFilledEnergyVariant(new ItemStack(this)));
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (slotId >= Inventory.INVENTORY_SIZE && slotId < Inventory.INVENTORY_SIZE + 4 && entity instanceof Player player) {
            ModuleContainer container = ModuleHelper.get().getModuleContainerNullable(stack);
            if (container != null) {
                for (Module<?> module : container.modules()) {
                    module.tick(player);
                }
            }
        }
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        ContainerType.ENERGY.addDefaultContainer(eventBus, this, stack -> NoClampRateLimitEnergyContainer.create(
              () -> ModuleEnergyUnit.getChargeRate(stack, MekanismConfig.gear.mekaSuitBaseChargeRate.get()),
              () -> ModuleEnergyUnit.getEnergyCapacity(stack, MekanismConfig.gear.mekaSuitBaseEnergyCapacity.get())
        ), MekanismConfig.gear);

        if (!gasTankSpecs.isEmpty()) {
            ContainerType.GAS.addDefaultContainers(eventBus, this, stack -> {
                List<IGasTank> list = new ArrayList<>(gasTankSpecs.size());
                for (ChemicalTankSpec<Gas> spec : gasTankSpecs) {
                    list.add(spec.createTank(RateLimitGasTank::create, stack));
                }
                return list;
            }, MekanismConfig.gear);
        }
        if (!fluidTankSpecs.isEmpty()) {
            ContainerType.FLUID.addDefaultContainers(eventBus, this, stack -> {
                List<IExtendedFluidTank> list = new ArrayList<>(fluidTankSpecs.size());
                for (FluidTankSpec spec : fluidTankSpecs) {
                    list.add(spec.createTank(RateLimitFluidTank::create, stack));
                }
                return list;
            }, MekanismConfig.gear);
        }
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        super.attachCapabilities(event);
        //Note: The all our providers only expose the capabilities (both those via attachments and those here) if the required configs for initializing that capability are loaded
        event.registerItem(Capabilities.RADIATION_SHIELDING, (stack, ctx) -> {
            if (!MekanismConfig.gear.isLoaded() || !isModuleEnabled(stack, MekanismModules.RADIATION_SHIELDING_UNIT)) {
                return null;
            }
            return RadiationShieldingHandler.create(ItemHazmatSuitArmor.getShieldingByArmor(getType()));
        }, this);

        event.registerItem(Capabilities.LASER_DISSIPATION, (stack, ctx) -> {
            //Note: This doesn't rely on configs, so we can skip the gear loaded check
            return isModuleEnabled(stack, MekanismModules.LASER_DISSIPATION_UNIT) ? LaserDissipationHandler.create(laserDissipation, laserRefraction) : null;
        }, this);
    }

    public List<ChemicalTankSpec<Gas>> getGasTankSpecs() {
        return gasTankSpecsView;
    }

    public List<FluidTankSpec> getFluidTankSpecs() {
        return fluidTankSpecsView;
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        //Note: We ignore radial modes as those are just for the Meka-Tool currently
        return slotType == getEquipmentSlot() && getModules(stack).stream().anyMatch(IModule::handlesModeChange);
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        if (getType() == ArmorItem.Type.CHESTPLATE && !entity.isShiftKeyDown()) {
            //Don't allow elytra flight if the player is sneaking. This lets the player exit elytra flight early
            IModuleContainer container = moduleContainer(stack);
            if (container != null) {
                IModule<ModuleElytraUnit> elytra = container.getIfEnabled(MekanismModules.ELYTRA_UNIT);
                if (elytra != null && elytra.canUseEnergy(entity, MekanismConfig.gear.mekaSuitElytraEnergyUsage.get())) {
                    //If we can use the elytra, check if the jetpack unit is also installed, and if it is,
                    // only mark that we can use the elytra if the jetpack is not set to hover or if it is if it has no hydrogen stored
                    IModule<ModuleJetpackUnit> jetpack = container.getIfEnabled(MekanismModules.JETPACK_UNIT);
                    return jetpack == null || jetpack.getCustomInstance().getMode() != JetpackMode.HOVER ||
                           StorageUtils.getContainedGas(stack, MekanismGases.HYDROGEN).isEmpty();
                }
            }
        }
        return false;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        //Note: As canElytraFly is checked just before this we don't bother validating ahead of time we have the energy
        // or that we are the correct slot
        if (!entity.level().isClientSide) {
            int nextFlightTicks = flightTicks + 1;
            if (nextFlightTicks % MekanismUtils.TICKS_PER_HALF_SECOND == 0) {
                if (nextFlightTicks % SharedConstants.TICKS_PER_SECOND == 0) {
                    IModule<ModuleElytraUnit> module = getEnabledModule(stack, MekanismModules.ELYTRA_UNIT);
                    if (module != null) {
                        module.useEnergy(entity, MekanismConfig.gear.mekaSuitElytraEnergyUsage.get());
                    }
                }
                entity.gameEvent(GameEvent.ELYTRA_GLIDE);
            }
        }
        return true;
    }

    @Override
    public boolean canUseJetpack(ItemStack stack) {
        if (type == ArmorItem.Type.CHESTPLATE) {
            if (isModuleEnabled(stack, MekanismModules.JETPACK_UNIT)) {
                return ChemicalUtil.hasChemical(stack, MekanismGases.HYDROGEN.get());
            }
            return getModules(stack).stream().anyMatch(module -> module.isEnabled() && module.getData().isExclusive(ExclusiveFlag.OVERRIDE_JUMP.getMask()));
        }
        return false;
    }

    @Override
    public JetpackMode getJetpackMode(ItemStack stack) {
        IModule<ModuleJetpackUnit> module = getEnabledModule(stack, MekanismModules.JETPACK_UNIT);
        if (module != null) {
            return module.getCustomInstance().getMode();
        }
        return JetpackMode.DISABLED;
    }

    @Override
    public double getJetpackThrust(ItemStack stack) {
        IModule<ModuleJetpackUnit> module = getEnabledModule(stack, MekanismModules.JETPACK_UNIT);
        if (module != null) {
            float thrustMultiplier = module.getCustomInstance().getThrustMultiplier();
            int neededGas = Mth.ceil(thrustMultiplier);
            //Note: We verified we have at least one mB of gas before we get to the point of getting the thrust,
            // so we only need to do extra validation if we need more than a single mB of hydrogen
            if (neededGas > 1) {
                GasStack containedGas = StorageUtils.getContainedGas(stack, MekanismGases.HYDROGEN);
                if (neededGas > containedGas.getAmount()) {
                    //If we don't have enough gas stored to go at the set thrust, scale down the thrust
                    // to be whatever gas we have remaining
                    thrustMultiplier = containedGas.getAmount();
                }
            }
            return 0.15 * thrustMultiplier;
        }
        return 0;
    }

    @Override
    public void useJetpackFuel(ItemStack stack) {
        IModule<ModuleJetpackUnit> module = getEnabledModule(stack, MekanismModules.JETPACK_UNIT);
        if (module != null) {
            IGasHandler gasHandlerItem = Capabilities.GAS.getCapability(stack);
            if (gasHandlerItem != null) {
                int amount = Mth.ceil(module.getCustomInstance().getThrustMultiplier());
                gasHandlerItem.extractChemical(MekanismGases.HYDROGEN.getStack(amount), Action.EXECUTE);
            }
        }
    }

    @NotNull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
        if (slot == getEquipmentSlot()) {
            if (slot == EquipmentSlot.CHEST && CommonPlayerTickHandler.isGravitationalModulationReady(stack)) {
                return attributeCacheWithFlight.get();
            }
            return attributeCache.get();
        }
        return ImmutableMultimap.of();
    }

    @Override
    public void addToBuilder(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        UUID modifier = ARMOR_MODIFIER_UUID_PER_TYPE.get(getType());
        builder.put(Attributes.ARMOR, new AttributeModifier(modifier, "Armor modifier", getDefense(), Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifier, "Armor toughness", getToughness(), Operation.ADDITION));
        builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(modifier, "Armor knockback resistance", getMaterial().getKnockbackResistance(),
              Operation.ADDITION));
    }

    @Override
    public int getDefense() {
        return getMaterial().getDefenseForType(getType());
    }

    @Override
    public float getToughness() {
        return getMaterial().getToughness();
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        //Ignore NBT for energized items causing re-equip animations
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        //Ignore NBT for energized items causing block break reset
        return oldStack.getItem() != newStack.getItem();
    }

    public static float getDamageAbsorbed(Player player, DamageSource source, float amount) {
        return getDamageAbsorbed(player, source, amount, null);
    }

    public static boolean tryAbsorbAll(Player player, DamageSource source, float amount) {
        List<Runnable> energyUsageCallbacks = new ArrayList<>(4);
        if (getDamageAbsorbed(player, source, amount, energyUsageCallbacks) >= 1) {
            //If we can fully absorb it, actually use the energy from the various pieces and then return that we absorbed it all
            for (Runnable energyUsageCallback : energyUsageCallbacks) {
                energyUsageCallback.run();
            }
            return true;
        }
        return false;
    }

    private static float getDamageAbsorbed(Player player, DamageSource source, float amount, @Nullable List<Runnable> energyUseCallbacks) {
        if (amount <= 0) {
            return 0;
        }
        float ratioAbsorbed = 0;
        List<FoundArmorDetails> armorDetails = new ArrayList<>();
        //Start by looping the armor, allowing modules to absorb damage if they can
        for (ItemStack stack : player.getArmorSlots()) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemMekaSuitArmor armor) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer != null) {
                    FoundArmorDetails details = new FoundArmorDetails(energyContainer, armor);
                    armorDetails.add(details);
                    for (IModule<?> module : details.armor.getModules(stack)) {
                        if (module.isEnabled()) {
                            ModuleDamageAbsorbInfo damageAbsorbInfo = getModuleDamageAbsorbInfo(module, source);
                            if (damageAbsorbInfo != null) {
                                float absorption = damageAbsorbInfo.absorptionRatio().getAsFloat();
                                ratioAbsorbed += absorbDamage(details.usageInfo, amount, absorption, ratioAbsorbed, damageAbsorbInfo.energyCost());
                                if (ratioAbsorbed >= 1) {
                                    //If we have fully absorbed the damage, stop checking/trying to absorb more
                                    break;
                                }
                            }
                        }
                    }
                    if (ratioAbsorbed >= 1) {
                        //If we have fully absorbed the damage, stop checking/trying to absorb more
                        break;
                    }
                }
            }
        }
        if (ratioAbsorbed < 1) {
            //If we haven't fully absorbed it check the individual pieces of armor for if they can absorb any
            Float absorbRatio = null;
            for (FoundArmorDetails details : armorDetails) {
                if (absorbRatio == null) {
                    //If we haven't looked up yet if we can absorb the damage type and if we can't
                    // stop checking if the armor is able to
                    if (source.is(Tags.DamageTypes.IS_TECHNICAL) || !source.is(MekanismTags.DamageTypes.MEKASUIT_ALWAYS_SUPPORTED) && source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                        break;
                    }
                    // Next lookup the ratio at which we can absorb the given damage type from the data map
                    MekaSuitAbsorption absorptionData = null;
                    if (source.typeHolder().unwrapKey().isPresent()) {
                        // Reference holders can query data map values
                        absorptionData = source.typeHolder().getData(MekanismDataMapTypes.MEKA_SUIT_ABSORPTION);
                    } else {
                        // Note: In theory the above path should always be done as vanilla only makes damage sources with reference holders
                        // but just in case have the fallback to look up the name from the registry
                        Optional<Registry<DamageType>> registry = player.level().registryAccess().registry(Registries.DAMAGE_TYPE);
                        if (registry.isPresent()) {
                            absorptionData = registry.get().wrapAsHolder(source.type()).getData(MekanismDataMapTypes.MEKA_SUIT_ABSORPTION);
                        }
                    }
                    if (absorptionData != null) {
                        absorbRatio = absorptionData.absorption();
                    } else {
                        absorbRatio = MekanismConfig.gear.mekaSuitUnspecifiedDamageRatio.get();
                    }
                    if (absorbRatio == 0) {
                        //If the config or the data map specifies that the damage type shouldn't be blocked at all
                        // stop checking if the armor is able to
                        break;
                    }
                }
                float absorption = details.armor.absorption * absorbRatio;
                ratioAbsorbed += absorbDamage(details.usageInfo, amount, absorption, ratioAbsorbed, MekanismConfig.gear.mekaSuitEnergyUsageDamage);
                if (ratioAbsorbed >= 1) {
                    //If we have fully absorbed the damage, stop checking/trying to absorb more
                    break;
                }
            }
        }
        for (FoundArmorDetails details : armorDetails) {
            //Use energy/or enqueue usage for each piece as needed
            if (!details.usageInfo.energyUsed.isZero()) {
                if (energyUseCallbacks == null) {
                    details.energyContainer.extract(details.usageInfo.energyUsed, Action.EXECUTE, AutomationType.MANUAL);
                } else {
                    energyUseCallbacks.add(() -> details.energyContainer.extract(details.usageInfo.energyUsed, Action.EXECUTE, AutomationType.MANUAL));
                }
            }
        }
        return Math.min(ratioAbsorbed, 1);
    }

    @Nullable
    private static <MODULE extends ICustomModule<MODULE>> ModuleDamageAbsorbInfo getModuleDamageAbsorbInfo(IModule<MODULE> module, DamageSource damageSource) {
        return module.getCustomInstance().getDamageAbsorbInfo(module, damageSource);
    }

    private static float absorbDamage(EnergyUsageInfo usageInfo, float amount, float absorption, float currentAbsorbed, FloatingLongSupplier energyCost) {
        //Cap the amount that we can absorb to how much we have left to absorb
        absorption = Math.min(1 - currentAbsorbed, absorption);
        float toAbsorb = amount * absorption;
        if (toAbsorb > 0) {
            FloatingLong usage = energyCost.get().multiply(toAbsorb);
            if (usage.isZero()) {
                //No energy is actually needed to absorb the damage, either because of the config
                // or how small the amount to absorb is
                return absorption;
            } else if (usageInfo.energyAvailable.greaterOrEqual(usage)) {
                //If we have more energy available than we need, increase how much energy we "used"
                // and decrease how much we have available.
                usageInfo.energyUsed = usageInfo.energyUsed.plusEqual(usage);
                usageInfo.energyAvailable = usageInfo.energyAvailable.minusEqual(usage);
                return absorption;
            } else if (!usageInfo.energyAvailable.isZero()) {
                //Otherwise, if we have energy available but not as much as needed to fully absorb it
                // then we calculate what ratio we are able to block
                float absorbedPercent = usageInfo.energyAvailable.divide(usage).floatValue();
                usageInfo.energyUsed = usageInfo.energyUsed.plusEqual(usageInfo.energyAvailable);
                usageInfo.energyAvailable = FloatingLong.ZERO;
                return absorption * absorbedPercent;
            }
        }
        return 0;
    }

    private static class FoundArmorDetails {

        private final IEnergyContainer energyContainer;
        private final EnergyUsageInfo usageInfo;
        private final ItemMekaSuitArmor armor;

        public FoundArmorDetails(IEnergyContainer energyContainer, ItemMekaSuitArmor armor) {
            this.energyContainer = energyContainer;
            this.usageInfo = new EnergyUsageInfo(energyContainer.getEnergy());
            this.armor = armor;
        }
    }

    private static class EnergyUsageInfo {

        private FloatingLong energyAvailable;
        private FloatingLong energyUsed = FloatingLong.ZERO;

        public EnergyUsageInfo(FloatingLong energyAvailable) {
            //Copy it so we can just use minusEquals without worry
            this.energyAvailable = energyAvailable.copy();
        }
    }

    // This is unused for the most part; toughness / damage reduction is handled manually, though it can fall back to netherite values
    protected static class MekaSuitMaterial extends BaseSpecialArmorMaterial {

        @Override
        public int getDefenseForType(@NotNull ArmorItem.Type armorType) {
            return switch (armorType) {
                case BOOTS -> MekanismConfig.gear.mekaSuitBootsArmor.getOrDefault();
                case LEGGINGS -> MekanismConfig.gear.mekaSuitPantsArmor.getOrDefault();
                case CHESTPLATE -> MekanismConfig.gear.mekaSuitBodyArmorArmor.getOrDefault();
                case HELMET -> MekanismConfig.gear.mekaSuitHelmetArmor.getOrDefault();
            };
        }

        @Override
        public float getToughness() {
            return MekanismConfig.gear.mekaSuitToughness.getOrDefault();
        }

        @Override
        public float getKnockbackResistance() {
            return MekanismConfig.gear.mekaSuitKnockbackResistance.getOrDefault();
        }

        @NotNull
        @Override
        public String getName() {
            return Mekanism.MODID + ":mekasuit";
        }
    }
}
