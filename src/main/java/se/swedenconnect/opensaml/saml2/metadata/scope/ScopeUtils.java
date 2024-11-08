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
package se.swedenconnect.opensaml.saml2.metadata.scope;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLRuntimeException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import se.swedenconnect.opensaml.saml2.attribute.AttributeUtils;
import se.swedenconnect.opensaml.saml2.metadata.EntityDescriptorUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.PatternSyntaxException;

/**
 * Utility methods for validating a scoped attribute against a {@code shibmd:Scope} element.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ScopeUtils {

  /**
   * Given an (IdP) {@link EntityDescriptor}, the method finds all {@code shibmd:Scope} elements.
   *
   * @param entityDescriptor the metadata object
   * @return a (possible empty) list of {@code shibmd:Scope} elements
   */
  public static List<XMLObject> getScopeExtensions(final EntityDescriptor entityDescriptor) {
    return Optional.ofNullable(entityDescriptor.getIDPSSODescriptor(SAMLConstants.SAML20P_NS))
        .map(d -> EntityDescriptorUtils.getMetadataExtensions(d.getExtensions(), Scope.DEFAULT_ELEMENT_NAME))
        .orElse(Collections.emptyList());
  }

  /**
   * Predicate that tells if a scoped attribute is "authorized", i.e., if its scope is listed in the supplied list of
   * {@code shibmd:Scope} elements (gotten from the IdP metadata).
   * <p>
   * If an attribute that is not "scoped" (value@scope) the method returns {@code false}.
   * </p>
   *
   * @param scopedAttribute the attribute to test
   * @param scopes the shibmd:Scope elements
   * @return true if the attribute scope is listed among the Scope extensions and false otherwise
   */
  public static boolean isAuthorized(final Attribute scopedAttribute, final List<XMLObject> scopes) {
    return scopes.stream()
        .anyMatch(s -> isMatch(s, scopedAttribute));
  }

  /**
   * Given a {@code shibmd:Scope} element, the method tests whether the value of the (scoped) attribute matches the
   * scope.
   * <p>
   * If the attribute contains multiple values, all must match the scope.
   * </p>
   *
   * @param scope the Scope element
   * @param attribute the attribute
   * @return true if there is a match and false otherwise
   */
  public static boolean isMatch(final XMLObject scope, final Attribute attribute) {
    if (attribute == null) {
      return false;
    }
    final List<String> attributeValues = AttributeUtils.getAttributeStringValues(attribute);
    for (final String a : attributeValues) {
      if (!isMatch(scope, a)) {
        return false;
      }
    }
    return !attributeValues.isEmpty();
  }

  /**
   * Given a {@code shibmd:Scope} element, the method tests whether the value of the (scoped) attribute matches the
   * scope.
   *
   * @param scope the Scope element
   * @param attributeValue the full attribute value
   * @return true if there is a match and false otherwise
   */
  public static boolean isMatch(final XMLObject scope, final String attributeValue) {
    if (StringUtils.isBlank(attributeValue)) {
      return false;
    }
    final String domainValue = getScopedDomain(attributeValue);
    if (domainValue == null) {
      // Not a scoped attribute
      return false;
    }

    final boolean isRegexp;
    final String scopeValue;

    if (scope instanceof Scope) {
      isRegexp = ((Scope) scope).getRegexp();
      scopeValue = ((Scope) scope).getValue();
    }
    else {
      try {
        isRegexp = (Boolean) scope.getClass().getDeclaredMethod("getRegexp").invoke(scope);
        scopeValue = (String) scope.getClass().getDeclaredMethod("getValue").invoke(scope);
      }
      catch (final Exception e) {
        throw new XMLRuntimeException("Not a valid Scope object", e);
      }
    }

    if (!isRegexp) {
      return domainValue.equals(scopeValue);
    }
    else {
      try {
        return domainValue.matches(scopeValue);
      }
      catch (final PatternSyntaxException e) {
        return false;
      }
    }
  }

  /**
   * Gets the domain part (value@domain) from a scoped attribute value.
   *
   * @param attributeValue the attribute value
   * @return the domain part, or null
   */
  public static String getScopedDomain(final String attributeValue) {
    final String[] attributeParts = attributeValue.split("@", 2);
    if (attributeParts.length == 1) {
      // Not a scoped attribute
      return null;
    }
    return attributeParts[1];
  }

  // Hidden
  private ScopeUtils() {
  }

}
