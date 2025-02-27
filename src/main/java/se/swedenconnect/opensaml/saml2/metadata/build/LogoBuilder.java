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

import org.opensaml.saml.ext.saml2mdui.Logo;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;

/**
 * A builder for {@code mdui:Logo} elements.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class LogoBuilder extends AbstractSAMLObjectBuilder<Logo> {

  /**
   * Creates a new {@code LogoBuilder} instance.
   *
   * @return a LogoBuilder instance
   */
  public static LogoBuilder builder() {
    return new LogoBuilder();
  }

  /**
   * Utility method that builds a {@code mdui:Logo} object.
   *
   * @param url the Logo URL
   * @param height the height
   * @param width the width
   * @return a {@code Logo} instance
   */
  public static Logo logo(final String url, final Integer height, final Integer width) {
    return builder()
        .url(url)
        .height(height)
        .width(width)
        .build();
  }

  /**
   * Utility method that builds a {@code mdui:Logo} object.
   *
   * @param url the Logo URL
   * @param language the language
   * @param height the height
   * @param width the width
   * @return a Logo instance
   */
  public static Logo logo(final String url, final String language, final Integer height, final Integer width) {
    return builder()
        .url(url)
        .language(language)
        .height(height)
        .width(width)
        .build();
  }

  /** {@inheritDoc} */
  @Override
  protected Class<Logo> getObjectType() {
    return Logo.class;
  }

  /**
   * Assigns the URL of the {@code Logo}.
   *
   * @param url the URL
   * @return the builder
   */
  public LogoBuilder url(final String url) {
    this.object().setURI(url);
    return this;
  }

  /**
   * Assigns the language tag of the {@code Logo}.
   *
   * @param language the language tag
   * @return the builder
   */
  public LogoBuilder language(final String language) {
    this.object().setXMLLang(language);
    return this;
  }

  /**
   * Assigns the height of the {@code Logo}.
   *
   * @param height the height (in pixels)
   * @return the builder
   */
  public LogoBuilder height(final Integer height) {
    this.object().setHeight(height);
    return this;
  }

  /**
   * Assigns the width of the {@code Logo}.
   *
   * @param width the width (in pixels)
   * @return the builder
   */
  public LogoBuilder width(final Integer width) {
    this.object().setWidth(width);
    return this;
  }

}
