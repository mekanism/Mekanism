package mekanism.common.item.block;

import java.util.List;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes.AttributeInventory;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.TextUtils;
import mekanism.common.util.text.UpgradeDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class ItemBlockTooltip<BLOCK extends Block & IHasDescription> extends ItemBlockMekanism<BLOCK> {

    private final boolean hasDetails;

    public ItemBlockTooltip(BLOCK block, Item.Properties properties) {
        this(block, false, properties);
    }

    public ItemBlockTooltip(BLOCK block) {
        this(block, true, new Item.Properties().stacksTo(1));
    }

    protected ItemBlockTooltip(BLOCK block, boolean hasDetails, Properties properties) {
        super(block, properties);
        this.hasDetails = hasDetails;
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        //Try to drop the inventory contents if we are a block item that persists our inventory
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    public boolean placeBlock(@NotNull BlockPlaceContext context, @NotNull BlockState state) {
        AttributeHasBounding hasBounding = Attribute.get(state, AttributeHasBounding.class);
        return (hasBounding == null || WorldUtils.areBlocksValidAndReplaceable(context.getLevel(), hasBounding.getPositions(context.getClickedPos(), state))) &&
               super.placeBlock(context, state);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
            tooltip.add(getBlock().getDescription().translate());
        } else if (hasDetails && MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            addDetails(stack, world, tooltip, flag);
        } else {
            addStats(stack, world, tooltip, flag);
            if (hasDetails) {
                tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            }
            tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()));
        }
    }

    protected void addStats(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
    }

    protected void addDetails(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        //Note: Security and owner info gets skipped if the stack doesn't expose them
        IItemSecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);
        addTypeDetails(stack, world, tooltip, flag);
        //TODO: Make this support "multiple" fluid types (and maybe display multiple tanks of the same fluid)
        FluidStack fluidStack = StorageUtils.getStoredFluidFromAttachment(stack);
        if (!fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, TextUtils.format(fluidStack.getAmount())));
        }
        if (Attribute.has(getBlock(), AttributeInventory.class) && ContainerType.ITEM.supports(stack)) {
            tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.hasInventory(stack)));
        }
        if (Attribute.has(getBlock(), AttributeUpgradeSupport.class)) {
            stack.getData(MekanismAttachmentTypes.UPGRADES).getUpgrades().forEach((upgrade, level) -> tooltip.add(UpgradeDisplay.of(upgrade, level).getTextComponent()));
        }
    }

    protected void addTypeDetails(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        //Put this here so that energy cubes can skip rendering energy here
        if (exposesEnergyCap(stack)) {
            StorageUtils.addStoredEnergy(stack, tooltip, false);
        }
    }
}
