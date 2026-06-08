package mekanism.common.item.gear;

import java.util.List;
import java.util.function.Consumer;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IFreeRunnerItem;
import mekanism.common.item.interfaces.IFreeRunnerItem.FreeRunnerMode;
import mekanism.common.item.interfaces.IHasConditionalAttributes;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismArmorMaterials;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

public class ItemFreeRunners extends ItemSpecialArmor implements IItemHUDProvider, ICustomCreativeTabContents, IAttachmentBasedModeItem<FreeRunnerMode>,
      IHasConditionalAttributes, IFreeRunnerItem {

    private static final AttributeModifier MOVEMENT_EFFICIENCY = new AttributeModifier(Mekanism.rl("free_runners"), 1, Operation.ADD_VALUE);

    public ItemFreeRunners(Item.Properties properties) {
        this(MekanismArmorMaterials.FREE_RUNNERS, properties);
    }

    public ItemFreeRunners(ArmorMaterial material, Item.Properties properties) {
        super(material, ArmorType.BOOTS, properties.rarity(Rarity.RARE).setNoCombineRepair().stacksTo(1)
              .component(MekanismDataComponents.FREE_RUNNER_MODE, FreeRunnerMode.NORMAL)
        );
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        StorageUtils.addStoredEnergy(ItemAccessUtils.sideEffectFreeAccess(stack), tooltipAdder, true);
        tooltipAdder.accept(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));
    }

    @Override
    public void addItems(Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(ContainerType.ENERGY.getFilledVariant(item, null));
    }

    @Override
    public boolean canWalkOnPowderedSnow(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return true;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return StorageUtils.isEnergyBarVisible(stack);
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
    public DataComponentType<FreeRunnerMode> getModeDataType() {
        return MekanismDataComponents.FREE_RUNNER_MODE.get();
    }

    @Override
    public FreeRunnerMode getDefaultMode() {
        return FreeRunnerMode.NORMAL;
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> FreeRunnerMode getFreeRunnerMode(ITEM instance) {
        return getMode(instance);
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDStrings(List<Component> list, Player player, ITEM instance, EquipmentSlot slotType) {
        if (slotType == EquipmentSlot.FEET) {
            list.add(MekanismLang.FREE_RUNNERS_MODE.translateColored(EnumColor.GRAY, getMode(instance).getTextComponent()));
            StorageUtils.addStoredEnergy(ItemAccessUtils.sideEffectFreeAccess(instance), list::add, true, MekanismLang.FREE_RUNNERS_STORED);
        }
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemAccess itemAccess, int shift, DisplayChange displayChange, TransactionContext transaction) {
        FreeRunnerMode mode = getMode(itemAccess);
        FreeRunnerMode newMode = mode.adjust(shift);
        if (mode != newMode && setMode(itemAccess, player, newMode, transaction)) {
            displayChange.sendMessage(player, newMode, MekanismLang.FREE_RUNNER_MODE_CHANGE::translate);
        }
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean supportsSlotType(ITEM instance, @NotNull EquipmentSlot slotType) {
        return slotType == EquipmentSlot.FEET;
    }

    @Override
    public void adjustAttributes(ItemAttributeModifierEvent event) {
        if (getMode(event.getItemStack()) == FreeRunnerMode.NORMAL) {
            event.addModifier(Attributes.MOVEMENT_EFFICIENCY, MOVEMENT_EFFICIENCY, EquipmentSlotGroup.FEET);
        }
    }
}