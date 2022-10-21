package mekanism.common.item.gear;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import mekanism.api.NBTConstants;
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
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemJetpack extends ItemGasArmor implements IItemHUDProvider, IModeItem, IJetpackItem {

    private static final JetpackMaterial JETPACK_MATERIAL = new JetpackMaterial();

    public ItemJetpack(Properties properties) {
        this(JETPACK_MATERIAL, properties);
    }

    public ItemJetpack(ArmorMaterial material, Properties properties) {
        super(material, EquipmentSlot.CHEST, properties.setNoRepair());
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.jetpack());
    }

    @Override
    protected LongSupplier getMaxGas() {
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
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, getJetpackMode(stack).getTextComponent()));
    }

    @Override
    public boolean canUseJetpack(ItemStack stack) {
        return hasGas(stack);
    }

    @Override
    public JetpackMode getJetpackMode(ItemStack stack) {
        return JetpackMode.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.MODE));
    }

    @Override
    public void useJetpackFuel(ItemStack stack) {
        useGas(stack, 1);
    }

    public void setMode(ItemStack stack, JetpackMode mode) {
        ItemDataUtils.setInt(stack, NBTConstants.MODE, mode.ordinal());
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        if (slotType == getSlot()) {
            ItemJetpack jetpack = (ItemJetpack) stack.getItem();
            list.add(MekanismLang.JETPACK_MODE.translateColored(EnumColor.DARK_GRAY, jetpack.getJetpackMode(stack)));
            GasStack stored = GasStack.EMPTY;
            Optional<IGasHandler> capability = stack.getCapability(Capabilities.GAS_HANDLER).resolve();
            if (capability.isPresent()) {
                IGasHandler gasHandlerItem = capability.get();
                if (gasHandlerItem.getTanks() > 0) {
                    stored = gasHandlerItem.getChemicalInTank(0);
                }
            }
            list.add(MekanismLang.JETPACK_STORED.translateColored(EnumColor.DARK_GRAY, EnumColor.ORANGE, stored.getAmount()));
        }
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, boolean displayChangeMessage) {
        JetpackMode mode = getJetpackMode(stack);
        JetpackMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, newMode);
            if (displayChangeMessage) {
                player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.JETPACK_MODE_CHANGE.translate(newMode)));
            }
        }
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        return slotType == getSlot();
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
