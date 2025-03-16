package mekanism.common.item.gear;

import java.util.List;
import java.util.Locale;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.registries.MekanismArmorMaterials;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

public class ItemJetpack extends ItemChemicalArmor implements IItemHUDProvider, IJetpackItem, IAttachmentBasedModeItem<JetpackMode> {

    public ItemJetpack(Properties properties) {
        this(MekanismArmorMaterials.JETPACK, properties);
    }

    public ItemJetpack(Holder<ArmorMaterial> material, Properties properties) {
        super(material, ArmorItem.Type.CHESTPLATE, properties.setNoRepair().component(MekanismDataComponents.JETPACK_MODE, JetpackMode.NORMAL));
    }

    @Override
    protected Holder<Chemical> getChemicalType() {
        return MekanismChemicals.HYDROGEN;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));
    }

    @Override
    public boolean canUseJetpack(ItemStack stack) {
        return hasChemical(stack);
    }

    @Override
    public DataComponentType<JetpackMode> getModeDataType() {
        return MekanismDataComponents.JETPACK_MODE.get();
    }

    @Override
    public JetpackMode getDefaultMode() {
        return JetpackMode.NORMAL;
    }

    @Override
    public JetpackMode getJetpackMode(ItemStack stack) {
        return getMode(stack);
    }

    @Override
    public double getJetpackThrust(ItemStack stack) {
        return 0.15;
    }

    @Override
    public void useJetpackFuel(ItemStack stack) {
        useChemical(stack, 1);
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        if (slotType == getEquipmentSlot()) {
            ItemJetpack jetpack = (ItemJetpack) stack.getItem();
            list.add(MekanismLang.JETPACK_MODE.translateColored(EnumColor.DARK_GRAY, jetpack.getMode(stack)));
            ChemicalStack stored = ChemicalStack.EMPTY;
            long capacity = 1;
            IChemicalHandler gasHandlerItem = Capabilities.CHEMICAL.getCapability(stack);
            if (gasHandlerItem != null && gasHandlerItem.getChemicalTanks() > 0) {
                stored = gasHandlerItem.getChemicalInTank(0);
                capacity = gasHandlerItem.getChemicalTankCapacity(0);
            }
            list.add(MekanismLang.JETPACK_STORED.translateColored(EnumColor.DARK_GRAY, EnumColor.ORANGE, stored.getAmount(), String.format(Locale.ROOT, "%.0f", 100.0 * stored.getAmount() / capacity)));
        }
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        JetpackMode mode = getMode(stack);
        JetpackMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, player, newMode);
            displayChange.sendMessage(player, newMode, MekanismLang.JETPACK_MODE_CHANGE::translate);
        }
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        return slotType == getEquipmentSlot();
    }
}
