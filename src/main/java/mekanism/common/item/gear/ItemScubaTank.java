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
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemScubaTank extends ItemGasArmor implements IItemHUDProvider, IAttachmentBasedModeItem<Boolean> {

    private static final ScubaTankMaterial SCUBA_TANK_MATERIAL = new ScubaTankMaterial();

    public ItemScubaTank(Properties properties) {
        super(SCUBA_TANK_MATERIAL, ArmorItem.Type.CHESTPLATE, properties);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.scubaTank());
    }

    @Override
    protected CachedLongValue getMaxGas() {
        return MekanismConfig.gear.scubaMaxGas;
    }

    @Override
    protected LongSupplier getFillRate() {
        return MekanismConfig.gear.scubaFillRate;
    }

    @Override
    protected IGasProvider getGasType() {
        return MekanismGases.OXYGEN;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.FLOWING.translateColored(EnumColor.GRAY, YesNo.of(getMode(stack), true)));
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        if (slotType == getEquipmentSlot()) {
            ItemScubaTank scubaTank = (ItemScubaTank) stack.getItem();
            list.add(MekanismLang.SCUBA_TANK_MODE.translateColored(EnumColor.DARK_GRAY, OnOff.of(scubaTank.getMode(stack), true)));
            GasStack stored = GasStack.EMPTY;
            IGasHandler gasHandlerItem = Capabilities.GAS.getCapability(stack);
            if (gasHandlerItem != null && gasHandlerItem.getTanks() > 0) {
                stored = gasHandlerItem.getChemicalInTank(0);
            }
            list.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.DARK_GRAY, MekanismGases.OXYGEN, EnumColor.ORANGE, stored.getAmount()));
        }
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        if (Math.abs(shift) % 2 == 1) {
            //We are changing by an odd amount, so toggle the mode
            boolean newState = !getMode(stack);
            setMode(stack, player, newState);
            displayChange.sendMessage(player, newState, s -> MekanismLang.FLOWING.translate(OnOff.of(s, true)));
        }
    }

    @Override
    public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
        return super.getDefaultTooltipHideFlags(stack) | TooltipPart.MODIFIERS.getMask();
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
        return slotType == getEquipmentSlot();
    }

    @Override
    public AttachmentType<Boolean> getModeAttachment() {
        return MekanismAttachmentTypes.SCUBA_TANK_MODE.get();
    }

    @NothingNullByDefault
    protected static class ScubaTankMaterial extends BaseSpecialArmorMaterial {

        @Override
        public String getName() {
            return Mekanism.MODID + ":scuba_tank";
        }
    }
}