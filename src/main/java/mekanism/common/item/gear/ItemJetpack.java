package mekanism.common.item.gear;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
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
import mekanism.common.util.ChemicalUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class ItemJetpack extends ItemChemicalArmor implements IItemHUDProvider, IJetpackItem, IAttachmentBasedModeItem<JetpackMode> {

    public ItemJetpack(Item.Properties properties) {
        this(MekanismArmorMaterials.JETPACK, properties);
    }

    public ItemJetpack(ArmorMaterial material, Item.Properties properties) {
        super(material, ArmorType.CHESTPLATE, properties.component(MekanismDataComponents.JETPACK_MODE, JetpackMode.NORMAL));
    }

    @Override
    protected Holder<Chemical> getChemicalType() {
        return MekanismChemicals.HYDROGEN;
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));
    }

    @Override
    public boolean canUseJetpack(ItemStack stack) {
        return ChemicalUtil.hasChemicalOfType(stack, getChemicalType());
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
        ResourceHandler<ChemicalResource> chemicalHandler = Capabilities.CHEMICAL.getCapability(ItemAccess.forStack(stack));//TODO - 26.1 check this Access works
        if (chemicalHandler != null) {
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = chemicalHandler.extract(ChemicalResource.of(getChemicalType()), 1, transaction);
                if (extracted == 1) {
                    transaction.commit();
                }
            }
        }
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        if (slotType == EquipmentSlot.CHEST) {
            ItemJetpack jetpack = (ItemJetpack) stack.getItem();
            list.add(MekanismLang.JETPACK_MODE.translateColored(EnumColor.DARK_GRAY, jetpack.getMode(stack)));
            long stored = 0;
            long capacity = 1;
            ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(ItemAccess.forStack(stack));
            if (handler != null && handler.size() > 0) {
                stored = handler.getAmountAsLong(0);
                capacity = handler.getCapacityAsLong(0, handler.getResource(0));
            }
            list.add(MekanismLang.JETPACK_STORED.translateColored(EnumColor.DARK_GRAY, EnumColor.ORANGE, stored, String.format(Locale.ROOT, "%.0f", 100.0 * stored / capacity)));
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
        return slotType == EquipmentSlot.CHEST;
    }
}
