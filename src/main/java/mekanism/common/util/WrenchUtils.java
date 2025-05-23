package mekanism.common.util;

import java.util.Objects;
import mekanism.api.IConfigurable;
import mekanism.api.IConfigurable.ConfigureActions;
import mekanism.api.IConfigurable.ConfigureContext;
import mekanism.api.RelativeSide;
import mekanism.api.WrenchResult;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ITileRadioactive;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public final class WrenchUtils {

    private WrenchUtils() {
    }

    public static boolean checkType(ConfigureContext context, TransmissionType type) {
        return context.toolItem() instanceof ItemConfigurator item && item.getMode(context.toolStack()).getTransmission() == type;
    }

    private static WrenchResult configure(Player player, Level world, BlockPos pos, Direction side, BlockEntity tile, ItemStack stack, @NotNull TransmissionType type) {
        //This mumbo jumbo handles side I/O configuration (based on TransmissionType)
        if (tile instanceof ISideConfiguration config && config.getConfig().supports(type)) {
            ConfigInfo info = config.getConfig().getConfig(type);
            if (info != null) {
                RelativeSide relativeSide = RelativeSide.fromDirections(config.getDirection(), side);
                DataType dataType = info.getDataType(relativeSide);
                if (!player.isShiftKeyDown()) {
                    player.displayClientMessage(MekanismLang.CONFIGURATOR_VIEW_MODE.translateColored(EnumColor.GRAY, type, dataType.getColor(),
                          dataType, dataType.getColor().getColoredName()), true);
                } else {
                    DataType old = dataType;
                    dataType = info.incrementDataType(relativeSide);
                    if (dataType != old) {
                        player.displayClientMessage(MekanismLang.CONFIGURATOR_TOGGLE_MODE.translateColored(EnumColor.GRAY, type, dataType.getColor(),
                              dataType, dataType.getColor().getColoredName()), true);
                        config.getConfig().sideChanged(type, relativeSide);
                    }
                }
            }
            return WrenchResult.CONFIGURED;
        }
        //This is a more general configuration, handled specifically by the block via an exposed capability
        IConfigurable config = WorldUtils.getCapability(world, Capabilities.CONFIGURABLE, pos, null, tile, side);
        if (config != null) {
            return config.onConfigure(new ConfigureContext(ConfigureActions.PROBE_OR_COLOR, player, side, stack));
        }
        return WrenchResult.PASS;
    }

    private static WrenchResult empty(Player player, Level world, BlockPos pos, Direction side, BlockEntity tile) {
        if (tile instanceof IMekanismInventory inv && inv.hasInventory()) {
            if (tile instanceof TileEntityBin bin && bin.getTier() == BinTier.CREATIVE) {
                //If the tile is a creative bin only allow clearing it if the player is in creative
                // and don't bother popping the stack out
                if (player.isCreative()) {
                    bin.getBinSlot().setEmpty();
                    return WrenchResult.EMPTIED;
                }
                return WrenchResult.NOT_ALLOWED;
            }
            //TODO: Switch this to items being handled by TileEntityMekanism, energy handled here (via lambdas?)
            for (IInventorySlot inventorySlot : inv.getInventorySlots(null)) {
                if (!inventorySlot.isEmpty()) {
                    InventoryUtils.dropStack(world, pos, side, inventorySlot.getStack().copy(), Block::popResourceFromFace);
                    inventorySlot.setEmpty();
                }
            }
            return WrenchResult.EMPTIED;
        }
        return WrenchResult.PASS;
    }

    private static WrenchResult rotate(Player player, Direction side, BlockEntity tile) {
        if (tile instanceof TileEntityMekanism tileMekanism) {
            if (!tileMekanism.isDirectional()) {
                return WrenchResult.PASS;
            } else if (Attribute.matches(tileMekanism.getBlockHolder(), AttributeStateFacing.class, AttributeStateFacing::canRotate)) {
                tileMekanism.setFacing(player.isShiftKeyDown() ? side.getOpposite() : side);
            }
        }
        return WrenchResult.ROTATED;
    }

    public static WrenchResult useConfigurator(Player player, Level world, BlockPos pos, Direction side, ItemStack stack, ConfiguratorMode mode) {
        if (!world.isClientSide && player != null) {
            final BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                return WrenchResult.NOT_ALLOWED;
            } else if (mode.configurating) {
                final TransmissionType type = Objects.requireNonNull(mode.getTransmission(), "Configurating state requires transmission type");
                return WrenchUtils.configure(player, world, pos, side, tile, stack, type);
            } else if (mode == ConfiguratorMode.EMPTY) {
                return WrenchUtils.empty(player, world, pos, side, tile);
            } else if (mode == ConfiguratorMode.ROTATE) {
                return WrenchUtils.rotate(player, side, tile);
            }
        }
        return WrenchResult.PASS;
    }

    public static WrenchResult useConfigurator(UseOnContext context, ItemConfigurator item) {
        final ItemStack stack = context.getItemInHand();
        final ConfiguratorMode mode = item.getMode(context.getItemInHand());
        return useConfigurator(context.getPlayer(), context.getLevel(), context.getClickedPos(), context.getClickedFace(), stack, mode);
    }

    public static ItemInteractionResult useWrench(Player player, Level world, BlockPos pos, Direction side, ItemStack stack, BlockState state, InteractionHand hand) {
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        //TODO: Implement an IWrenchable capability?
        //Use the intended capability if it is present
        /*IWrenchable capability = WorldUtils.getCapability(world, Capabilities.WRENCHABLE, pos, side);
        if (capability != null) {
            return capability.tryWrench(state, player, stack).getInteractionResult();
        }*/
        //Handle Configurator specially
        if (stack.getItem() instanceof ItemConfigurator item && item.getMode(stack) == ConfiguratorMode.WRENCH && !player.isShiftKeyDown()) {
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                return WrenchResult.NOT_ALLOWED.getItemInteractionResult();
            }
            IConfigurable config = WorldUtils.getCapability(world, Capabilities.CONFIGURABLE, pos, null, tile, side);
            if (config != null) {
                return config.onConfigure(new ConfigureContext(ConfigureActions.PLUMB_ONLY, player, side, stack)).getItemInteractionResult();
            }
        }
        //Fallback to TileEntityMekanism
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            //No tile, so we skip doing the use-without-item
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        } else if (world.isClientSide) {
            return genericClientActivated(state.getBlock(), stack, tile);
        }
        return tile.tryWrench(state, player, stack, hand).getItemInteractionResult();
    }

    private static ItemInteractionResult genericClientActivated(Block block, ItemStack stack, BlockEntity blockEntity) {
        if (!Attribute.has(block, AttributeGui.class) && MekanismUtils.canUseAsWrench(stack)) {
            if (blockEntity instanceof ITileRadioactive tileRadioactive && tileRadioactive.getRadiationScale() > 0) {
                return ItemInteractionResult.FAIL;
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
