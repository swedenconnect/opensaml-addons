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
package se.swedenconnect.opensaml.saml2.metadata.provider;

import java.util.function.Predicate;

import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

/**
 * A number of predicates that may be installed as filters for a metadata provider.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class MetadataProviderPredicates {

  /**
   * Returns a predicate that evaluates to {@code true} if the supplied entity descriptor represents an IdP.
   *
   * @return predicate for filtering IdPs
   */
  public static Predicate<EntityDescriptor> includeOnlyIDPs() {
    return MetadataProviderPredicates::isIDP;
  }

  /**
   * Returns a predicate that evaluates to {@code true} if the supplied entity descriptor represents an IdP or if the
   * entity descriptor is "my" entry (typically the SP).
   *
   * @param entityID the entityID for the entity descriptor to include even if it's not an IdP
   * @return predicate for filtering IdPs and "my" entity
   */
  public static Predicate<EntityDescriptor> includeOnlyIDPsAndMe(final String entityID) {
    return e -> entityID.equals(e.getEntityID()) || isIDP(e);
  }

  /**
   * Returns a predicate that evaluates to {@code true} if the supplied entity descriptor represents an SP.
   *
   * @return predicate for filtering SPs
   */
  public static Predicate<EntityDescriptor> includeOnlySPs() {
    return e -> !e.getRoleDescriptors(SPSSODescriptor.DEFAULT_ELEMENT_NAME).isEmpty();
  }

  /**
   * Tells whether the supplied entity descriptor is an IdP.
   *
   * @param ed the entity descriptor to test
   * @return {@code true} if the entity descriptor represents an IdP and {@code false} otherwise
   */
  public static boolean isIDP(final EntityDescriptor ed) {
    return !ed.getRoleDescriptors(IDPSSODescriptor.DEFAULT_ELEMENT_NAME).isEmpty();
  }

  /**
   * Tells whether the supplied entity descriptor is an SP.
   *
   * @param ed the entity descriptor to test
   * @return {@code true} if the entity descriptor represents an SP and {@code false} otherwise
   */
  public static boolean isSP(final EntityDescriptor ed) {
    return !ed.getRoleDescriptors(SPSSODescriptor.DEFAULT_ELEMENT_NAME).isEmpty();
  }

  // Hidden constructor.
  private MetadataProviderPredicates() {
  }

}
