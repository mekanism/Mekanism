package mekanism.common.util;

import java.util.Objects;
import mekanism.api.IConfigurable;
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
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class WrenchUtils {

    private WrenchUtils() {
    }

    public static WrenchResult configure(Player player, Level world, BlockPos pos, Direction side, BlockEntity tile, ConfiguratorMode mode) {
        TransmissionType transmissionType = Objects.requireNonNull(mode.getTransmission(), "Configurating state requires transmission type");
        if (tile instanceof ISideConfiguration config && config.getConfig().supports(transmissionType)) {
            ConfigInfo info = config.getConfig().getConfig(transmissionType);
            if (info != null) {
                RelativeSide relativeSide = RelativeSide.fromDirections(config.getDirection(), side);
                DataType dataType = info.getDataType(relativeSide);
                if (!player.isShiftKeyDown()) {
                    player.displayClientMessage(MekanismLang.CONFIGURATOR_VIEW_MODE.translateColored(EnumColor.GRAY, transmissionType, dataType.getColor(),
                          dataType, dataType.getColor().getColoredName()), true);
                } else if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                    return WrenchResult.NOT_ALLOWED;
                } else {
                    DataType old = dataType;
                    dataType = info.incrementDataType(relativeSide);
                    if (dataType != old) {
                        player.displayClientMessage(MekanismLang.CONFIGURATOR_TOGGLE_MODE.translateColored(EnumColor.GRAY, transmissionType, dataType.getColor(),
                              dataType, dataType.getColor().getColoredName()), true);
                        config.getConfig().sideChanged(transmissionType, relativeSide);
                    }
                }
            }
            return WrenchResult.CONFIGURED;
        }
        if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
            return WrenchResult.NOT_ALLOWED;
        }
        IConfigurable config = WorldUtils.getCapability(world, Capabilities.CONFIGURABLE, pos, null, tile, side);
        if (config != null) {
            if (player.isShiftKeyDown()) {
                return config.onSneakRightClick(player);
            }
            return config.onRightClick(player);
        }
        return WrenchResult.PASS;
    }

    public static WrenchResult empty(Player player, Level world, BlockPos pos, Direction side, BlockEntity tile) {
        if (tile instanceof IMekanismInventory inv && inv.hasInventory()) {
            if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                return WrenchResult.NOT_ALLOWED;
            }
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

    public static WrenchResult rotate(Player player, Level world, BlockPos pos, Direction side, BlockEntity tile) {
        if (tile instanceof TileEntityMekanism tileMekanism) {
            if (!tileMekanism.isDirectional()) {
                return WrenchResult.PASS;
            } else if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                return WrenchResult.NOT_ALLOWED;
            } else if (Attribute.matches(tileMekanism.getBlockHolder(), AttributeStateFacing.class, AttributeStateFacing::canRotate)) {
                tileMekanism.setFacing(player.isShiftKeyDown() ? side.getOpposite() : side);
            }
        }
        return WrenchResult.ROTATED;
    }

    public static WrenchResult useConfigurator(Player player, Level world, BlockPos pos, Direction side, ConfiguratorMode mode) {
        if (!world.isClientSide && player != null) {
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (mode.isConfigurating()) {
                return WrenchUtils.configure(player, world, pos, side, tile, mode);
            } else if (mode == ConfiguratorMode.EMPTY) {
                return WrenchUtils.empty(player, world, pos, side, tile);
            } else if (mode == ConfiguratorMode.ROTATE) {
                return WrenchUtils.rotate(player, world, pos, side, tile);
            }
        }
        return WrenchResult.PASS;
    }

    public static ItemInteractionResult useWrench(Player player, Level world, BlockPos pos, ItemStack stack, BlockState state) {
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        //Use the intended capability if it is present
        /*IWrenchable capability = WorldUtils.getCapability(world, Capabilities.WRENCHABLE, pos, side);
        if (capability != null) {
            return capability.tryWrench(state, player, stack).getInteractionResult();
        }*/
        //Fallback to TileEntityMekanism
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            //No tile, so we skip doing the use-without-item
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        } else if (world.isClientSide) {
            return genericClientActivated(state.getBlock(), stack, tile);
        }
        return tile.tryWrench(state, player, stack).getItemInteractionResult();
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
