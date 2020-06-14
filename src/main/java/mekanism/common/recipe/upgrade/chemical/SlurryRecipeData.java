package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryHandler.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SlurryRecipeData extends ChemicalRecipeData<Slurry, SlurryStack, ISlurryTank, ISlurryHandler> {

    public SlurryRecipeData(ListNBT tanks) {
        super(tanks);
    }

    private SlurryRecipeData(List<ISlurryTank> tanks) {
        super(tanks);
    }

    @Override
    protected SlurryRecipeData create(List<ISlurryTank> tanks) {
        return new SlurryRecipeData(tanks);
    }

    @Override
    protected SubstanceType getSubstanceType() {
        return SubstanceType.SLURRY;
    }

    @Override
    protected ChemicalTankBuilder<Slurry, SlurryStack, ISlurryTank> getTankBuilder() {
        return ChemicalTankBuilder.SLURRY;
    }

    @Override
    protected ISlurryHandler getOutputHandler(List<ISlurryTank> tanks) {
        return new IMekanismSlurryHandler() {
            @Nonnull
            @Override
            public List<ISlurryTank> getChemicalTanks(@Nullable Direction side) {
                return tanks;
            }

            @Override
            public void onContentsChanged() {
            }
        };
    }

    @Override
    protected Capability<ISlurryHandler> getCapability() {
        return Capabilities.SLURRY_HANDLER_CAPABILITY;
    }

    @Override
    protected Predicate<Slurry> cloneValidator(ISlurryHandler handler, int tank) {
        return type -> handler.isValid(tank, new SlurryStack(type, 1));
    }

    @Override
    protected ISlurryHandler getHandlerFromTile(TileEntityMekanism tile) {
        return tile.getSlurryManager().getInternal();
    }
}