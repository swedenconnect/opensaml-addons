/*
 * Copyright 2016-2024 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.swedenconnect.opensaml.common.utils;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link LocalizedString}.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class LocalizedStringTest {

  @Test
  public void testDefaultConstructor() {
    final LocalizedString ls = new LocalizedString();
    Assertions.assertEquals(LocalizedString.DEFAULT_LANGUAGE_TAG, ls.getLanguage());
    Assertions.assertNull(ls.getLocalString());
  }

  @Test
  public void testParse() {
    final LocalizedString ls = new LocalizedString("sv-Hejsan");
    Assertions.assertEquals("sv", ls.getLanguage());
    Assertions.assertEquals("Hejsan", ls.getLocalString());

    final LocalizedString ls2 = new LocalizedString("sv-");
    Assertions.assertEquals("sv", ls2.getLanguage());
    Assertions.assertEquals("", ls2.getLocalString());
  }

  @Test
  public void testParseError1() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new LocalizedString("");
    });
  }

  @Test
  public void testParseError2() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new LocalizedString("sv+Hejsan");
    });
  }

  @Test
  public void testParseError3() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new LocalizedString("english-Hello");
    });
  }

  @Test
  public void testSetLocale() {
    final LocalizedString ls = new LocalizedString("Guten tag", Locale.GERMANY);
    Assertions.assertEquals("de", ls.getLanguage());
    Assertions.assertEquals("Guten tag", ls.getLocalString());

    final LocalizedString ls2 = new LocalizedString("Hello", (Locale) null);
    Assertions.assertEquals(Locale.ENGLISH.getLanguage(), ls2.getLanguage());
    Assertions.assertEquals("Hello", ls2.getLocalString());
  }

}
