package mekanism.common.content.qio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.TagCache;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;

public class SearchQueryParser {

    private static final ISearchQuery INVALID = (stack) -> false;
    private static final Set<Character> TERMINATORS = Sets.newHashSet('|', '(', '\"', '\'');

    private String query;

    private SearchQueryParser(String query) {
        this.query = query;
    }

    private ISearchQuery parse() {
        List<SearchQuery> ret = new ArrayList<>();
        SearchQuery curQuery = new SearchQuery();

        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            // query split
            if (c == '|') {
                ret.add(curQuery);
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
            Pair<Boolean, Integer> keyListResult = readKeyList(query, i, type, curQuery);
            if (!keyListResult.getLeft()) {
                return INVALID;
            }
            i = keyListResult.getRight();
        }
        if (!curQuery.isEmpty()) {
            ret.add(curQuery);
        }
        return new SearchQueryList(ret);
    }

    public static ISearchQuery parse(String query) {
        return new SearchQueryParser(query).parse();
    }

    private static Pair<Boolean, Integer> readKeyList(String query, int start, QueryType type, SearchQuery curQuery) {
        int newIndex = -1;
        List<String> keys;
        if (query.charAt(start) == '(') {
            Pair<List<String>, Integer> listResult = readList(query, start);
            if (listResult == null) {
                return Pair.of(false, -1);
            }
            keys = listResult.getLeft();
            newIndex = listResult.getRight();
        } else if (query.charAt(start) == '\"' || query.charAt(start) == '\'') {
            Pair<String, Integer> quoteResult = readQuote(query, start);
            if (quoteResult == null) {
                return Pair.of(false, -1);
            }
            keys = Arrays.asList(quoteResult.getLeft());
            newIndex = quoteResult.getRight();
        } else {
            Pair<String, Integer> textResult = readUntilTermination(query, start, type != QueryType.NAME);
            keys = Arrays.asList(textResult.getLeft());
            newIndex = textResult.getRight();
        }
        curQuery.queryStrings.put(type, keys);
        return Pair.of(true, newIndex);
    }

    // called with index of start parenthesis, returns index of last character
    private static Pair<List<String>, Integer> readList(String query, int start) {
        List<String> ret = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (int i = start + 1; i < query.length(); i++) {
            if (query.charAt(i) == ')') {
                String key = sb.toString().trim();
                if (!key.isEmpty()) {
                    ret.add(key);
                }
                return Pair.of(ret, i);
            } else if (query.charAt(i) == '|') {
                String key = sb.toString().trim();
                if (!key.isEmpty()) {
                    ret.add(key);
                }
                sb = new StringBuilder();
            } else if (query.charAt(i) == '\"' || query.charAt(i) == '\'') {
                Pair<String, Integer> quoteResult = readQuote(query, i);
                if (quoteResult == null) {
                    return null;
                }
                ret.add(quoteResult.getLeft());
                i = quoteResult.getRight();
            } else {
                sb.append(query.charAt(i));
            }
        }

        return null;
    }

    // called with the index of the start quote, returns index of last character
    private static Pair<String, Integer> readQuote(String text, int start) {
        char quoteChar = text.charAt(start);
        StringBuilder ret = new StringBuilder();
        for (int i = start + 1; i < text.length(); i++) {
            if (text.charAt(i) == quoteChar) {
                return Pair.of(ret.toString(), i);
            }
            ret.append(text.charAt(i));
        }
        return null;
    }

    private static Pair<String, Integer> readUntilTermination(String text, int start, boolean spaceTerminate) {
        StringBuilder sb = new StringBuilder();
        int i = start;
        for (; i < text.length(); i++) {
            if (TERMINATORS.contains(text.charAt(i)) ||QueryType.get(text.charAt(i)) != null || (spaceTerminate && text.charAt(i) == ' ')) {
                i--; // back up so we don't include terminating char
                break;
            }
            sb.append(text.charAt(i));
        }
        return Pair.of(sb.toString().trim(), i);
    }

    public enum QueryType {
        // ~ is a dummy char, not actually used by parser
        NAME('~', (key, stack) -> stack.getDisplayName().getString().toLowerCase().contains(key.toLowerCase())),
        MOD_ID('@', (key, stack) -> stack.getItem().getRegistryName().getNamespace().toLowerCase().contains(key.toLowerCase())),
        TOOLTIP('$', (key, stack) -> stack.getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL).stream().map(t -> t.getString().toLowerCase())
              .anyMatch(tooltip -> tooltip.contains(key.toLowerCase()))),
        TAG('#', (key, stack) -> TagCache.getItemTags(stack).stream().anyMatch(itemTag -> itemTag.toLowerCase().contains(key.toLowerCase())));

        private static final Map<Character, QueryType> charLookupMap = new Char2ObjectOpenHashMap<>();
        static {
            for (QueryType type : QueryType.values()) {
                charLookupMap.put(type.prefix, type);
            }
        }

        public static QueryType get(char prefix) {
            return charLookupMap.get(prefix);
        }

        private char prefix;
        private BiPredicate<String, ItemStack> checker;

        private QueryType(char prefix, BiPredicate<String, ItemStack> checker) {
            this.prefix = prefix;
            this.checker = checker;
        }

        public boolean matches(String key, ItemStack stack) {
            return checker.test(key, stack);
        }
    }

    public class SearchQuery implements ISearchQuery {

        private Map<QueryType, List<String>> queryStrings = new Object2ObjectOpenHashMap<>();

        @Override
        public boolean matches(ItemStack stack) {
            return queryStrings.entrySet().stream().allMatch(entry -> entry.getValue().stream().anyMatch(key -> entry.getKey().matches(key, stack)));
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

    public class SearchQueryList implements ISearchQuery {

        private List<SearchQuery> queries = new ArrayList<>();

        private SearchQueryList(List<SearchQuery> queries) {
            this.queries = queries;
        }

        @Override
        public boolean matches(ItemStack stack) {
            return queries.stream().anyMatch(query -> query.matches(stack));
        }

        protected List<SearchQuery> getQueries() {
            return queries;
        }
    }

    public interface ISearchQuery {

        boolean matches(ItemStack stack);

        default boolean isInvalid() {
            return this == INVALID;
        }
    }
}
