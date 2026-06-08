package mekanism.client.model.props;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import mekanism.common.component.FormulaComponent;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record CraftingFormulaStatus() implements SelectItemModelProperty<CraftingFormulaStatus.CraftingCardStatus> {

    public static final Type<CraftingFormulaStatus, CraftingCardStatus> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit(new CraftingFormulaStatus()), CraftingCardStatus.CODEC);

    @Nullable
    @Override
    public CraftingCardStatus get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        FormulaComponent attachment = stack.get(MekanismDataComponents.FORMULA_HOLDER);
        if (attachment == null) {
            return null;
        }
        if (attachment.hasItems()) {
            if (attachment.invalid()) {
                return CraftingCardStatus.INVALID;
            }
            return CraftingCardStatus.ENCODED;
        }
        return null;
    }

    @Override
    public Codec<CraftingCardStatus> valueCodec() {
        return CraftingCardStatus.CODEC;
    }

    @Override
    public Type<CraftingFormulaStatus, CraftingCardStatus> type() {
        return TYPE;
    }

    public enum CraftingCardStatus implements StringRepresentable{
        ENCODED("encoded"),
        INVALID("invalid");

        public static final Codec<CraftingCardStatus> CODEC = StringRepresentable.fromEnum(CraftingCardStatus::values);

        private final String serialized;

        CraftingCardStatus(String serialized) {
            this.serialized = serialized;
        }

        @Override
        public String getSerializedName() {
            return serialized;
        }
    }
}