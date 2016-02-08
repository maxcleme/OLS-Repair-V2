package com.cedarsoftware.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

public class TestStringUtilities {
  @Test
  public void testConstructorIsPrivate() throws Exception {
    Class c = StringUtilities.class;
    assertEquals(Modifier.FINAL, c.getModifiers() & Modifier.FINAL);

    Constructor<StringUtilities> con = c.getDeclaredConstructor();
    assertEquals(Modifier.PRIVATE, con.getModifiers() & Modifier.PRIVATE);
    con.setAccessible(true);

    assertNotNull(con.newInstance());
  }

  /**
  * @see com.cedarsoftware.util.StringUtilities#isEmpty(java.lang.String)
  */
  @Test
  public void testIsEmpty() {
    assertEquals(true, StringUtilities.isEmpty(null));
    assertEquals(true, StringUtilities.isEmpty(""));
    assertEquals(false, StringUtilities.isEmpty("foo"));
  }

  /**
  * @see com.cedarsoftware.util.StringUtilities#hasContent(java.lang.String)
  */
  @Test
  public void testHasContent() {
    assertEquals(false, StringUtilities.hasContent(null));
    assertEquals(false, StringUtilities.hasContent(""));
    assertEquals(true, StringUtilities.hasContent("foo"));
  }

  /**
  * @see com.cedarsoftware.util.StringUtilities#trimLength(java.lang.String)
  */
  @Test
  public void testTrimLength() {
    assertEquals(0, StringUtilities.trimLength(null));
    assertEquals(0, StringUtilities.trimLength(""));
    assertEquals(3, StringUtilities.trimLength("  abc "));
  }

  @Test
  public void testEqualsWithTrim() {
    assertEquals(true, StringUtilities.equalsWithTrim("abc", " abc "));
    assertEquals(true, StringUtilities.equalsWithTrim(" abc ", "abc"));
    assertEquals(false, StringUtilities.equalsWithTrim("abc", " AbC "));
    assertEquals(false, StringUtilities.equalsWithTrim(" AbC ", "abc"));
    assertEquals(false, StringUtilities.equalsWithTrim(null, ""));
    assertEquals(false, StringUtilities.equalsWithTrim("", null));
    assertEquals(true, StringUtilities.equalsWithTrim("", "\t\n\r"));
  }

  /**
  * @see com.cedarsoftware.util.StringUtilities#equalsIgnoreCaseWithTrim(java.lang.String, java.lang.String)
  */
  @Test
  public void testEqualsIgnoreCaseWithTrim() {
    assertEquals(true, StringUtilities.equalsIgnoreCaseWithTrim("abc", " abc "));
    assertEquals(true, StringUtilities.equalsIgnoreCaseWithTrim(" abc ", "abc"));
    assertEquals(true, StringUtilities.equalsIgnoreCaseWithTrim("abc", " AbC "));
    assertEquals(true, StringUtilities.equalsIgnoreCaseWithTrim(" AbC ", "abc"));
    assertEquals(false, StringUtilities.equalsIgnoreCaseWithTrim(null, ""));
    assertEquals(false, StringUtilities.equalsIgnoreCaseWithTrim("", null));
    assertEquals(true, StringUtilities.equalsIgnoreCaseWithTrim("", "\t\n\r"));
  }

  @Test
  public void testCount() {
    assertEquals(2, StringUtilities.count("abcabc", 'a'));
    assertEquals(0, StringUtilities.count("foo", 'a'));
    assertEquals(0, StringUtilities.count(null, 'a'));
    assertEquals(0, StringUtilities.count("", 'a'));
  }

  @Test
  public void testString() {
    assertTrue(StringUtilities.isEmpty(null));
    assertFalse(StringUtilities.hasContent(null));
    assertEquals(0, StringUtilities.trimLength(null));
    assertTrue(StringUtilities.equalsIgnoreCaseWithTrim("abc", " Abc "));
    assertTrue(StringUtilities.equalsWithTrim("abc", " abc "));
    assertEquals("1A", StringUtilities.encode(new byte[] {0x1A}));
    assertArrayEquals(new byte[] {0x1A}, StringUtilities.decode("1A"));
    assertEquals(2, StringUtilities.count("abcabc", 'a'));
  }

  @Test
  public void testEncode() {
    assertEquals("1A", StringUtilities.encode(new byte[] {0x1A}));
    assertEquals("", StringUtilities.encode(new byte[] {}));
  }

