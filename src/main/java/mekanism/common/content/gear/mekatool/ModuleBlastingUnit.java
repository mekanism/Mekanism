package mekanism.common.content.gear.mekatool;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.TranslatableEnum;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public record ModuleBlastingUnit(BlastRadius blastRadius) implements ICustomModule<ModuleBlastingUnit> {

    public static final Identifier BLAST_RADIUS = Mekanism.rl("blast_radius");

    private static final Identifier RADIAL_ID = Mekanism.rl("blasting_mode");
    private static final Int2ObjectMap<Lazy<NestedRadialMode>> RADIAL_DATAS = Util.make(() -> {
        int types = BlastRadius.values().length - 1;
        Int2ObjectMap<Lazy<NestedRadialMode>> map = new Int2ObjectArrayMap<>(types);
        for (int type = 1; type <= types; type++) {
            int accessibleValues = type + 1;
            map.put(type, Lazy.of(() -> new NestedRadialMode(IRadialDataHelper.INSTANCE.dataForTruncated(RADIAL_ID, accessibleValues, BlastRadius.LOW),
                  MekanismLang.RADIAL_BLASTING_POWER, BlastRadius.LOW.icon(), EnumColor.DARK_BLUE)));
        }
        return map;
    });

    public ModuleBlastingUnit(IModule<ModuleBlastingUnit> module) {
        this(module.<BlastRadius>getConfigOrThrow(BLAST_RADIUS).get());
    }

    private NestedRadialMode getNestedData(IModule<ModuleBlastingUnit> module) {
        return RADIAL_DATAS.get(module.getInstalledCount()).get();
    }

    private RadialData<?> getRadialData(IModule<ModuleBlastingUnit> module) {
        return getNestedData(module).nestedData();
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addRadialModes(IModule<ModuleBlastingUnit> module, ITEM instance, Consumer<NestedRadialMode> adder) {
        adder.accept(getNestedData(module));
    }

    @Nullable
    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter, MODE extends IRadialMode> MODE getMode(IModule<ModuleBlastingUnit> module, ITEM instance,
          RadialData<MODE> radialData) {
        if (radialData == getRadialData(module)) {
            return (MODE) blastRadius;
        }
        return null;
    }

    @Override
    public <MODE extends IRadialMode> boolean setMode(IModule<ModuleBlastingUnit> module, Player player, ItemAccess itemAccess, RadialData<MODE> radialData, MODE mode,
          @Nullable TransactionContext transaction) {
        if (radialData == getRadialData(module)) {
            BlastRadius newMode = (BlastRadius) mode;
            if (blastRadius != newMode) {
                module.replaceModuleConfig(player.registryAccess(), itemAccess, transaction, module.<BlastRadius>getConfigOrThrow(BLAST_RADIUS).with(newMode));
            }
        }
        return false;
    }

    public int getBlastRadius() {
        return blastRadius.getRadius();
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDStrings(IModule<ModuleBlastingUnit> module, IModuleContainer moduleContainer, ITEM instance,
          Player player, Consumer<Component> hudStringAdder) {
        //Only add hud string if enabled in config
        if (module.isEnabled()) {
            hudStringAdder.accept(MekanismLang.MODULE_BLASTING_ENABLED.translateColored(EnumColor.DARK_GRAY, EnumColor.INDIGO, blastRadius));
        }
    }

    public enum BlastRadius implements IHasTextComponent, TranslatableEnum, IRadialMode, StringRepresentable {
        OFF(0, MekanismLang.RADIAL_BLASTING_POWER_OFF, EnumColor.WHITE, "blasting_off"),
        LOW(1, MekanismLang.RADIAL_BLASTING_POWER_LOW, EnumColor.BRIGHT_GREEN, "blasting_low"),
        MED(2, MekanismLang.RADIAL_BLASTING_POWER_MED, EnumColor.YELLOW, "blasting_med"),
        HIGH(3, MekanismLang.RADIAL_BLASTING_POWER_HIGH, EnumColor.ORANGE, "blasting_high"),
        EXTREME(4, MekanismLang.RADIAL_BLASTING_POWER_EXTREME, EnumColor.RED, "blasting_extreme");

        public static final Codec<BlastRadius> CODEC = StringRepresentable.fromEnum(BlastRadius::values);
        public static final IntFunction<BlastRadius> BY_ID = ByIdMap.continuous(BlastRadius::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, BlastRadius> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, BlastRadius::ordinal);

        private final String serializedName;
        private final int radius;
        private final Component label;
        private final EnumColor color;
        private final Identifier icon;
        private final ILangEntry langEntry;

        BlastRadius(int radius, ILangEntry langEntry, EnumColor color, String texture) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.radius = radius;
            this.label = MekanismLang.MODULE_BLAST_AREA.translate(2 * radius + 1);
            this.langEntry = langEntry;
            this.color = color;
            this.icon = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_RADIAL, texture + ".png");
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public int getRadius() {
            return radius;
        }

        @Override
        public Component sliceName() {
            return langEntry.translateColored(color);
        }

        @Override
        public Component getTranslatedName() {
            return sliceName();
        }

        @Override
        public Identifier icon() {
            return icon;
        }

        @Override
        public EnumColor color() {
            return color;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}