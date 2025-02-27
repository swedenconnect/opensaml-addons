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
package se.swedenconnect.opensaml.saml2.metadata.scope.impl;

import jakarta.annotation.Nonnull;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectUnmarshaller;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;
import se.swedenconnect.opensaml.saml2.metadata.scope.Scope;

/**
 * Unmarshaller for the {@link Scope} element.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ScopeUnmarshaller extends AbstractXMLObjectUnmarshaller {

  /** {@inheritDoc} */
  @Override
  protected void processAttribute(@Nonnull final XMLObject xmlObject, @Nonnull final Attr attribute)
      throws UnmarshallingException {
    final Scope scope = (Scope) xmlObject;
    if (attribute.getLocalName().equals(Scope.REGEXP_ATTRIB_NAME)) {
      scope.setRegexp(Boolean.valueOf(attribute.getValue()));
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void processElementContent(@Nonnull final XMLObject xmlObject, @Nonnull final String elementContent) {
    final Scope scope = (Scope) xmlObject;
    scope.setValue(elementContent);
  }

}
