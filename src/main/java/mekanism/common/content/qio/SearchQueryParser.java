package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import mekanism.common.base.TagCache;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Advanced pattern searching, in use by QIO Item Viewers. Only use on client-side.
 *
 * @author aidancbrady
 */
public class SearchQueryParser {

    private static final Set<Character> TERMINATORS = Set.of('|', '(', '\"', '\'');

    /**
     * @param query Expected to be lower case
     */
    public static ISearchQuery parse(String query) {
        if (query == null || query.isEmpty()) {
            return ISearchQuery.INVALID;
        }
        return parse(query, new HashSet<>());
    }

    @VisibleForTesting
    public static ISearchQuery parseOrdered(String query) {
        if (query == null || query.isEmpty()) {
            return ISearchQuery.INVALID;
        }
        return parse(query.trim().toLowerCase(Locale.ROOT), new LinkedHashSet<>());
    }

    private static ISearchQuery parse(String query, Set<SearchQuery> ret) {
        SearchQuery curQuery = new SearchQuery();

        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            // query split
            if (c == '|') {
                if (!curQuery.isEmpty()) {
                    ret.add(curQuery);
                }
                curQuery = new SearchQuery();
                continue;
            } else if (c == ' ') {
                // always skip spaces
                continue;
            }
            QueryType type = QueryType.get(c);
            if (type != null) {
                // increment our pointer, we skip over the query prefix
                i++;
            } else {
                // default to the name query type otherwise; no need to increment as name has no prefix
                type = QueryType.NAME;
            }
            // read the key string(s) of the given query type
            KeyListResult keyListResult = readKeyList(query, i, type, curQuery);
            if (!keyListResult.hasResult()) {
                return ISearchQuery.INVALID;
            }
            i = keyListResult.index();
        }
        if (!curQuery.isEmpty()) {
            if (ret.isEmpty()) {
                return curQuery;
            }
            ret.add(curQuery);
        } else if (ret.size() == 1) {
            return ret.iterator().next();
        }
        return ret.isEmpty() ? ISearchQuery.INVALID : new SearchQuerySet(ret);
    }

    private static KeyListResult readKeyList(String query, int start, QueryType type, SearchQuery curQuery) {
        // make sure the query doesn't begin out of string bounds
        // if it does, it's just incomplete- we'll treat it as valid and just skip this key list
        if (start >= query.length()) {
            return new KeyListResult(true, start);
        }
        int newIndex;
        List<String> keys;
        char qc = query.charAt(start);
        if (qc == '(') {
            ListResult<String> listResult = readList(query, start);
            if (listResult == null) {
                return KeyListResult.INVALID;
            }
            keys = listResult.result();
            newIndex = listResult.index();
        } else if (qc == '\"' || qc == '\'') {
            Result quoteResult = readQuote(query, start);
            if (quoteResult == null) {
                return KeyListResult.INVALID;
            }
            keys = Collections.singletonList(quoteResult.result());
            newIndex = quoteResult.index();
        } else {
            Result textResult = readUntilTermination(query, start, type != QueryType.NAME);
            keys = Collections.singletonList(textResult.result());
            newIndex = textResult.index();
        }
        if (!keys.isEmpty()) {
            curQuery.queryStrings.put(type, keys);
        }
        return new KeyListResult(true, newIndex);
    }

    // called with index of start parenthesis, returns index of last character
    @Nullable
    private static ListResult<String> readList(String query, int start) {
        List<String> ret = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (int i = start + 1; i < query.length(); i++) {
            char qc = query.charAt(i);
            switch (qc) {
                case ')' -> {
                    String key = sb.toString().trim();
                    if (!key.isEmpty()) {
                        ret.add(key);
                    }
                    return new ListResult<>(ret, i);
                }
                case '|' -> {
                    String key = sb.toString().trim();
                    if (!key.isEmpty()) {
                        ret.add(key);
                    }
                    sb = new StringBuilder();
                }
                case '\"', '\'' -> {
                    Result quoteResult = readQuote(query, i);
                    if (quoteResult == null) {
                        return null;
                    }
                    ret.add(quoteResult.result());
                    i = quoteResult.index();
                }
                default -> sb.append(qc);
            }
        }

        return null;
    }

    // called with the index of the start quote, returns index of last character
    @Nullable
    private static Result readQuote(String text, int start) {
        char quoteChar = text.charAt(start);
        StringBuilder ret = new StringBuilder();
        for (int i = start + 1; i < text.length(); i++) {
            char tc = text.charAt(i);
            if (tc == quoteChar) {
                return new Result(ret.toString(), i);
            }
            ret.append(tc);
        }
        return null;
    }

    private static Result readUntilTermination(String text, int start, boolean spaceTerminate) {
        StringBuilder sb = new StringBuilder();
        int i = start;
        for (; i < text.length(); i++) {
            char tc = text.charAt(i);
            if (TERMINATORS.contains(tc) || QueryType.get(tc) != null || (spaceTerminate && tc == ' ')) {
                i--; // back up so we don't include terminating char
                break;
            }
            sb.append(tc);
        }
        return new Result(sb.toString().trim(), i);
    }

    private record KeyListResult(boolean hasResult, int index) {

        public static final KeyListResult INVALID = new KeyListResult(false, -1);
    }

    private record Result(String result, int index) {
    }

    private record ListResult<TYPE>(List<TYPE> result, int index) {
    }

    public enum QueryType {
        // ~ is a dummy char, not actually used by parser
        NAME('~') {
            @Override
            public boolean matches(@Nullable Level level, @Nullable Player player, String key, ItemStack stack) {
                return stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(key);
            }
        },
        MOD_ID('@') {
            @Override
            public boolean matches(@Nullable Level level, @Nullable Player player, String key, ItemStack stack) {
                return MekanismUtils.getModId(stack).toLowerCase(Locale.ROOT).contains(key);
            }
        },
        TOOLTIP('$') {
            @Override
            public boolean matches(@Nullable Level level, @Nullable Player player, String key, ItemStack stack) {
                for (Component tooltipLine : stack.getTooltipLines(Item.TooltipContext.of(level), player, Default.NORMAL)) {
                    String tooltip = tooltipLine.getString().toLowerCase(Locale.ROOT);
                    if (tooltip.contains(key)) {
                        return true;
                    }
                }
                return false;
            }
        },
        TAG('#') {
            @Override
            public boolean matches(@Nullable Level level, @Nullable Player player, String key, ItemStack stack) {
                for (String tag : TagCache.getItemTags(stack)) {
                    if (tag.toLowerCase(Locale.ROOT).contains(key)) {
                        return true;
                    }
                }
                return false;
            }
        };

        private static final Char2ObjectMap<QueryType> charLookupMap;

        static {
            QueryType[] values = values();
            charLookupMap = new Char2ObjectArrayMap<>(values.length);
            for (QueryType type : values) {
                charLookupMap.put(type.prefix, type);
            }
        }

        public static QueryType get(char prefix) {
            return charLookupMap.get(prefix);
        }

        public static CharSet getPrefixChars() {
            return charLookupMap.keySet();
        }

        private final char prefix;

        QueryType(char prefix) {
            this.prefix = prefix;
        }

        public abstract boolean matches(@Nullable Level level, @Nullable Player player, String key, ItemStack stack);
    }

    public static class SearchQuery implements ISearchQuery {

        //TODO: Do we want to allow adding to query strings of a query type after the fact and instead force using an query set if you don't want to use parenthesis
        private final Map<QueryType, List<String>> queryStrings = new EnumMap<>(QueryType.class);

        @Override
        public boolean test(@Nullable Level level, @Nullable Player player, ItemStack stack) {
            for (Map.Entry<QueryType, List<String>> entry : queryStrings.entrySet()) {
                boolean hasNoMatch = true;
                for (String key : entry.getValue()) {
                    if (entry.getKey().matches(level, player, key, stack)) {
                        hasNoMatch = false;
                        break;
                    }
                }
                if (hasNoMatch) {
                    return false;
                }
            }
            return true;
        }

        private boolean isEmpty() {
            return queryStrings.isEmpty();
        }

        protected Map<QueryType, List<String>> getQueryMap() {
            return queryStrings;
        }

        @Override
        public String toString() {
            return queryStrings.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return queryStrings.equals(((SearchQuery) o).queryStrings);
        }

        @Override
        public int hashCode() {
            return queryStrings.hashCode();
        }
    }

    public static class SearchQuerySet implements ISearchQuery {

        private final Set<SearchQuery> queries;

        private SearchQuerySet(Set<SearchQuery> queries) {
            this.queries = queries;
        }

        @Override
        public boolean test(@Nullable Level level, @Nullable Player player, ItemStack stack) {
            // allow empty query lists to match all stacks
            if (queries.isEmpty()) {
                return true;
            }
            for (SearchQuery query : queries) {
                if (query.test(level, player, stack)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return queries.toString();
        }

        protected Set<SearchQuery> getQueries() {
            return queries;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return queries.equals(((SearchQuerySet) o).queries);
        }

        @Override
        public int hashCode() {
            return queries.hashCode();
        }
    }

    @FunctionalInterface
    public interface ISearchQuery {

        ISearchQuery INVALID = (level, player, stack) -> false;

        boolean test(@Nullable Level level, @Nullable Player player, ItemStack stack);

        default boolean isInvalid() {
            return this == INVALID;
        }
    }
}
