/*
 * Copyright 2016-2021 Sweden Connect
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

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for {@link LocalizedString}.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public class LocalizedStringTest {

  @Test
  public void testDefaultConstructor() {
    final LocalizedString ls = new LocalizedString();
    Assert.assertEquals(LocalizedString.DEFAULT_LANGUAGE_TAG, ls.getLanguage());
    Assert.assertNull(ls.getLocalString());
  }
  
  @Test
  public void testParse() {
    final LocalizedString ls = new LocalizedString("sv-Hejsan");
    Assert.assertEquals("sv", ls.getLanguage());
    Assert.assertEquals("Hejsan", ls.getLocalString());
    
    final LocalizedString ls2 = new LocalizedString("sv-");
    Assert.assertEquals("sv", ls2.getLanguage());
    Assert.assertEquals("", ls2.getLocalString());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testParseError1() {
    new LocalizedString("");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testParseError2() {
    new LocalizedString("sv+Hejsan");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testParseError3() {
    new LocalizedString("english-Hello");
  }
  
  @Test
  public void testSetLocale() {
    final LocalizedString ls = new LocalizedString("Guten tag", Locale.GERMANY);
    Assert.assertEquals("de", ls.getLanguage());
    Assert.assertEquals("Guten tag", ls.getLocalString());
    
    final LocalizedString ls2 = new LocalizedString("Hello", (Locale)null);
    Assert.assertEquals(Locale.ENGLISH.getLanguage(), ls2.getLanguage());
    Assert.assertEquals("Hello", ls2.getLocalString());
  }

}
