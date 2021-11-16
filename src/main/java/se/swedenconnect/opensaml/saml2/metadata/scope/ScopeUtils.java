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
package se.swedenconnect.opensaml.saml2.metadata.scope;

import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLRuntimeException;

/**
 * Utility methods for validating a scoped attribute against a {@code shibmd:Scope} element.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ScopeUtils {

  /**
   * Given a {@code shibmd:Scope} element, the method tests whether the value of the (scoped) attribute matches the
   * scope.
   *
   * @param scope
   *          the Scope element
   * @param attributeValue
   *          the full attribute value
   * @return true if there is a match and false otherwise
   */
  public static boolean isMatch(final XMLObject scope, final String attributeValue) {
    if (StringUtils.isBlank(attributeValue)) {
      return false;
    }
    final String[] attributeParts = attributeValue.split("@", 2);
    if (attributeParts.length == 1) {
      // Not a scoped attribute
      return false;
    }
    final String domainValue = attributeParts[1];

    boolean isRegexp = false;
    String scopeValue = null;

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

  // Hidden
  private ScopeUtils() {
  }

}
