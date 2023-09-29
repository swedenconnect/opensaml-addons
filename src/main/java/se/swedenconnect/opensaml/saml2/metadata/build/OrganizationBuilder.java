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
import java.util.List;

import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml.saml2.metadata.OrganizationName;
import org.opensaml.saml.saml2.metadata.OrganizationURL;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;
import se.swedenconnect.opensaml.common.utils.LocalizedString;

/**
 * A builder for {@code Organization} elements.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class OrganizationBuilder extends AbstractSAMLObjectBuilder<Organization> {

  /**
   * Utility method that creates a builder.
   *
   * @return a builder
   */
  public static OrganizationBuilder builder() {
    return new OrganizationBuilder();
  }

  /** {@inheritDoc} */
  @Override
  protected Class<Organization> getObjectType() {
    return Organization.class;
  }

  /**
   * Assigns the {@code OrganizationName} elements.
   *
   * @param organizationNames the names
   * @return the builder
   */
  public OrganizationBuilder organizationNames(final List<LocalizedString> organizationNames) {
    if (organizationNames != null) {
      for (final LocalizedString s : organizationNames) {
        final OrganizationName on =
            (OrganizationName) XMLObjectSupport.buildXMLObject(OrganizationName.DEFAULT_ELEMENT_NAME);
        on.setValue(s.getLocalString());
        on.setXMLLang(s.getLanguage());
        this.object().getOrganizationNames().add(on);
      }
    }
    return this;
  }

  /**
   * @see #organizationNames(List)
   *
   * @param organizationNames the names
   * @return the builder
   */
  public OrganizationBuilder organizationNames(final LocalizedString... organizationNames) {
    return this.organizationNames(organizationNames != null ? Arrays.asList(organizationNames) : null);
  }

  /**
   * Assigns the {@code OrganizationDisplayName} elements.
   *
   * @param organizationDisplayNames the names
   * @return the builder
   */
  public OrganizationBuilder organizationDisplayNames(final List<LocalizedString> organizationDisplayNames) {
    if (organizationDisplayNames != null) {
      for (final LocalizedString s : organizationDisplayNames) {
        final OrganizationDisplayName on =
            (OrganizationDisplayName) XMLObjectSupport.buildXMLObject(OrganizationDisplayName.DEFAULT_ELEMENT_NAME);
        on.setValue(s.getLocalString());
        on.setXMLLang(s.getLanguage());
        this.object().getDisplayNames().add(on);
      }
    }
    return this;
  }

  /**
   * @see #organizationDisplayNames(List)
   *
   * @param organizationDisplayNames the names
   * @return the builder
   */
  public OrganizationBuilder organizationDisplayNames(final LocalizedString... organizationDisplayNames) {
    return this
        .organizationDisplayNames(organizationDisplayNames != null ? Arrays.asList(organizationDisplayNames) : null);
  }

  /**
   * Assigns the {@code OrganizationURL} elements.
   *
   * @param organizationURLs the URLs
   * @return the builder
   */
  public OrganizationBuilder organizationURLs(final List<LocalizedString> organizationURLs) {
    if (organizationURLs != null) {
      for (final LocalizedString s : organizationURLs) {
        final OrganizationURL on =
            (OrganizationURL) XMLObjectSupport.buildXMLObject(OrganizationURL.DEFAULT_ELEMENT_NAME);
        on.setURI(s.getLocalString());
        on.setXMLLang(s.getLanguage());
        this.object().getURLs().add(on);
      }
    }
    return this;
  }

  /**
   * @see #organizationURLs(List)
   *
   * @param organizationURLs the URLs
   * @return the builder
   */
  public OrganizationBuilder organizationURLs(final LocalizedString... organizationURLs) {
    return this.organizationURLs(organizationURLs != null ? Arrays.asList(organizationURLs) : null);
  }

}
