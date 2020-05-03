package mekanism.common.lib;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test WildcardMatcher implementation")
class WildcardMatcherTest {

    @Test
    @DisplayName("Test universal wildcard")
    void testUniversalWildcard() {
        Assertions.assertTrue(WildcardMatcher.matches("*", "test"));
        Assertions.assertTrue(WildcardMatcher.matches("*", ""));
    }

    @Test
    @DisplayName("Test exact string matching")
    void testExactMatch() {
        Assertions.assertTrue(WildcardMatcher.matches("", ""));
        Assertions.assertTrue(WildcardMatcher.matches("hello", "hello"));
        Assertions.assertFalse(WildcardMatcher.matches("hell", "hello"));
        Assertions.assertFalse(WildcardMatcher.matches("hello", "hell"));
        Assertions.assertFalse(WildcardMatcher.matches("", "hello"));
        Assertions.assertFalse(WildcardMatcher.matches("hello", ""));
    }

    @Test
    @DisplayName("Test character-specific wildcards")
    void testCharacterSpecificWildcards() {
        Assertions.assertTrue(WildcardMatcher.matches("he?lo", "hello"));
        Assertions.assertTrue(WildcardMatcher.matches("#23#", "1234"));
        Assertions.assertTrue(WildcardMatcher.matches("?###?", "h329o"));
        Assertions.assertFalse(WildcardMatcher.matches("??#?", "AAAA"));
        Assertions.assertFalse(WildcardMatcher.matches("?????", "AAAA"));
    }

    @Test
    @DisplayName("Test wildcard character")
    void testWildcardGroupings() {
        Assertions.assertTrue(WildcardMatcher.matches("*******", "test"));
        Assertions.assertTrue(WildcardMatcher.matches("*test*", "12341234test23523"));
        Assertions.assertTrue(WildcardMatcher.matches("test*", "test23423424"));
        Assertions.assertTrue(WildcardMatcher.matches("*test", "234234test"));
        Assertions.assertTrue(WildcardMatcher.matches("*test*another*", "23234test2342389another23423"));
        Assertions.assertTrue(WildcardMatcher.matches("*#*", "asudnfai3asifa"));
        Assertions.assertFalse(WildcardMatcher.matches("*#*", "asudnfaiasifa"));
        Assertions.assertFalse(WildcardMatcher.matches("*test*tester", "29342398test289289tester3"));
    }
}