  @Test(expected = NullPointerException.class)
  public void testEncodeWithNull() {
    StringUtilities.encode(null);
  }

  @Test
  public void testDecode() {
    assertArrayEquals(new byte[] {0x1A}, StringUtilities.decode("1A"));
    assertArrayEquals(new byte[] {}, StringUtilities.decode(""));
    assertNull(StringUtilities.decode("1AB"));
  }

  @Test(expected = NullPointerException.class)
  public void testDecodeWithNull() {
    StringUtilities.decode(null);
  }

  /**
  * @see com.cedarsoftware.util.StringUtilities#equals(java.lang.String, java.lang.String)
  */
  @Test
  public void testEquals() {
    assertEquals(true, StringUtilities.equals(null, null));
    assertEquals(false, StringUtilities.equals(null, ""));
    assertEquals(false, StringUtilities.equals("", null));
    assertEquals(false, StringUtilities.equals("foo", "bar"));
    assertEquals(false, StringUtilities.equals("Foo", "foo"));
    assertEquals(true, StringUtilities.equals("foo", "foo"));
  }

  /**
  * @see com.cedarsoftware.util.StringUtilities#equalsIgnoreCase(java.lang.String, java.lang.String)
  */
  @Test
  public void testEqualsIgnoreCase() {
    assertEquals(true, StringUtilities.equalsIgnoreCase(null, null));
    assertEquals(false, StringUtilities.equalsIgnoreCase(null, ""));
    assertEquals(false, StringUtilities.equalsIgnoreCase("", null));
    assertEquals(false, StringUtilities.equalsIgnoreCase("foo", "bar"));
    assertEquals(true, StringUtilities.equalsIgnoreCase("Foo", "foo"));
    assertEquals(true, StringUtilities.equalsIgnoreCase("foo", "foo"));
  }

  /**
  * @see com.cedarsoftware.util.StringUtilities#lastIndexOf(java.lang.String, char)
  */
  @Test
  public void testLastIndexOf() {
    assertEquals(-1, StringUtilities.lastIndexOf(null, 'a'));
    assertEquals(-1, StringUtilities.lastIndexOf("foo", 'a'));
    assertEquals(1, StringUtilities.lastIndexOf("bar", 'a'));
  }

  /**
  * @see com.cedarsoftware.util.StringUtilities#length(java.lang.String)
  */
  @Test
  public void testLength() {
    assertEquals(0, StringUtilities.length(""));
    assertEquals(0, StringUtilities.length(null));
    assertEquals(3, StringUtilities.length("abc"));
  }

  /**
  * @see com.cedarsoftware.util.StringUtilities#levenshteinDistance(java.lang.String, java.lang.String)
  */
  @Test
  public void testLevenshtein() {
    assertEquals(3, StringUtilities.levenshteinDistance("example", "samples"));
    assertEquals(6, StringUtilities.levenshteinDistance("sturgeon", "urgently"));
    assertEquals(6, StringUtilities.levenshteinDistance("levenshtein", "frankenstein"));
    assertEquals(5, StringUtilities.levenshteinDistance("distance", "difference"));
    assertEquals(7, StringUtilities.levenshteinDistance("java was neat", "scala is great"));
    assertEquals(0, StringUtilities.levenshteinDistance(null, ""));
    assertEquals(0, StringUtilities.levenshteinDistance("", null));
    assertEquals(0, StringUtilities.levenshteinDistance(null, null));
    assertEquals(0, StringUtilities.levenshteinDistance("", ""));
    assertEquals(1, StringUtilities.levenshteinDistance(null, "1"));
    assertEquals(1, StringUtilities.levenshteinDistance("1", null));
    assertEquals(1, StringUtilities.levenshteinDistance("", "1"));
    assertEquals(1, StringUtilities.levenshteinDistance("1", ""));
    assertEquals(3, StringUtilities.levenshteinDistance("schill", "thrill"));
    assertEquals(2, StringUtilities.levenshteinDistance("abcdef", "bcdefa"));
  }

