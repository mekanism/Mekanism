package mekanism.common.item.gear;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.providers.IGasProvider;
import mekanism.api.text.EnumColor;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismGases;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemJetpack extends ItemGasArmor implements IItemHUDProvider, IJetpackItem, IAttachmentBasedModeItem<JetpackMode> {

    private static final JetpackMaterial JETPACK_MATERIAL = new JetpackMaterial();

    public ItemJetpack(Properties properties) {
        this(JETPACK_MATERIAL, properties);
    }

    public ItemJetpack(ArmorMaterial material, Properties properties) {
        super(material, ArmorItem.Type.CHESTPLATE, properties.setNoRepair());
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.jetpack());
    }

    @Override
    protected CachedLongValue getMaxGas() {
        return MekanismConfig.gear.jetpackMaxGas;
    }

    @Override
    protected LongSupplier getFillRate() {
        return MekanismConfig.gear.jetpackFillRate;
    }

    @Override
    protected IGasProvider getGasType() {
        return MekanismGases.HYDROGEN;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));
    }

    @Override
    public boolean canUseJetpack(ItemStack stack) {
        return hasGas(stack);
    }

    @Override
    public AttachmentType<JetpackMode> getModeAttachment() {
        return MekanismAttachmentTypes.JETPACK_MODE.get();
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
        useGas(stack, 1);
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        if (slotType == getEquipmentSlot()) {
            ItemJetpack jetpack = (ItemJetpack) stack.getItem();
            list.add(MekanismLang.JETPACK_MODE.translateColored(EnumColor.DARK_GRAY, jetpack.getMode(stack)));
            GasStack stored = GasStack.EMPTY;
            long capacity = 1;
            IGasHandler gasHandlerItem = Capabilities.GAS.getCapability(stack);
            if (gasHandlerItem != null && gasHandlerItem.getTanks() > 0) {
                stored = gasHandlerItem.getChemicalInTank(0);
                capacity = gasHandlerItem.getTankCapacity(0);
            }
            list.add(MekanismLang.JETPACK_STORED.translateColored(EnumColor.DARK_GRAY, EnumColor.ORANGE, stored.getAmount(), String.format("%.0f", 100.0 * stored.getAmount() / capacity)));
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

    @Override
    public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
        if (this instanceof ItemArmoredJetpack) {
            return super.getDefaultTooltipHideFlags(stack);
        }
        return super.getDefaultTooltipHideFlags(stack) | TooltipPart.MODIFIERS.getMask();
    }

    @NothingNullByDefault
    protected static class JetpackMaterial extends BaseSpecialArmorMaterial {

        @Override
        public String getName() {
            return Mekanism.MODID + ":jetpack";
        }
    }
}
