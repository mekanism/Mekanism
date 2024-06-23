package mekanism.common.content.gear.mekasuit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

@ParametersAreNotNullByDefault
public record ModuleHydraulicPropulsionUnit(JumpBoost jumpBoost, StepAssist stepAssist) implements ICustomModule<ModuleHydraulicPropulsionUnit> {

    public static final ResourceLocation JUMP_BOOST = Mekanism.rl("jump_boost");
    public static final ResourceLocation STEP_ASSIST = Mekanism.rl("step_assist");

    public ModuleHydraulicPropulsionUnit(IModule<ModuleHydraulicPropulsionUnit> module) {
        this(module.<JumpBoost>getConfigOrThrow(JUMP_BOOST).get(), module.<StepAssist>getConfigOrThrow(STEP_ASSIST).get());
    }

    public float getBoost() {
        return jumpBoost.getBoost();
    }

    public float getStepHeight() {
        return stepAssist.getHeight();
    }

    @NothingNullByDefault
    public enum JumpBoost implements IHasTextComponent, StringRepresentable {
        OFF(0),
        LOW(0.5F),
        MED(1),
        HIGH(3),
        ULTRA(5);

        public static final Codec<JumpBoost> CODEC = StringRepresentable.fromEnum(JumpBoost::values);
        public static final IntFunction<JumpBoost> BY_ID = ByIdMap.continuous(JumpBoost::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, JumpBoost> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, JumpBoost::ordinal);

        private final String serializedName;
        private final float boost;
        private final Component label;

        JumpBoost(float boost) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.boost = boost;
            this.label = TextComponentUtil.getString(Float.toString(boost));
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public float getBoost() {
            return boost;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    @NothingNullByDefault
    public enum StepAssist implements IHasTextComponent, StringRepresentable {
        OFF(0),
        LOW(0.5F),
        MED(1),
        HIGH(1.5F),
        ULTRA(2);

        public static final Codec<StepAssist> CODEC = StringRepresentable.fromEnum(StepAssist::values);
        public static final IntFunction<StepAssist> BY_ID = ByIdMap.continuous(StepAssist::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, StepAssist> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, StepAssist::ordinal);

        private final String serializedName;
        private final float height;
        private final Component label;

        StepAssist(float height) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.height = height;
            this.label = TextComponentUtil.getString(Float.toString(height));
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public float getHeight() {
            return height;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}