package mekanism.common.lib.codec;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.BaseMapCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Copy of {@link com.mojang.serialization.codecs.UnboundedMapCodec}, but with decoding modified to not fail for duplicate keys
 */
public record DroppingUnboundedMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec) implements BaseMapCodec<K, V>, Codec<Map<K, V>> {
    //TODO - 1.22: Remove this class

    @Override
    public <T> DataResult<Pair<Map<K, V>, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map)).map(r -> Pair.of(r, input));
    }

    @Override
    public <T> DataResult<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final T prefix) {
        return encode(input, ops, ops.mapBuilder()).build(prefix);
    }

    @Override
    public String toString() {
        return "DroppingUnboundedMapCodec[" + keyCodec + " -> " + elementCodec + ']';
    }

    @Override
    public <T> DataResult<Map<K, V>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final Object2ObjectMap<K, V> read = new Object2ObjectArrayMap<>();
        final Stream.Builder<Pair<T, T>> failed = Stream.builder();

        final DataResult<Unit> result = input.entries().reduce(
              DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
              (r, pair) -> {
                  final DataResult<K> key = keyCodec().parse(ops, pair.getFirst());
                  final DataResult<V> value = elementCodec().parse(ops, pair.getSecond());

                  final DataResult<Pair<K, V>> entryResult = key.apply2stable(Pair::of, value);
                  final Optional<Pair<K, V>> entry = entryResult.resultOrPartial();
                  if (entry.isPresent()) {
                      final V existingValue = read.putIfAbsent(entry.get().getFirst(), entry.get().getSecond());
                      //Unlike super, if there is an existing value, we just want to use that and ignore the alternate value
                      //Note: We just return r, rather than checking if there was an error in the value we didn't add
                      // as we are skipping it, so we don't care if there was
                      return r;
                      /*if (existingValue != null) {
                          failed.add(pair);
                          return r.apply2stable((u, p) -> u, DataResult.error(() -> "Duplicate entry for key: '" + entry.get().getFirst() + "'"));
                      }*/
                  }
                  if (entryResult.isError()) {
                      failed.add(pair);
                  }

                  return r.apply2stable((u, p) -> u, entryResult);
              },
              (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2)
        );

        final Map<K, V> elements = ImmutableMap.copyOf(read);
        final T errors = ops.createMap(failed.build());

        return result.map(unit -> elements).setPartial(elements).mapError(e -> e + " missed input: " + errors);
    }
}