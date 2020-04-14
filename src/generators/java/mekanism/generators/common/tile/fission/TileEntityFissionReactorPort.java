package mekanism.generators.common.tile.fission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigurable;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode;
import mekanism.generators.common.block.attribute.AttributeStateFissionPortMode.FissionPortMode;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityFissionReactorPort extends TileEntityFissionReactorCasing implements IConfigurable {

    public TileEntityFissionReactorPort() {
        super(GeneratorsBlocks.FISSION_REACTOR_PORT);
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!isRemote()) {
            BlockState state = getBlockState();
            FissionPortMode mode = state.get(AttributeStateFissionPortMode.modeProperty);
            mode = FissionPortMode.values()[(mode.ordinal() + 1) % FissionPortMode.values().length];
            world.setBlockState(pos, state.with(AttributeStateFissionPortMode.modeProperty, mode));
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                  GeneratorsLang.FISSION_PORT_MODE_CHANGE.translateColored(EnumColor.GRAY, mode.translate())));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }
}
