package mekanism.common.item.gear;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.MekaSuitAbsorption;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ICustomModule.ModuleDamageAbsorbInfo;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData.ExclusiveFlag;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.GenericTankSpec;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.laser.item.LaserDissipationHandler;
import mekanism.common.capabilities.proxy.AutomatedEnergyHandler;
import mekanism.common.capabilities.proxy.AutomatedResourceHandler;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.component.IComponentAware;
import mekanism.common.component.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.component.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.component.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.component.containers.fluid.FluidTanksBuilder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.ModuleContainer;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.gear.mekasuit.ModuleElytraUnit;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismArmorMaterials;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.LivingEntityEquipmentWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ItemMekaSuitArmor extends ItemSpecialArmor implements IModuleContainerItem, IJetpackItem, ICustomCreativeTabContents, IComponentAware, ICapabilityAware {

    //TODO - 26.2: Expand this system so that modules can maybe define needed tanks?
    // Maybe we can define some of the things via a datapack registry, and then have modules declare what types of tanks they need?
    private final List<GenericTankSpec<ChemicalResource>> chemicalTankSpecs = new ArrayList<>();
    private final List<GenericTankSpec<ChemicalResource>> chemicalTankSpecsView = Collections.unmodifiableList(chemicalTankSpecs);
    private final List<GenericTankSpec<FluidResource>> fluidTankSpecs = new ArrayList<>();
    private final List<GenericTankSpec<FluidResource>> fluidTankSpecsView = Collections.unmodifiableList(fluidTankSpecs);
    //Full laser dissipation causes 3/4 of the energy to be dissipated and the remaining energy to be refracted
    private final float laserDissipation;
    private final float laserRefraction;
    private final ArmorType armorType;

    public ItemMekaSuitArmor(ArmorType armorType, Item.Properties properties) {
        super(MekanismArmorMaterials.MEKASUIT, armorType, IModuleHelper.INSTANCE.applyModuleContainerProperties(
              properties.rarity(Rarity.EPIC).setNoCombineRepair().stacksTo(1)
        ));
        this.armorType = armorType;
        switch (this.armorType) {
            case HELMET -> {
                fluidTankSpecs.add(GenericTankSpec.createFillOnly(MekanismConfig.gear.mekaSuitNutritionalTransferRate, MekanismConfig.gear.mekaSuitNutritionalMaxStorage,
                      fluid -> fluid.is(MekanismFluids.NUTRITIONAL_PASTE), itemType -> hasModule(itemType, MekanismModules.NUTRITIONAL_INJECTION_UNIT)));
                laserDissipation = 0.15F;
                laserRefraction = 0.2F;
            }
            case CHESTPLATE -> {
                chemicalTankSpecs.add(GenericTankSpec.createFillOnly(MekanismConfig.gear.mekaSuitJetpackTransferRate, itemAccess -> {
                    //Note: We intentionally don't require the module to be enabled for purposes of calculating capacity
                    ItemResource itemType = itemAccess.getResource();
                    if (itemType.isEmpty()) {
                        return 0;
                    }
                    IModule<ModuleJetpackUnit> module = IModuleHelper.INSTANCE.getModule(itemType, MekanismModules.JETPACK_UNIT);
                    return module == null ? 0L : MekanismConfig.gear.mekaSuitJetpackMaxStorage.get() * module.getInstalledCount();
                }, chemical -> chemical.is(ChemicalIds.HYDROGEN), itemType -> hasModule(itemType, MekanismModules.JETPACK_UNIT)));
                laserDissipation = 0.3F;
                laserRefraction = 0.4F;
            }
            case LEGGINGS -> {
                laserDissipation = 0.1875F;
                laserRefraction = 0.25F;
            }
            case BOOTS -> {
                laserDissipation = 0.1125F;
                laserRefraction = 0.15F;
            }
            default -> throw new IllegalArgumentException("Unknown Equipment Slot Type");
        }
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        // safety check
        return 0;
    }

    @Override
    public void onDestroyed(ItemEntity item, DamageSource damageSource) {
        ModuleHelper.INSTANCE.dropModuleContainerContents(item, damageSource);
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addModuleDetails(stack, tooltipAdder);
        } else {
            ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(stack);
            StorageUtils.addStoredEnergy(itemAccess, tooltipAdder, true);
            if (!chemicalTankSpecs.isEmpty()) {
                StorageUtils.addStoredChemical(itemAccess, tooltipAdder);
            }
            if (!fluidTankSpecs.isEmpty()) {
                StorageUtils.addStoredFluid(itemAccess, tooltipAdder, MekanismLang.NO_FLUID_TOOLTIP);
            }
            tooltipAdder.accept(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return true;
    }

    @Override
    public boolean isGazeDisguise(ItemStack stack, Player player, @Nullable LivingEntity entity) {
        return true;//only called on helmet slot, no need to check type
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return armorType == ArmorType.BOOTS;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return StorageUtils.isEnergyBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return StorageUtils.getEnergyBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return MekanismConfig.client.energyColor.get();
    }

    @Override
    public boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
        //Try to avoid replacing this item if there are any modules currently installed
        return super.isNotReplaceableByPickAction(stack, player, inventorySlot) || hasInstalledModules(stack);
    }

    @Override
    public int getEnchantmentLevel(ItemInstance instance, Holder<Enchantment> enchantment) {
        //Enchantments in our data
        IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(instance);
        int moduleLevel = container == null ? 0 : container.getModuleEnchantmentLevel(enchantment);
        return Math.max(moduleLevel, super.getEnchantmentLevel(instance, enchantment));
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, RegistryLookup<Enchantment> lookup) {
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
    public void addItems(ItemDisplayParameters displayParameters, Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(ContainerType.ENERGY.getFilledVariant(item, null));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (slot != null && slot.getType() == Type.HUMANOID_ARMOR && entity instanceof Player player) {
            ModuleContainer container = ModuleHelper.get().getModuleContainer(stack);
            if (container != null) {
                try (Transaction transaction = Transaction.openRoot()) {
                    //TODO - 26.2: Re-evaluate this item access
                    ItemAccess itemAccess = ItemAccess.forStack(stack);
                    for (Module<?> module : container.modules()) {
                        module.tick(itemAccess, player, transaction);
                    }
                    transaction.commit();
                }
            }
        }
    }

    @Override
    public void addComponents(IEventBus eventBus) {
        if (!chemicalTankSpecs.isEmpty()) {
            ContainerType.CHEMICAL.addDefaultCreators(eventBus, this, () -> {
                ChemicalTanksBuilder builder = ChemicalTanksBuilder.builder();
                for (GenericTankSpec<ChemicalResource> spec : chemicalTankSpecs) {
                    spec.addTank(builder, ComponentBackedChemicalTank::new);
                }
                return builder.build();
            }, MekanismConfig.gear);
        }
        if (!fluidTankSpecs.isEmpty()) {
            ContainerType.FLUID.addDefaultCreators(eventBus, this, () -> {
                FluidTanksBuilder builder = FluidTanksBuilder.builder();
                for (GenericTankSpec<FluidResource> spec : fluidTankSpecs) {
                    spec.addTank(builder, ComponentBackedFluidTank::new);
                }
                return builder.build();
            }, MekanismConfig.gear);
        }
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        //Note: The all our providers only expose the capabilities (both those via attachments and those here) if the required configs for initializing that capability are loaded
        event.registerItem(Capabilities.RADIATION_SHIELDING, (stack, _) -> {
            if (!MekanismConfig.gear.isLoaded() || !isModuleEnabled(stack, MekanismModules.RADIATION_SHIELDING_UNIT)) {
                return null;
            }
            return RadiationShieldingHandler.create(ItemHazmatSuitArmor.getShieldingByArmor(armorType));
        }, this);

        event.registerItem(Capabilities.LASER_DISSIPATION, (stack, _) -> {
            //Note: This doesn't rely on configs, so we can skip the gear loaded check
            return isModuleEnabled(stack, MekanismModules.LASER_DISSIPATION_UNIT) ? LaserDissipationHandler.create(laserDissipation, laserRefraction) : null;
        }, this);
    }

    public List<GenericTankSpec<ChemicalResource>> getChemicalTankSpecs() {
        return chemicalTankSpecsView;
    }

    public List<GenericTankSpec<FluidResource>> getFluidTankSpecs() {
        return fluidTankSpecsView;
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean supportsSlotType(ITEM instance, EquipmentSlot slotType) {
        //Note: We ignore radial modes as those are just for the Meka-Tool currently
        return slotType == armorType.getSlot() && getModules(instance).stream().anyMatch(IModule::handlesModeChange);
    }

    @Override
    public void onGlideDamage(ItemStack stack, LivingEntity wearer, EquipmentSlot slot) {
        //Note: As canElytraFly is checked just before this we don't bother validating ahead of time we have the energy
        // or that we are the correct slot
        IModule<ModuleElytraUnit> module = getEnabledModule(stack, MekanismModules.ELYTRA_UNIT);
        if (module != null) {
            try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                //Theoretically we can use the amount of energy specified the config, but in case we can't use as much as we can
                module.usePossibleEnergy(wearer, ItemAccess.forStack(stack), MekanismConfig.gear.mekaSuitElytraEnergyUsage.get(), transaction);
                transaction.commit();
            }
        }
    }

    @Override
    public boolean canUseJetpack(ItemAccess itemAccess) {
        if (armorType == ArmorType.CHESTPLATE) {
            ItemResource armor = itemAccess.getResource();
            if (isModuleEnabled(armor, MekanismModules.JETPACK_UNIT)) {
                return ChemicalUtils.hasChemicalOfType(itemAccess, ChemicalIds.HYDROGEN);
            }
            return getModules(armor).stream().anyMatch(module -> module.isEnabled() && module.getUntypedData().isExclusive(ExclusiveFlag.OVERRIDE_JUMP.getMask()));
        }
        return false;
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> JetpackMode getJetpackMode(ITEM instance) {
        IModule<ModuleJetpackUnit> module = getEnabledModule(instance, MekanismModules.JETPACK_UNIT);
        if (module != null) {
            return module.getCustomInstance().mode();
        }
        return JetpackMode.DISABLED;
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> double useJetpackFuel(RegistryAccess registryAccess, ItemAccess itemAccess, ITEM primaryInstance, TransactionContext transaction) {
        //Use the primary jetpack for calculating the thrust
        IModule<ModuleJetpackUnit> module = getEnabledModule(primaryInstance, MekanismModules.JETPACK_UNIT);
        if (module != null) {
            ChemicalResource fuel = ChemicalUtils.getResource(registryAccess, ChemicalIds.HYDROGEN);
            if (fuel.isEmpty()) {
                return 0;
            }
            ResourceHandler<ChemicalResource> handler = AutomatedResourceHandler.manual(Capabilities.CHEMICAL.getCapability(itemAccess));
            if (handler != null) {
                float thrustMultiplier = module.getCustomInstance().getThrustMultiplier();
                //If we don't have enough gas stored to go at the set thrust, scale down the thrust
                // to be whatever gas we have remaining (this might be zero)
                return 0.15 * handler.extract(fuel, Mth.ceil(thrustMultiplier), transaction);
            }
        }
        return 0;
    }

    /*TODO - 26.2: check that thse are handled by the item props
    @Override
    public int getDefense() {
        return getMaterial().value().getDefense(getType());
    }

    @Override
    public float getToughness() {
        return getMaterial().value().toughness();
    }*/

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

    private static float getAbsorption(EquipmentSlot armorType) {
        return switch (armorType) {
            case HEAD, FEET -> 0.15F;
            case CHEST -> 0.4F;
            case LEGS -> 0.3F;
            //Based roughly off the defense values of the netherite armor material for body vs the sum of all the ones for a full set of humanoid armor
            case BODY -> 0.95F;
            default -> throw new IllegalArgumentException("Unknown Equipment Slot Armor Type");
        };
    }

    public static float getDamageAbsorbed(LivingEntity entity, DamageSource source, float amount) {
        if (amount <= 0) {
            return 0;
        }
        float ratioAbsorbed = 0;
        List<FoundArmorDetails> armorDetails = new ArrayList<>();
        //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
        try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
            //Start by looping the armor, allowing modules to absorb damage if they can
            for (EquipmentSlot armorType : EquipmentSlotGroup.ARMOR) {
                //Player's don't have non-humanoid armor and the wrapper will throw for them, so skip them
                if (!(entity instanceof Player) || armorType.getType() == Type.HUMANOID_ARMOR) {
                    ResourceHandler<ItemResource> armorSlot = LivingEntityEquipmentWrapper.of(entity, armorType);
                    ItemResource itemType = armorSlot.getResource(0);
                    if (!itemType.isEmpty() && itemType.is(MekanismAPITags.Items.MODULE_CONTAINERS_ARMOR)) {
                        ItemAccess itemAccess = ItemAccess.forHandlerIndexStrict(armorSlot, 0);
                        EnergyHandler energyHandler = AutomatedEnergyHandler.manual(Capabilities.ENERGY.getCapability(itemAccess));
                        if (energyHandler != null) {
                            FoundArmorDetails details = new FoundArmorDetails(energyHandler, getAbsorption(armorType));
                            armorDetails.add(details);
                            for (IModule<?> module : IModuleHelper.INSTANCE.getAllModules(itemType)) {
                                if (module.isEnabled()) {
                                    ModuleDamageAbsorbInfo damageAbsorbInfo = getModuleDamageAbsorbInfo(module, source);
                                    if (damageAbsorbInfo != null) {
                                        float absorption = damageAbsorbInfo.absorptionRatio().getAsFloat();
                                        ratioAbsorbed += absorbDamage(details.energyHandler, amount, absorption, ratioAbsorbed, damageAbsorbInfo.energyCost(), transaction);
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
                        MekaSuitAbsorption absorptionData = IMekanismDataMapTypes.INSTANCE.getMekaSuitAbsorption(entity.registryAccess(), source.typeHolder());
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
                    float absorption = details.armorAbsorption * absorbRatio;
                    ratioAbsorbed += absorbDamage(details.energyHandler, amount, absorption, ratioAbsorbed, MekanismConfig.gear.mekaSuitEnergyUsageDamage, transaction);
                    if (ratioAbsorbed >= 1) {
                        //If we have fully absorbed the damage, stop checking/trying to absorb more
                        break;
                    }
                }
            }
            //Use energy/or enqueue usage for each piece as needed
            transaction.commit();
        }
        return Math.min(ratioAbsorbed, 1);
    }

    @Nullable
    private static <MODULE extends ICustomModule<MODULE>> ModuleDamageAbsorbInfo getModuleDamageAbsorbInfo(IModule<MODULE> module, DamageSource damageSource) {
        return module.getCustomInstance().getDamageAbsorbInfo(module, damageSource);
    }

    private static float absorbDamage(EnergyHandler energyHandler, float amount, float absorption, float currentAbsorbed, IntSupplier energyCost, TransactionContext transaction) {
        //Cap the amount that we can absorb to how much we have left to absorb
        absorption = Math.min(1 - currentAbsorbed, absorption);
        float toAbsorb = amount * absorption;
        if (toAbsorb > 0) {
            int usage = Mth.ceil(energyCost.getAsInt() * toAbsorb);
            if (usage == 0) {
                //No energy is actually needed to absorb the damage, either because of the config
                // or how small the amount to absorb is
                return absorption;
            }
            int energyUsed = energyHandler.extract(usage, transaction);
            if (energyUsed == usage) {
                //If we have more energy available than we need, return that we can absorb it all
                return absorption;
            } else if (energyUsed > 0) {
                //Otherwise, if we have energy available but not as much as needed to fully absorb it
                // then we calculate what ratio we are able to block
                float absorbedPercent = energyUsed / (float) usage;
                return absorption * absorbedPercent;
            }
        }
        return 0;
    }

    private record FoundArmorDetails(EnergyHandler energyHandler, float armorAbsorption) {
    }
}
