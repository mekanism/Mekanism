package mekanism.common.item.gear;

import java.util.List;
import java.util.Locale;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.AutomatedResourceHandler;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.registries.MekanismArmorMaterials;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ItemJetpack extends ItemChemicalArmor implements IItemHUDProvider, IJetpackItem, IAttachmentBasedModeItem<JetpackMode> {

    public ItemJetpack(Item.Properties properties) {
        this(MekanismArmorMaterials.JETPACK, properties);
    }

    public ItemJetpack(ArmorMaterial material, Item.Properties properties) {
        super(material, ArmorType.CHESTPLATE, properties.component(MekanismDataComponents.JETPACK_MODE, JetpackMode.NORMAL));
    }

    @Override
    public ResourceKey<Chemical> getChemicalType() {
        return ChemicalIds.HYDROGEN;
    }

    @Override
    public boolean canUseJetpack(ItemAccess itemAccess) {
        return ChemicalUtils.hasChemicalOfType(itemAccess, getChemicalType());
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
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> JetpackMode getJetpackMode(ITEM instance) {
        return getMode(instance);
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> double useJetpackFuel(RegistryAccess registryAccess, ItemAccess itemAccess, ITEM primaryInstance, TransactionContext transaction) {
        ChemicalResource fuel = ChemicalUtils.getResource(registryAccess, getChemicalType());
        if (fuel.isEmpty()) {
            return 0;
        }
        ResourceHandler<ChemicalResource> chemicalHandler = AutomatedResourceHandler.manual(Capabilities.CHEMICAL.getCapability(itemAccess));
        if (chemicalHandler == null) {
            return 0;
        }
        return 0.15 * chemicalHandler.extract(fuel, 1, transaction);
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDStrings(List<Component> list, Player player, ITEM instance, EquipmentSlot slotType) {
        if (slotType == EquipmentSlot.CHEST) {
            list.add(MekanismLang.JETPACK_MODE.translateColored(EnumColor.DARK_GRAY, getMode(instance)));
            long stored = 0;
            long capacity = 1;
            ResourceHandler<ChemicalResource> handler = Capabilities.CHEMICAL.getCapability(ItemAccessUtils.sideEffectFreeAccess(instance));
            if (handler != null && handler.size() > 0) {
                stored = handler.getAmountAsLong(0);
                capacity = handler.getCapacityAsLong(0, handler.getResource(0));
            }
            list.add(MekanismLang.JETPACK_STORED.translateColored(EnumColor.DARK_GRAY, EnumColor.ORANGE, stored, String.format(Locale.ROOT, "%.0f", 100.0 * stored / capacity)));
        }
    }

    @Override
    public void changeMode(Player player, ItemAccess itemAccess, int shift, DisplayChange displayChange, TransactionContext transaction) {
        JetpackMode mode = getMode(itemAccess);
        JetpackMode newMode = mode.adjust(shift);
        if (mode != newMode && setMode(itemAccess, player, newMode, transaction)) {
            displayChange.sendMessage(player, newMode, MekanismLang.JETPACK_MODE_CHANGE::translate);
        }
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean supportsSlotType(ITEM instance, EquipmentSlot slotType) {
        return slotType == EquipmentSlot.CHEST;
    }
}
