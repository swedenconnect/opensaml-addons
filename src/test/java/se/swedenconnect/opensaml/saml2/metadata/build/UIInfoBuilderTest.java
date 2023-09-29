/*
 * Copyright 2016-2023 Sweden Connect
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
package se.swedenconnect.opensaml.saml2.metadata.build;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.ext.saml2mdui.Keywords;
import org.opensaml.saml.ext.saml2mdui.Logo;
import org.opensaml.saml.ext.saml2mdui.UIInfo;

import com.google.common.base.Objects;

import se.swedenconnect.opensaml.OpenSAMLTestBase;
import se.swedenconnect.opensaml.common.utils.LocalizedString;

/**
 * Test cases for {@code UIInfoBuilder}.
 *
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
public class UIInfoBuilderTest extends OpenSAMLTestBase {

  public static final LocalizedString[] DISPLAY_NAMES = new LocalizedString[] {
      new LocalizedString("Litsec legitimering", "sv"),
      new LocalizedString("Litsec authentication", Locale.ENGLISH) };

  public static Map<String, List<String>> KEYWORDS;

  public static final LocalizedString[] DESCRIPTIONS = new LocalizedString[] {
      new LocalizedString("Litsecs tjänst för legitimering", "sv"),
      new LocalizedString("Litsec service for authentication", "en") };

  public static final Logo[] LOGOS = new Logo[] {
      createLogo("http://www.litsec.se/logo-sv.jpg", "sv", 16, 16),
      createLogo("http://www.litsec.se/logo-sv-large.jpg", "sv", 200, 200),
      createLogo("http://www.litsec.se/logo-en.jpg", "en", 16, 16) };

  public static final LocalizedString[] INFORMATION_URLS = new LocalizedString[] {
      new LocalizedString("http://www.litsec.se", "sv"),
      new LocalizedString("http://www.litsec.se/index-en.html", Locale.ENGLISH) };

  public static final LocalizedString[] PRIVACY_STATEMENT_URLS = new LocalizedString[] {
      new LocalizedString("http://www.litsec.se/privacy.html", "sv"),
      new LocalizedString("http://www.litsec.se/privacy-en.html", Locale.ENGLISH) };

  static {
    KEYWORDS = new HashMap<String, List<String>>();
    KEYWORDS.put(Locale.ENGLISH.getLanguage(), Arrays.asList("authentication", "SAML", "two-factor"));
    KEYWORDS.put("sv", Arrays.asList("legitimering"));
  }

  /**
   * Tests creating a full UIINfo with all elements set.
   *
   * @throws Exception
   *           for errors
   */
  @Test
  public void testCreateFull() throws Exception {

    UIInfo uiInfo = (new UIInfoBuilder())
      .displayNames(DISPLAY_NAMES)
      .keywords(KEYWORDS)
      .descriptions(DESCRIPTIONS)
      .logos(LOGOS)
      .informationURLs(INFORMATION_URLS)
      .privacyStatementURLs(PRIVACY_STATEMENT_URLS)
      .build();

    assertUIInfo(uiInfo, DISPLAY_NAMES, KEYWORDS, null, DESCRIPTIONS, LOGOS, INFORMATION_URLS, PRIVACY_STATEMENT_URLS);
  }

  /**
   * Test where not all elements and attributes are set.
   *
   * @throws Exception
   *           for errors
   */
  @Test
  public void testSimple() throws Exception {

    LocalizedString[] displayNames = new LocalizedString[] {
        new LocalizedString("Litsec legitimering", "sv"),
        new LocalizedString("Litsec authentication", Locale.ENGLISH) };

    String[] keywords = new String[] { "authentication", "SAML", "two-factor" };

    LocalizedString description = new LocalizedString("Litsecs tjänst för legitimering", "sv");

    Logo[] logos = new Logo[] {
        createLogo("http://www.litsec.se/logo-sv.jpg", null, 16, 16),
        createLogo("http://www.litsec.se/logo-sv-large.jpg", null, 200, 200) };

    LocalizedString informationURL = new LocalizedString("http://www.litsec.se", "sv");

    UIInfo uiInfo = (new UIInfoBuilder())
      .displayNames(displayNames)
      .keywords(keywords)
      .descriptions(description)
      .logos(logos)
      .informationURLs(informationURL)
      .build();

    assertUIInfo(uiInfo, displayNames, null, keywords, new LocalizedString[] { description }, logos, new LocalizedString[] {
        informationURL }, null);
  }

  public static Logo createLogo(String url, String language, Integer height, Integer width) {
    Logo logo = (Logo) XMLObjectSupport.buildXMLObject(Logo.DEFAULT_ELEMENT_NAME);
    logo.setURI(url);
    logo.setXMLLang(language);
    logo.setHeight(height);
    logo.setWidth(width);
    return logo;
  }

  public static void assertUIInfo(UIInfo uiInfo, LocalizedString[] displayNames, Map<String, List<String>> keywords, String[] keywordsArray,
      LocalizedString[] descriptions, Logo[] logos, LocalizedString[] informationURLs, LocalizedString[] privayStatementURLs) {

    Assertions.assertEquals(displayNames != null ? displayNames.length : 0, uiInfo.getDisplayNames().size());
    for (int i = 0; i < uiInfo.getDisplayNames().size(); i++) {
      Assertions.assertEquals(displayNames[i].getLanguage(), uiInfo.getDisplayNames().get(i).getXMLLang());
      Assertions.assertEquals(displayNames[i].getLocalString(), uiInfo.getDisplayNames().get(i).getValue());
    }

    if (keywords != null) {
      Assertions.assertEquals(keywords.keySet().size(), uiInfo.getKeywords().size());
      for (Keywords kw : uiInfo.getKeywords()) {
        List<String> words = keywords.get(kw.getXMLLang());
        Assertions.assertNotNull(words);
        Assertions.assertEquals(words, kw.getKeywords());
      }
    }
    if (keywordsArray != null) {
      Assertions.assertEquals(1, uiInfo.getKeywords().size());
      Assertions.assertNull(uiInfo.getKeywords().get(0).getXMLLang());
      Assertions.assertEquals(Arrays.asList(keywordsArray), uiInfo.getKeywords().get(0).getKeywords());
    }

    Assertions.assertEquals(descriptions != null ? descriptions.length : 0, uiInfo.getDescriptions().size());
    for (int i = 0; i < uiInfo.getDescriptions().size(); i++) {
      Assertions.assertEquals(descriptions[i].getLanguage(), uiInfo.getDescriptions().get(i).getXMLLang());
      Assertions.assertEquals(descriptions[i].getLocalString(), uiInfo.getDescriptions().get(i).getValue());
    }

    Assertions.assertEquals(logos != null ? logos.length : 0, uiInfo.getLogos().size());
    for (int i = 0; i < uiInfo.getLogos().size(); i++) {
      Assertions.assertTrue(equals(logos[i], uiInfo.getLogos().get(i)));
    }

    Assertions.assertEquals(informationURLs != null ? informationURLs.length : 0, uiInfo.getInformationURLs().size());
    for (int i = 0; i < uiInfo.getInformationURLs().size(); i++) {
      Assertions.assertEquals(informationURLs[i].getLanguage(), uiInfo.getInformationURLs().get(i).getXMLLang());
      Assertions.assertEquals(informationURLs[i].getLocalString(), uiInfo.getInformationURLs().get(i).getURI());
    }

    Assertions.assertEquals(privayStatementURLs != null ? privayStatementURLs.length : 0, uiInfo.getPrivacyStatementURLs().size());
    for (int i = 0; i < uiInfo.getPrivacyStatementURLs().size(); i++) {
      Assertions.assertEquals(privayStatementURLs[i].getLanguage(), uiInfo.getPrivacyStatementURLs().get(i).getXMLLang());
      Assertions.assertEquals(privayStatementURLs[i].getLocalString(), uiInfo.getPrivacyStatementURLs().get(i).getURI());
    }
  }

  public static boolean equals(final Logo l1, final Logo l2) {
    if (l1 == null && l2 == null) {
      return true;
    }
    if ((l1 == null && l2 != null) || (l1 != null && l2 == null)) {
      return false;
    }
    return Objects.equal(l1.getURI(), l2.getURI()) || Objects.equal(l1.getXMLLang(), l2.getXMLLang())
        || Objects.equal(l1.getHeight(), l2.getHeight()) || Objects.equal(l1.getWidth(), l2.getWidth());
  }
}
