package mekanism.common.content.qio;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.common.base.TagCache;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.TriPredicate;
import org.jetbrains.annotations.Nullable;

/**
 * Advanced pattern searching, in use by QIO Item Viewers. Only use on client-side.
 *
 * @author aidancbrady
 */
public class SearchQueryParser {

    private static final ISearchQuery INVALID = (level, stack) -> false;
    private static final Set<Character> TERMINATORS = Set.of('|', '(', '\"', '\'');

    public static ISearchQuery parse(String query) {
        List<SearchQuery> ret = new ArrayList<>();
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
                return INVALID;
            }
            i = keyListResult.index();
        }
        if (!curQuery.isEmpty()) {
            ret.add(curQuery);
        }
        return new SearchQueryList(ret);
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
        NAME('~', (level, key, stack) -> stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(key.toLowerCase(Locale.ROOT))),
        MOD_ID('@', (level, key, stack) -> MekanismUtils.getModId(stack).toLowerCase(Locale.ROOT).contains(key.toLowerCase(Locale.ROOT))),
        TOOLTIP('$', (level, key, stack) -> {
            List<Component> tooltipLines = stack.getTooltipLines(Item.TooltipContext.of(level), null, Default.NORMAL);
            if (!tooltipLines.isEmpty()) {
                String lowerKey = key.toLowerCase(Locale.ROOT);
                for (Component tooltipLine : tooltipLines) {
                    String tooltip = tooltipLine.getString().toLowerCase(Locale.ROOT);
                    if (tooltip.contains(lowerKey)) {
                        return true;
                    }
                }
            }
            return false;
        }),
        TAG('#', (level, key, stack) -> {
            List<String> itemTags = TagCache.getItemTags(stack);
            if (!itemTags.isEmpty()) {
                String lowerKey = key.toLowerCase(Locale.ROOT);
                for (String tag : itemTags) {
                    if (tag.toLowerCase(Locale.ROOT).contains(lowerKey)) {
                        return true;
                    }
                }
            }
            return false;
        });

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
        private final TriPredicate<@Nullable Level, String, ItemStack> checker;

        QueryType(char prefix, TriPredicate<@Nullable Level, String, ItemStack> checker) {
            this.prefix = prefix;
            this.checker = checker;
        }

        public boolean matches(@Nullable Level level, String key, ItemStack stack) {
            return checker.test(level, key, stack);
        }
    }

    public static class SearchQuery implements ISearchQuery {

        private final Map<QueryType, List<String>> queryStrings = new LinkedHashMap<>();

        @Override
        public boolean test(@Nullable Level level, ItemStack stack) {
            for (Entry<QueryType, List<String>> entry : queryStrings.entrySet()) {
                boolean hasMatch = true;
                for (String key : entry.getValue()) {
                    if (entry.getKey().matches(level, key, stack)) {
                        hasMatch = false;
                        break;
                    }
                }
                if (hasMatch) {
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
    }

    public static class SearchQueryList implements ISearchQuery {

        private final List<SearchQuery> queries;

        private SearchQueryList(List<SearchQuery> queries) {
            this.queries = queries;
        }

        @Override
        public boolean test(@Nullable Level level, ItemStack stack) {
            // allow empty query lists to match all stacks
            if (queries.isEmpty()) {
                return true;
            }
            for (SearchQuery query : queries) {
                if (query.test(level, stack)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return queries.toString();
        }

        protected List<SearchQuery> getQueries() {
            return queries;
        }
    }

    @FunctionalInterface
    public interface ISearchQuery extends BiPredicate<@Nullable Level, ItemStack> {

        default boolean isInvalid() {
            return this == INVALID;
        }
    }
}
