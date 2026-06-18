package mekanism.api.text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/// Similar to [net.minecraft.world.level.block.ColorCollection] but with the extra colors we have in [EnumColor].
///
/// @since 10.8.0
public record EnumColorCollection<TYPE>(
      TYPE white, TYPE orange,
      //Vanilla name for the corresponding enum color is magenta
      TYPE pink,
      //Vanilla name for the corresponding enum color is lightBlue
      TYPE indigo,
      TYPE yellow,
      //Vanilla name for the corresponding enum color is lime
      TYPE brightGreen,
      //Vanilla name for the corresponding enum color is pink
      TYPE brightPink,
      //Vanilla name for the corresponding enum color is gray
      TYPE darkGray,
      //Vanilla name for the corresponding enum color is lightGray
      TYPE gray,
      //Vanilla name for the corresponding enum color is cyan
      TYPE darkAqua,
      TYPE purple,
      //Vanilla name for the corresponding enum color is blue
      TYPE darkBlue,
      TYPE brown,
      //Vanilla name for the corresponding enum color is green
      TYPE darkGreen,
      TYPE red, TYPE black, TYPE darkRed, TYPE aqua
) {

    public static final EnumColorCollection<EnumColor> VALUES = new EnumColorCollection<>(
          EnumColor.WHITE,
          EnumColor.ORANGE,
          EnumColor.PINK,
          EnumColor.INDIGO,
          EnumColor.YELLOW,
          EnumColor.BRIGHT_GREEN,
          EnumColor.BRIGHT_PINK,
          EnumColor.DARK_GRAY,
          EnumColor.GRAY,
          EnumColor.DARK_AQUA,
          EnumColor.PURPLE,
          EnumColor.DARK_BLUE,
          EnumColor.BROWN,
          EnumColor.DARK_GREEN,
          EnumColor.RED,
          EnumColor.BLACK,
          EnumColor.DARK_RED,
          EnumColor.AQUA
    );
    public static final EnumColorCollection<String> NAMES = VALUES.map(EnumColor::getRegistryPrefix);

    public static <T> EnumColorCollection<T> create(T value) {
        return new EnumColorCollection<>(value, value, value, value, value, value, value, value, value, value, value, value, value, value, value, value, value, value);
    }

    public static EnumColorCollection<String> prefixWithColor(EnumColorCollection<String> ids) {
        return zipMap(NAMES, ids, (color, id) -> color + "_" + id);
    }

    public List<TYPE> asList() {
        Builder<TYPE> builder = ImmutableList.builderWithExpectedSize(18);
        forEach(builder::add);
        return builder.build();
    }

    public void forEach(Consumer<TYPE> consumer) {
        consumer.accept(this.white);
        consumer.accept(this.orange);
        consumer.accept(this.pink);
        consumer.accept(this.indigo);
        consumer.accept(this.yellow);
        consumer.accept(this.brightGreen);
        consumer.accept(this.brightPink);
        consumer.accept(this.darkGray);
        consumer.accept(this.gray);
        consumer.accept(this.darkAqua);
        consumer.accept(this.purple);
        consumer.accept(this.darkBlue);
        consumer.accept(this.brown);
        consumer.accept(this.darkGreen);
        consumer.accept(this.red);
        consumer.accept(this.black);
        consumer.accept(this.darkRed);
        consumer.accept(this.aqua);
    }

    public TYPE pick(EnumColor color) {
        return switch (color) {
            case WHITE -> this.white;
            case ORANGE -> this.orange;
            case PINK -> this.pink;
            case INDIGO -> this.indigo;
            case YELLOW -> this.yellow;
            case BRIGHT_GREEN -> this.brightGreen;
            case BRIGHT_PINK -> this.brightPink;
            case DARK_GRAY -> this.darkGray;
            case GRAY -> this.gray;
            case DARK_AQUA -> this.darkAqua;
            case PURPLE -> this.purple;
            case DARK_BLUE -> this.darkBlue;
            case BROWN -> this.brown;
            case DARK_GREEN -> this.darkGreen;
            case RED -> this.red;
            case BLACK -> this.black;
            case DARK_RED -> this.darkRed;
            case AQUA -> this.aqua;
        };
    }

    public <U> EnumColorCollection<U> map(Function<TYPE, U> mapper) {
        return new EnumColorCollection<>(
              mapper.apply(this.white),
              mapper.apply(this.orange),
              mapper.apply(this.pink),
              mapper.apply(this.indigo),
              mapper.apply(this.yellow),
              mapper.apply(this.brightGreen),
              mapper.apply(this.brightPink),
              mapper.apply(this.darkGray),
              mapper.apply(this.gray),
              mapper.apply(this.darkAqua),
              mapper.apply(this.purple),
              mapper.apply(this.darkBlue),
              mapper.apply(this.brown),
              mapper.apply(this.darkGreen),
              mapper.apply(this.red),
              mapper.apply(this.black),
              mapper.apply(this.darkRed),
              mapper.apply(this.aqua)
        );
    }

    public static <T, U> void zipApply(EnumColorCollection<T> first, EnumColorCollection<U> second, BiConsumer<T, U> consumer) {
        consumer.accept(first.white(), second.white());
        consumer.accept(first.orange(), second.orange());
        consumer.accept(first.pink(), second.pink());
        consumer.accept(first.indigo(), second.indigo());
        consumer.accept(first.yellow(), second.yellow());
        consumer.accept(first.brightGreen(), second.brightGreen());
        consumer.accept(first.brightPink(), second.brightPink());
        consumer.accept(first.darkGray(), second.darkGray());
        consumer.accept(first.gray(), second.gray());
        consumer.accept(first.darkAqua(), second.darkAqua());
        consumer.accept(first.purple(), second.purple());
        consumer.accept(first.darkBlue(), second.darkBlue());
        consumer.accept(first.brown(), second.brown());
        consumer.accept(first.darkGreen(), second.darkGreen());
        consumer.accept(first.red(), second.red());
        consumer.accept(first.black(), second.black());
        consumer.accept(first.darkRed(), second.darkRed());
        consumer.accept(first.aqua(), second.aqua());
    }

    public static <T, U, R> EnumColorCollection<R> zipMap(EnumColorCollection<T> first, EnumColorCollection<U> second, BiFunction<T, U, R> operation) {
        return new EnumColorCollection<>(
              operation.apply(first.white(), second.white()),
              operation.apply(first.orange(), second.orange()),
              operation.apply(first.pink(), second.pink()),
              operation.apply(first.indigo(), second.indigo()),
              operation.apply(first.yellow(), second.yellow()),
              operation.apply(first.brightGreen(), second.brightGreen()),
              operation.apply(first.brightPink(), second.brightPink()),
              operation.apply(first.darkGray(), second.darkGray()),
              operation.apply(first.gray(), second.gray()),
              operation.apply(first.darkAqua(), second.darkAqua()),
              operation.apply(first.purple(), second.purple()),
              operation.apply(first.darkBlue(), second.darkBlue()),
              operation.apply(first.brown(), second.brown()),
              operation.apply(first.darkGreen(), second.darkGreen()),
              operation.apply(first.red(), second.red()),
              operation.apply(first.black(), second.black()),
              operation.apply(first.darkRed(), second.darkRed()),
              operation.apply(first.aqua(), second.aqua())
        );
    }
}