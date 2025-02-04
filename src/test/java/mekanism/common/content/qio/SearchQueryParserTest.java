package mekanism.common.content.qio;

import mekanism.common.content.qio.SearchQueryParser.ISearchQuery;
import mekanism.common.content.qio.SearchQueryParser.SearchQuerySet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test SearchQueryParser implementation")
class SearchQueryParserTest {

    @Test
    @DisplayName("Test OR'ed queries")
    void testMultiQueries() {
        // two whole different queries
        queryAssert("test | test2", "[{NAME=[test]}, {NAME=[test2]}]");
        // test two queries, first with two NAME keys
        queryAssert("(test | test1) | test2", "[{NAME=[test, test1]}, {NAME=[test2]}]");
        // test empty queries
        queryAssert("test | test1 | |  ", "[{NAME=[test]}, {NAME=[test1]}]");
    }

    @Test
    @DisplayName("Test key lists")
    void testLists() {
        // test long list
        queryAssert("(test | test1 | test2 test3 | test4)", "{NAME=[test, test1, test2 test3, test4]}");
        // test two queries, both with lists
        queryAssert("(test | test1) | (test2 | test3)", "[{NAME=[test, test1]}, {NAME=[test2, test3]}]");
        // test various query types in lists
        queryAssert("@(mod | mod1) (test | test1) $(tool | tool1) #(tag | tag1)", "{NAME=[test, test1], MOD_ID=[mod, mod1], TOOLTIP=[tool, tool1], TAG=[tag, tag1]}");
    }

    @Test
    @DisplayName("Test quotes")
    void testQuotes() {
        queryAssert("\"test with (parentheses)\"", "{NAME=[test with (parentheses)]}");
        queryAssert("\"test with (parentheses)\" | \"test2\"", "[{NAME=[test with (parentheses)]}, {NAME=[test2]}]");
        queryInvalid("\"no end quote");
        queryInvalid("quote at end\"");
    }

    @Test
    @DisplayName("Test various query types")
    void testQueryTypes() {
        queryAssert("@mod #tag $tool test", "{NAME=[test], MOD_ID=[mod], TOOLTIP=[tool], TAG=[tag]}");
        // with quotes
        queryAssert("#tag $\"this is a tooltip\" @mod", "{MOD_ID=[mod], TOOLTIP=[this is a tooltip], TAG=[tag]}");
        // with list
        queryAssert("$tool #(tag1 | tag2) test name", "{NAME=[test name], TOOLTIP=[tool], TAG=[tag1, tag2]}");
        // quotes + list
        queryAssert("$\"tooltip test\" test name #(tag1 | tag2) @mod", "{NAME=[test name], MOD_ID=[mod], TOOLTIP=[tooltip test], TAG=[tag1, tag2]}");
    }

    @Test
    @DisplayName("Test weird spacing")
    void testSpacing() {
        queryAssert("       test     ", "{NAME=[test]}");
        // key immediately following quote of other type
        queryAssert("@\"test mod\"mod", "{NAME=[mod], MOD_ID=[test mod]}");
    }

    @Test
    @DisplayName("Test random queries, trying to cause a crash")
    void testRandomQueries() {
        Assertions.assertDoesNotThrow(() -> SearchQueryParser.parse("|4434|'f1419879182749182^?%#@*&$1@(#*$\"'"));
    }

    private void queryAssert(String query, String mapResult) {
        Assertions.assertEquals(mapResult, SearchQueryParser.parseOrdered(query).toString());
    }

    private void queryInvalid(String query) {
        Assertions.assertTrue(SearchQueryParser.parse(query).isInvalid());
    }

    @SuppressWarnings("unused")
    private void readout(String queryStr) {
        ISearchQuery query = SearchQueryParser.parseOrdered(queryStr);
        if (query instanceof SearchQuerySet searchQueryList) {
            System.out.println(searchQueryList.getQueries());
        }
        System.out.println(query.isInvalid());
    }
}
