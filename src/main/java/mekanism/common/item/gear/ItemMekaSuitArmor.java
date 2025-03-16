package mekanism.common.item.gear;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.MekaSuitAbsorption;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ICustomModule.ModuleDamageAbsorbInfo;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData.ExclusiveFlag;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.IAttachmentAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.attachments.containers.fluid.FluidTanksBuilder;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.chemical.item.ChemicalTankSpec;
import mekanism.common.capabilities.fluid.item.FluidTankSpec;
import mekanism.common.capabilities.laser.item.LaserDissipationHandler;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.gear.mekasuit.ModuleElytraUnit;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismArmorMaterials;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemMekaSuitArmor extends ItemSpecialArmor implements IModuleContainerItem, IJetpackItem, ICustomCreativeTabContents, IAttachmentAware, ICapabilityAware {

    //TODO: Expand this system so that modules can maybe define needed tanks?
    private final List<ChemicalTankSpec> chemicalTankSpecs = new ArrayList<>();
    private final List<ChemicalTankSpec> chemicalTankSpecsView = Collections.unmodifiableList(chemicalTankSpecs);
    private final List<FluidTankSpec> fluidTankSpecs = new ArrayList<>();
    private final List<FluidTankSpec> fluidTankSpecsView = Collections.unmodifiableList(fluidTankSpecs);
    private final float absorption;
    //Full laser dissipation causes 3/4 of the energy to be dissipated and the remaining energy to be refracted
    private final double laserDissipation;
    private final double laserRefraction;

    public ItemMekaSuitArmor(ArmorItem.Type armorType, Properties properties) {
        super(MekanismArmorMaterials.MEKASUIT, armorType, IModuleHelper.INSTANCE.applyModuleContainerProperties(
              properties.rarity(Rarity.EPIC).setNoRepair().stacksTo(1)
        ));
        switch (armorType) {
            case HELMET -> {
                fluidTankSpecs.add(FluidTankSpec.createFillOnly(MekanismConfig.gear.mekaSuitNutritionalTransferRate, MekanismConfig.gear.mekaSuitNutritionalMaxStorage,
                      fluid -> fluid.is(MekanismFluids.NUTRITIONAL_PASTE), stack -> hasModule(stack, MekanismModules.NUTRITIONAL_INJECTION_UNIT)));
                absorption = 0.15F;
                laserDissipation = 0.15;
                laserRefraction = 0.2;
            }
            case CHESTPLATE -> {
                chemicalTankSpecs.add(ChemicalTankSpec.createFillOnly(MekanismConfig.gear.mekaSuitJetpackTransferRate, stack -> {
                    //Note: We intentionally don't require the module to be enabled for purposes of calculating capacity
                    IModule<ModuleJetpackUnit> module = IModuleHelper.INSTANCE.getModule(stack, MekanismModules.JETPACK_UNIT);
                    return module != null ? MekanismConfig.gear.mekaSuitJetpackMaxStorage.get() * module.getInstalledCount() : 0L;
                }, chemical -> chemical.is(MekanismChemicals.HYDROGEN), stack -> hasModule(stack, MekanismModules.JETPACK_UNIT)));
                absorption = 0.4F;
                laserDissipation = 0.3;
                laserRefraction = 0.4;
            }
            case LEGGINGS -> {
                absorption = 0.3F;
                laserDissipation = 0.1875;
                laserRefraction = 0.25;
            }
            case BOOTS -> {
                absorption = 0.15F;
                laserDissipation = 0.1125;
                laserRefraction = 0.15;
            }
            default -> throw new IllegalArgumentException("Unknown Equipment Slot Type");
        }
    }

    @Override
    public <T extends LivingEntity> int damageItem(@NotNull ItemStack stack, int amount, T entity, @NotNull Consumer<Item> onBroken) {
        // safety check
        return 0;
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        ModuleHelper.INSTANCE.dropModuleContainerContents(item, damageSource);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltip);
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, true);
            if (!chemicalTankSpecs.isEmpty()) {
                StorageUtils.addStoredChemical(stack, tooltip);
            }
            if (!fluidTankSpecs.isEmpty()) {
                StorageUtils.addStoredFluid(stack, tooltip);
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
    public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        //Enchantments in our data
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
        int moduleLevel = container == null ? 0 : container.getModuleEnchantmentLevel(enchantment);
        return Math.max(moduleLevel, super.getEnchantmentLevel(stack, enchantment));
    }

    @NotNull
    @Override
    public ItemEnchantments getAllEnchantments(@NotNull ItemStack stack, RegistryLookup<Enchantment> lookup) {
        ItemEnchantments enchantments = super.getAllEnchantments(stack, lookup);
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
        if (container != null) {
            ItemEnchantments moduleEnchantments = container.moduleBasedEnchantments();
            if (enchantments.isEmpty()) {
                //Skip copying if there are no builtin enchantments
                return moduleEnchantments;
            } else if (!moduleEnchantments.isEmpty()) {
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : moduleEnchantments.entrySet()) {
                    mutable.upgrade(entry.getKey(), entry.getIntValue());
                }
                return mutable.toImmutable();
            }
        }
        return enchantments;
    }

    @Override
    public void addItems(Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(StorageUtils.getFilledEnergyVariant(item));
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (slotId >= Inventory.INVENTORY_SIZE && slotId < Inventory.INVENTORY_SIZE + 4 && entity instanceof Player player) {
            ModuleContainer container = ModuleHelper.get().getModuleContainer(stack);
            if (container != null) {
                for (Module<?> module : container.modules()) {
                    module.tick(container, stack, player);
                }
            }
        }
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        if (!chemicalTankSpecs.isEmpty()) {
            ContainerType.CHEMICAL.addDefaultCreators(eventBus, this, () -> {
                ChemicalTanksBuilder builder = ChemicalTanksBuilder.builder();
                for (ChemicalTankSpec spec : chemicalTankSpecs) {
                    spec.addTank(builder, ComponentBackedChemicalTank::new);
                }
                return builder.build();
            }, MekanismConfig.gear);
        }
        if (!fluidTankSpecs.isEmpty()) {
            ContainerType.FLUID.addDefaultCreators(eventBus, this, () -> {
                FluidTanksBuilder builder = FluidTanksBuilder.builder();
                for (FluidTankSpec spec : fluidTankSpecs) {
                    spec.addTank(builder, ComponentBackedFluidTank::new);
                }
                return builder.build();
            }, MekanismConfig.gear);
        }
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
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

    public List<ChemicalTankSpec> getChemicalTankSpecs() {
        return chemicalTankSpecsView;
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
                if (elytra != null && elytra.canUseEnergy(entity, stack, MekanismConfig.gear.mekaSuitElytraEnergyUsage.get())) {
                    //If we can use the elytra, check if the jetpack unit is also installed, and if it is,
                    // only mark that we can use the elytra if the jetpack is not set to hover or if it is if it has no hydrogen stored
                    IModule<ModuleJetpackUnit> jetpack = container.getIfEnabled(MekanismModules.JETPACK_UNIT);
                    return jetpack == null || jetpack.getCustomInstance().mode() != JetpackMode.HOVER ||
                           StorageUtils.getContainedChemical(stack, MekanismChemicals.HYDROGEN).isEmpty();
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
                        module.useEnergy(entity, stack, MekanismConfig.gear.mekaSuitElytraEnergyUsage.get());
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
                return ChemicalUtil.hasChemicalOfType(stack, MekanismChemicals.HYDROGEN.get());
            }
            return getModules(stack).stream().anyMatch(module -> module.isEnabled() && module.getUntypedData().isExclusive(ExclusiveFlag.OVERRIDE_JUMP.getMask()));
        }
        return false;
    }

    @Override
    public JetpackMode getJetpackMode(ItemStack stack) {
        IModule<ModuleJetpackUnit> module = getEnabledModule(stack, MekanismModules.JETPACK_UNIT);
        if (module != null) {
            return module.getCustomInstance().mode();
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
                ChemicalStack containedGas = StorageUtils.getContainedChemical(stack, MekanismChemicals.HYDROGEN);
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
            IChemicalHandler gasHandlerItem = Capabilities.CHEMICAL.getCapability(stack);
            if (gasHandlerItem != null) {
                int amount = Mth.ceil(module.getCustomInstance().getThrustMultiplier());
                gasHandlerItem.extractChemical(MekanismChemicals.HYDROGEN.asStack(amount), Action.EXECUTE);
            }
        }
    }

    @Override
    public int getDefense() {
        return getMaterial().value().getDefense(getType());
    }

    @Override
    public float getToughness() {
        return getMaterial().value().toughness();
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
                    if (source.is(Tags.DamageTypes.IS_TECHNICAL) || !source.is(MekanismAPITags.DamageTypes.MEKASUIT_ALWAYS_SUPPORTED) && source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                        break;
                    }
                    // Next lookup the ratio at which we can absorb the given damage type from the data map
                    MekaSuitAbsorption absorptionData = IMekanismDataMapTypes.INSTANCE.getMekaSuitAbsorption(player.registryAccess(), source.typeHolder());
                    if (absorptionData == null) {
                        absorbRatio = MekanismConfig.gear.mekaSuitUnspecifiedDamageRatio.get();
                    } else {
                        absorbRatio = absorptionData.absorption();
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
            details.drainEnergy();
        }
        return Math.min(ratioAbsorbed, 1);
    }

    @Nullable
    private static <MODULE extends ICustomModule<MODULE>> ModuleDamageAbsorbInfo getModuleDamageAbsorbInfo(IModule<MODULE> module, DamageSource damageSource) {
        return module.getCustomInstance().getDamageAbsorbInfo(module, damageSource);
    }

    private static float absorbDamage(EnergyUsageInfo usageInfo, float amount, float absorption, float currentAbsorbed, LongSupplier energyCost) {
        //Cap the amount that we can absorb to how much we have left to absorb
        absorption = Math.min(1 - currentAbsorbed, absorption);
        float toAbsorb = amount * absorption;
        if (toAbsorb > 0) {
            long usage = MathUtils.ceilToLong(energyCost.getAsLong() * toAbsorb);
            if (usage == 0L) {
                //No energy is actually needed to absorb the damage, either because of the config
                // or how small the amount to absorb is
                return absorption;
            } else if (usageInfo.energyAvailable >= usage) {
                //If we have more energy available than we need, increase how much energy we "used"
                // and decrease how much we have available.
                usageInfo.energyUsed += usage;
                usageInfo.energyAvailable -= usage;
                return absorption;
            } else if (usageInfo.energyAvailable > 0L) {
                //Otherwise, if we have energy available but not as much as needed to fully absorb it
                // then we calculate what ratio we are able to block
                float absorbedPercent = (float) (usageInfo.energyAvailable / (double) usage);
                usageInfo.energyUsed += usageInfo.energyAvailable;
                usageInfo.energyAvailable = 0L;
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

        public void drainEnergy() {
            energyContainer.extract(usageInfo.energyUsed, Action.EXECUTE, AutomationType.MANUAL);
        }
    }

    private static class EnergyUsageInfo {

        private long energyAvailable;
        private long energyUsed = 0;

        public EnergyUsageInfo(long energyAvailable) {
            this.energyAvailable = energyAvailable;
        }
    }
}
