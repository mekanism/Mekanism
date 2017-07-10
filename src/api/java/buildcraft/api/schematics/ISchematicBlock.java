package buildcraft.api.schematics;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.InvalidInputDataException;

public interface ISchematicBlock<S extends ISchematicBlock<S>> {
    void init(SchematicBlockContext context);

    boolean isAir();

    @Nonnull
    Set<BlockPos> getRequiredBlockOffsets();

    @Nonnull
    List<ItemStack> computeRequiredItems(SchematicBlockContext context);

    @Nonnull
    List<FluidStack> computeRequiredFluids(SchematicBlockContext context);

    S getRotated(Rotation rotation);

    boolean canBuild(World world, BlockPos blockPos);

    boolean build(World world, BlockPos blockPos);

    boolean buildWithoutChecks(World world, BlockPos blockPos);

    boolean isBuilt(World world, BlockPos blockPos);

    NBTTagCompound serializeNBT();

    /** @throws InvalidInputDataException If the input data wasn't correct or didn't make sense. */
    void deserializeNBT(NBTTagCompound nbt) throws InvalidInputDataException;
}
