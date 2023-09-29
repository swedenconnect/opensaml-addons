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
package se.swedenconnect.opensaml.common.utils;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;

import net.shibboleth.shared.xml.SerializeSupport;

/**
 * Utilities for logging SAML messages.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class SamlLog {

  /**
   * Returns the given SAML object in its "pretty print" XML string form.
   *
   * @param <T> the type of object to "print"
   * @param object the object to display as a string
   * @return the XML as a string
   * @throws MarshallingException for marshalling errors
   */
  public static <T extends XMLObject> String toString(final T object) throws MarshallingException {
    return SerializeSupport.prettyPrintXML(XMLObjectSupport.marshall(object));
  }

  /**
   * The same as {@link #toString()} but the method never throws (returns the empty string instead). Useful for logging
   * statements.
   *
   * @param <T> the type of object to "print"
   * @param object the object to display as a string
   * @return the XML as a string
   */
  public static <T extends XMLObject> String toStringSafe(final T object) {
    try {
      return toString(object);
    }
    catch (Exception e) {
      return "";
    }
  }

  // Hidden
  private SamlLog() {
  }

}
