package mekanism.common.item.gear;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.IIncrementalEnum;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IGasProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;

public class ItemJetpack extends ItemGasArmor implements IItemHUDProvider, IModeItem {

    private static final JetpackMaterial JETPACK_MATERIAL = new JetpackMaterial();

    public ItemJetpack(Properties properties) {
        this(JETPACK_MATERIAL, properties);
    }

    public ItemJetpack(ArmorMaterial material, Properties properties) {
        super(material, EquipmentSlot.CHEST, properties.setNoRepair());
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
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
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, getMode(stack).getTextComponent()));
    }

    public JetpackMode getMode(ItemStack stack) {
        return JetpackMode.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.MODE));
    }

    public void setMode(ItemStack stack, JetpackMode mode) {
        ItemDataUtils.setInt(stack, NBTConstants.MODE, mode.ordinal());
    }

    @Override
    public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
        if (slotType == getSlot()) {
            ItemJetpack jetpack = (ItemJetpack) stack.getItem();
            list.add(MekanismLang.JETPACK_MODE.translateColored(EnumColor.DARK_GRAY, jetpack.getMode(stack)));
            GasStack stored = GasStack.EMPTY;
            Optional<IGasHandler> capability = stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).resolve();
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
    public void changeMode(@Nonnull Player player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        JetpackMode mode = getMode(stack);
        JetpackMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            setMode(stack, newMode);
            if (displayChangeMessage) {
                player.sendMessage(MekanismUtils.logFormat(MekanismLang.JETPACK_MODE_CHANGE.translate(newMode)), Util.NIL_UUID);
            }
        }
    }

    @Override
    public boolean supportsSlotType(ItemStack stack, @Nonnull EquipmentSlot slotType) {
        return slotType == getSlot();
    }

    @Override
    public int getDefaultTooltipHideFlags(@Nonnull ItemStack stack) {
        if (!(this instanceof ItemArmoredJetpack)) {
            return super.getDefaultTooltipHideFlags(stack) | TooltipPart.MODIFIERS.getMask();
        }
        return super.getDefaultTooltipHideFlags(stack);
    }

    /**
     * Gets the first found jetpack from an entity, if one is worn.
     * <br>
     * If Curios is loaded, the curio slots will be checked as well.
     *
     * @param entity the entity on which to look for the jetpack
     *
     * @return the jetpack stack if present, otherwise an empty stack
     */
    @Nonnull
    public static ItemStack getJetpack(LivingEntity entity) {
        return getJetpack(entity, entity.getItemBySlot(EquipmentSlot.CHEST));
    }

    @Nonnull
    public static ItemStack getJetpack(LivingEntity entity, ItemStack chest) {
        if (chest.getItem() instanceof ItemJetpack || chest.getItem() instanceof IModuleContainerItem moduleContainer &&
                                                      moduleContainer.supportsModule(chest, MekanismModules.JETPACK_UNIT)) {
            return chest;
        } else if (Mekanism.hooks.CuriosLoaded) {
            return CuriosIntegration.findFirstCurio(entity, s -> s.getItem() instanceof ItemJetpack jetpackItem && jetpackItem.hasGas(s));
        }
        return ItemStack.EMPTY;
    }

    public enum JetpackMode implements IIncrementalEnum<JetpackMode>, IHasTextComponent {
        NORMAL(MekanismLang.JETPACK_NORMAL, EnumColor.DARK_GREEN, MekanismUtils.getResource(ResourceType.GUI_HUD, "jetpack_normal.png")),
        HOVER(MekanismLang.JETPACK_HOVER, EnumColor.DARK_AQUA, MekanismUtils.getResource(ResourceType.GUI_HUD, "jetpack_hover.png")),
        DISABLED(MekanismLang.JETPACK_DISABLED, EnumColor.DARK_RED, MekanismUtils.getResource(ResourceType.GUI_HUD, "jetpack_off.png"));

        private static final JetpackMode[] MODES = values();
        private final ILangEntry langEntry;
        private final EnumColor color;
        private final ResourceLocation hudIcon;

        JetpackMode(ILangEntry langEntry, EnumColor color, ResourceLocation hudIcon) {
            this.langEntry = langEntry;
            this.color = color;
            this.hudIcon = hudIcon;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Nonnull
        @Override
        public JetpackMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public ResourceLocation getHUDIcon() {
            return hudIcon;
        }

        public static JetpackMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
        }
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class JetpackMaterial extends BaseSpecialArmorMaterial {

        @Override
        public String getName() {
            return Mekanism.MODID + ":jetpack";
        }
    }
}