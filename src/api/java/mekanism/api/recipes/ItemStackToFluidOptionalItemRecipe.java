package mekanism.api.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe.FluidOptionalItemOutput;
import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for defining ItemStack to fluid recipes with an optional item output.
 * <br>
 * Input: ItemStack
 * <br>
 * Output: FluidStack, Optional ItemStack
 *
 * @apiNote There is currently only one type of ItemStack to FluidStack recipe type:
 * <ul>
 *     <li>Nutritional Liquification: These cannot currently be created, but are processed in the Nutritional Liquifier.</li>
 * </ul>
 * @since 10.6.3
 */
@NothingNullByDefault
public abstract class ItemStackToFluidOptionalItemRecipe extends ItemInputRecipe<FluidOptionalItemOutput> {

    /**
     * @apiNote Fluid must be present, but the item may be empty.
     */
    public record FluidOptionalItemOutput(FluidStackTemplate fluid, @Nullable ItemStackTemplate optionalItem) {

        public static final Codec<FluidOptionalItemOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              FluidStackTemplate.CODEC.fieldOf(SerializationConstants.FLUID).forGetter(FluidOptionalItemOutput::fluid),
              ItemStackTemplate.CODEC.optionalFieldOf(SerializationConstants.ITEM).forGetter(output -> Optional.ofNullable(output.optionalItem))
        ).apply(instance, (fluid, item) -> new FluidOptionalItemOutput(fluid, item.orElse(null))));

        public FluidOptionalItemOutput {
            Objects.requireNonNull(fluid, "Fluid output cannot be null.");
        }
    }
}
