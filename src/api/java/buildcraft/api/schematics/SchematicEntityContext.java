package buildcraft.api.schematics;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SchematicEntityContext {
    @Nonnull
    public final World world;
    @Nonnull
    public final BlockPos basePos;
    @Nonnull
    public final Entity entity;

    public SchematicEntityContext(@Nonnull World world,
                                  @Nonnull BlockPos basePos,
                                  @Nonnull Entity entity) {
        this.world = world;
        this.basePos = basePos;
        this.entity = entity;
    }
}
