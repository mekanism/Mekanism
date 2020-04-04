package mekanism.common.item.gear;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import com.google.common.collect.Multimap;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.math.FloatingLong;
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
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemMekaSuitArmor extends ArmorItem implements IModuleContainerItem, IModeItem, IItemHUDProvider/*, ISpecialGear*/ {

    private static final MekaSuitMaterial MEKASUIT_MATERIAL = new MekaSuitMaterial();
    private static final int GAS_TRANSFER_RATE = 256;

    private Set<GasTankSpec> gasTankSpecs = new HashSet<>();

    public ItemMekaSuitArmor(EquipmentSlotType slot, Properties properties) {
        super(MEKASUIT_MATERIAL, slot, properties.setNoRepair().maxStackSize(1));
        Modules.setSupported(this, Modules.ENERGY_UNIT, Modules.RADIATION_SHIELDING_UNIT);

        if (slot == EquipmentSlotType.HEAD) {
            Modules.setSupported(this, Modules.ELECTROLYTIC_BREATHING_UNIT, Modules.INHALATION_PURIFICATION_UNIT, Modules.VISION_ENHANCEMENT_UNIT);
        } else if (slot == EquipmentSlotType.CHEST) {
            Modules.setSupported(this, Modules.JETPACK_UNIT, Modules.GRAVITATIONAL_MODULATING_UNIT, Modules.CHARGE_DISTRIBUTION_UNIT);
            gasTankSpecs.add(GasTankSpec.createFillOnly(GAS_TRANSFER_RATE, () -> 24_000, gas -> gas == MekanismGases.HYDROGEN.get()));
        } else if (slot == EquipmentSlotType.LEGS) {
            Modules.setSupported(this, Modules.LOCOMOTIVE_BOOSTING_UNIT);
        } else if (slot == EquipmentSlotType.FEET) {
            Modules.setSupported(this, Modules.HYDRAULIC_ABSORPTION_UNIT, Modules.HYDRAULIC_PROPULSION_UNIT);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.detailsKey)) {
            for (Module module : Modules.loadAll(stack)) {
                ITextComponent component = module.getData().getLangEntry().translateColored(EnumColor.GRAY);
                if (module.getInstalledCount() > 1) {
                    component.appendSibling(MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.GRAY, "", Integer.toString(module.getInstalledCount())));
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
    // TODO adjust values based on energy
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(equipmentSlot);
        if (equipmentSlot == slot) {
            multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor modifier", this.damageReduceAmount, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor toughness", this.toughness, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
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
        ItemCapabilityWrapper wrapper = new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(() -> getMaxEnergy(stack), BasicEnergyContainer.notExternal, BasicEnergyContainer.alwaysTrue),
            RadiationShieldingHandler.create(item -> isModuleEnabled(item, Modules.RADIATION_SHIELDING_UNIT) ? ItemHazmatSuitArmor.getShieldingByArmor(slot) : 0));
        if (!gasTankSpecs.isEmpty()) {
            wrapper.add(RateLimitMultiTankGasHandler.create(gasTankSpecs));
        }
        return wrapper;
    }

    @Nonnull
    public GasStack useGas(ItemStack stack, Gas type, int amount) {
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
        return module != null ? module.getEnergyCapacity() : MekanismConfig.general.mekaToolBaseEnergyCapacity.get();
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class MekaSuitMaterial implements IArmorMaterial {

        @Override
        public int getDurability(EquipmentSlotType slotType) {
            return 0;
        }

        @Override
        public int getDamageReductionAmount(EquipmentSlotType slotType) {
            return 0;
        }

        @Override
        public int getEnchantability() {
            return 0;
        }

        @Override
        public SoundEvent getSoundEvent() {
            return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
        }

        @Override
        public Ingredient getRepairMaterial() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return "mekasuit";
        }

        @Override
        public float getToughness() {
            return 0;
        }
    }
}