  /**
  * @see com.cedarsoftware.util.StringUtilities#damerauLevenshteinDistance(java.lang.String, java.lang.String)
  */
  @Test
  public void testDamerauLevenshtein() throws Exception {
    assertEquals(3, StringUtilities.damerauLevenshteinDistance("example", "samples"));
    assertEquals(6, StringUtilities.damerauLevenshteinDistance("sturgeon", "urgently"));
    assertEquals(6, StringUtilities.damerauLevenshteinDistance("levenshtein", "frankenstein"));
    assertEquals(5, StringUtilities.damerauLevenshteinDistance("distance", "difference"));
    assertEquals(9, StringUtilities.damerauLevenshteinDistance("java was neat", "groovy is great"));
    assertEquals(0, StringUtilities.damerauLevenshteinDistance(null, ""));
    assertEquals(0, StringUtilities.damerauLevenshteinDistance("", null));
    assertEquals(0, StringUtilities.damerauLevenshteinDistance(null, null));
    assertEquals(0, StringUtilities.damerauLevenshteinDistance("", ""));
    assertEquals(1, StringUtilities.damerauLevenshteinDistance(null, "1"));
    assertEquals(1, StringUtilities.damerauLevenshteinDistance("1", null));
    assertEquals(1, StringUtilities.damerauLevenshteinDistance("", "1"));
    assertEquals(1, StringUtilities.damerauLevenshteinDistance("1", ""));
    assertEquals(3, StringUtilities.damerauLevenshteinDistance("schill", "thrill"));
    assertEquals(2, StringUtilities.damerauLevenshteinDistance("abcdef", "bcdefa"));
    assertEquals(1, StringUtilities.damerauLevenshteinDistance("neat", "naet"));
  }

  @Test
  public void testRandomString() {
    Random random = new Random(42);
    Set<String> strings = new TreeSet<String>();
    for (int i = 0; i < 100000; i++) {
      String s = StringUtilities.getRandomString(random, 3, 9);
      strings.add(s);
    }

    for (String s : strings) {
      assertTrue(s.length() >= 3 && s.length() <= 9);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetBytesWithInvalidEncoding() {
    StringUtilities.getBytes("foo", "foo");
  }

  @Test
  public void testGetBytes() {
    assertArrayEquals(new byte[] {102, 111, 111}, StringUtilities.getBytes("foo", "UTF-8"));
  }

  @Test
  public void testGetUTF8Bytes() {
    assertArrayEquals(new byte[] {102, 111, 111}, StringUtilities.getUTF8Bytes("foo"));
  }

  @Test
  public void testGetBytesWithNull() {
    assertNull(null, StringUtilities.getBytes(null, "UTF-8"));
  }

  @Test
  public void testWildcard() {
    String name = "George Washington";
    assertTrue(name.matches(StringUtilities.wildcardToRegexString("*")));
    assertTrue(name.matches(StringUtilities.wildcardToRegexString("G*")));
    assertTrue(name.matches(StringUtilities.wildcardToRegexString("*on")));
    assertFalse(name.matches(StringUtilities.wildcardToRegexString("g*")));

    name = "com.acme.util.string";
    assertTrue(name.matches(StringUtilities.wildcardToRegexString("com.*")));
    assertTrue(name.matches(StringUtilities.wildcardToRegexString("com.*.util.string")));

    name = "com.acme.util.string";
    assertTrue(name.matches(StringUtilities.wildcardToRegexString("com.????.util.string")));
    assertFalse(name.matches(StringUtilities.wildcardToRegexString("com.??.util.string")));
  }

  @Test
  public void testCreateString() {
    assertEquals("foo", StringUtilities.createString(new byte[] {102, 111, 111}, "UTF-8"));
  }

  @Test
  public void testCreateUTF8String() {
    assertEquals("foo", StringUtilities.createUTF8String(new byte[] {102, 111, 111}));
  }

  @Test
  public void testCreateStringWithNull() {
    assertNull(null, StringUtilities.createString(null, "UTF-8"));
  }

  @Test
  public void testCreateStringWithEmptyArray() {
    assertEquals("", StringUtilities.createString(new byte[] {}, "UTF-8"));
  }

  @Test
  public void testCreateUTF8StringWithEmptyArray() {
    assertEquals("", StringUtilities.createUTF8String(new byte[] {}));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateStringWithInvalidEncoding() {
    StringUtilities.createString(new byte[] {102, 111, 111}, "baz");
  }

  @Test
  public void testCreateUtf8String() {
    assertEquals("foo", StringUtilities.createUtf8String(new byte[] {102, 111, 111}));
  }

  @Test
  public void testCreateUtf8StringWithNull() {
    assertNull(null, StringUtilities.createUtf8String(null));
  }

  @Test
  public void testCreateUtf8StringWithEmptyArray() {
    assertEquals("", StringUtilities.createUtf8String(new byte[] {}));
  }
}
