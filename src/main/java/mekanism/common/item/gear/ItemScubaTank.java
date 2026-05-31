package mekanism.common.item.gear;

import java.util.List;
import java.util.function.Consumer;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.registries.MekanismArmorMaterials;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

public class ItemScubaTank extends ItemChemicalArmor implements IItemHUDProvider, IAttachmentBasedModeItem<Boolean> {

    public ItemScubaTank(Item.Properties properties) {
        super(MekanismArmorMaterials.SCUBA_GEAR, ArmorType.CHESTPLATE, properties.component(MekanismDataComponents.SCUBA_TANK_MODE, false));
    }

    @Override
    protected Holder<Chemical> getChemicalType() {
        return MekanismChemicals.OXYGEN;
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(MekanismLang.FLOWING.translateColored(EnumColor.GRAY, YesNo.of(getMode(stack), true)));
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDStrings(List<Component> list, Player player, ITEM instance, EquipmentSlot slotType) {
        if (slotType == EquipmentSlot.CHEST) {
            list.add(MekanismLang.SCUBA_TANK_MODE.translateColored(EnumColor.DARK_GRAY, OnOff.of(getMode(instance), true)));
            long stored = 0;
            ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(ItemAccessUtils.queryOnlyAccess(instance));
            if (handler != null && handler.size() > 0) {
                stored = handler.getAmountAsLong(0);
            }
            list.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.DARK_GRAY, MekanismChemicals.OXYGEN, EnumColor.ORANGE, stored));
        }
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemAccess itemAccess, int shift, DisplayChange displayChange, TransactionContext transaction) {
        if (Math.abs(shift) % 2 == 1) {
            //We are changing by an odd amount, so toggle the mode
            boolean newState = !getMode(itemAccess);
            if (setMode(itemAccess, player, newState, transaction)) {
                displayChange.sendMessage(player, newState, s -> MekanismLang.FLOWING.translate(OnOff.of(s, true)));
            }
        }
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean supportsSlotType(ITEM instance, @NotNull EquipmentSlot slotType) {
        return slotType == EquipmentSlot.CHEST;
    }

    @Override
    public DataComponentType<Boolean> getModeDataType() {
        return MekanismDataComponents.SCUBA_TANK_MODE.get();
    }

    @Override
    public Boolean getDefaultMode() {
        return Boolean.FALSE;
    }
}