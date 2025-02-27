/*
 * Copyright 2016-2025 Sweden Connect
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
import java.util.Map;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.ext.saml2mdui.Description;
import org.opensaml.saml.ext.saml2mdui.DisplayName;
import org.opensaml.saml.ext.saml2mdui.InformationURL;
import org.opensaml.saml.ext.saml2mdui.Keywords;
import org.opensaml.saml.ext.saml2mdui.Logo;
import org.opensaml.saml.ext.saml2mdui.PrivacyStatementURL;
import org.opensaml.saml.ext.saml2mdui.UIInfo;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;
import se.swedenconnect.opensaml.common.utils.LocalizedString;

/**
 * A builder for creating {@link UIInfo} objects.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class UIInfoBuilder extends AbstractSAMLObjectBuilder<UIInfo> {

  /**
   * Creates a builder instance.
   *
   * @return a builder instance
   */
  public static UIInfoBuilder builder() {
    return new UIInfoBuilder();
  }

  /** {@inheritDoc} */
  @Override
  protected Class<UIInfo> getObjectType() {
    return UIInfo.class;
  }

  /**
   * Assigns the display names.
   *
   * @param displayNames the names
   * @return the builder
   */
  public UIInfoBuilder displayNames(final List<LocalizedString> displayNames) {
    if (displayNames != null) {
      for (final LocalizedString s : displayNames) {
        final DisplayName dn = (DisplayName) XMLObjectSupport.buildXMLObject(DisplayName.DEFAULT_ELEMENT_NAME);
        dn.setValue(s.getLocalString());
        dn.setXMLLang(s.getLanguage());
        this.object().getDisplayNames().add(dn);
      }
    }
    return this;
  }

  /**
   * @see #displayNames(List)
   *
   * @param displayNames the names
   * @return the builder
   */
  public UIInfoBuilder displayNames(final LocalizedString... displayNames) {
    return this.displayNames(displayNames != null ? Arrays.asList(displayNames) : null);
  }

  /**
   * Assigns the keywords.
   *
   * @param keywords the keywords where the map keys are language tags
   * @return the builder
   */
  public UIInfoBuilder keywords(final Map<String, List<String>> keywords) {
    if (keywords != null) {
      for (final Map.Entry<String, List<String>> e : keywords.entrySet()) {
        final Keywords kw = (Keywords) XMLObjectSupport.buildXMLObject(Keywords.DEFAULT_ELEMENT_NAME);
        if (!e.getKey().isEmpty()) {
          kw.setXMLLang(e.getKey());
        }
        kw.setKeywords(e.getValue());
        this.object().getKeywords().add(kw);
      }
    }
    return this;
  }

  /**
   * Assigns a set of keywords that do not have the language tag.
   *
   * @param keywords the keywords
   * @return the builder
   */
  public UIInfoBuilder keywords(final List<String> keywords) {
    if (keywords != null) {
      final Map<String, List<String>> m = new HashMap<>();
      m.put("", keywords);
      return this.keywords(m);
    }
    else {
      return this;
    }
  }

  /**
   * @see #keywords(List)
   *
   * @param keywords the keywords
   * @return the builder
   */
  public UIInfoBuilder keywords(final String... keywords) {
    return this.keywords(keywords != null ? Arrays.asList(keywords) : null);
  }

  /**
   * Assigns the descriptions.
   *
   * @param descriptions the descriptions
   * @return the builder
   */
  public UIInfoBuilder descriptions(final List<LocalizedString> descriptions) {
    if (descriptions != null) {
      for (final LocalizedString s : descriptions) {
        final Description d = (Description) XMLObjectSupport.buildXMLObject(Description.DEFAULT_ELEMENT_NAME);
        d.setValue(s.getLocalString());
        d.setXMLLang(s.getLanguage());
        this.object().getDescriptions().add(d);
      }
    }
    return this;
  }

  /**
   * @see #descriptions(List)
   *
   * @param descriptions the descriptions
   * @return the builder
   */
  public UIInfoBuilder descriptions(final LocalizedString... descriptions) {
    return this.descriptions(descriptions != null ? Arrays.asList(descriptions) : null);
  }

  /**
   * Assigns the logotypes.
   *
   * @param logos the logos (will be cloned before assignment)
   * @return the builder
   */
  public UIInfoBuilder logos(final List<Logo> logos) {
    try {
      if (logos != null) {
        for (final Logo logo : logos) {
          this.object().getLogos().add(XMLObjectSupport.cloneXMLObject(logo));
        }
      }
      return this;
    }
    catch (final UnmarshallingException | MarshallingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see #logos(List)
   *
   * @param logos the logos (will be cloned before assignment)
   * @return the builder
   */
  public UIInfoBuilder logos(final Logo... logos) {
    return this.logos(logos != null ? Arrays.asList(logos) : null);
  }

  /**
   * Assigns the information URL:s.
   *
   * @param informationURLs the information URL:s
   * @return the builder
   */
  public UIInfoBuilder informationURLs(final List<LocalizedString> informationURLs) {
    if (informationURLs != null) {
      for (final LocalizedString u : informationURLs) {
        final InformationURL url =
            (InformationURL) XMLObjectSupport.buildXMLObject(InformationURL.DEFAULT_ELEMENT_NAME);
        url.setURI(u.getLocalString());
        url.setXMLLang(u.getLanguage());
        this.object().getInformationURLs().add(url);
      }
    }
    return this;
  }

  /**
   * @see #informationURLs(List)
   *
   * @param informationURLs the information URL:s
   * @return the builder
   */
  public UIInfoBuilder informationURLs(final LocalizedString... informationURLs) {
    return this.informationURLs(informationURLs != null ? Arrays.asList(informationURLs) : null);
  }

  /**
   * Assigns the privacy statement URL:s.
   *
   * @param privacyStatementURLs the URL:s
   * @return the builder
   */
  public UIInfoBuilder privacyStatementURLs(final List<LocalizedString> privacyStatementURLs) {
    if (privacyStatementURLs != null) {
      for (final LocalizedString u : privacyStatementURLs) {
        final PrivacyStatementURL url =
            (PrivacyStatementURL) XMLObjectSupport.buildXMLObject(PrivacyStatementURL.DEFAULT_ELEMENT_NAME);
        url.setURI(u.getLocalString());
        url.setXMLLang(u.getLanguage());
        this.object().getPrivacyStatementURLs().add(url);
      }
    }
    return this;
  }

  /**
   * @see #privacyStatementURLs(List)
   *
   * @param privacyStatementURLs the URL:s
   * @return the builder
   */
  public UIInfoBuilder privacyStatementURLs(final LocalizedString... privacyStatementURLs) {
    return this.privacyStatementURLs(privacyStatementURLs != null ? Arrays.asList(privacyStatementURLs) : null);
  }

}
