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
package se.swedenconnect.opensaml.saml2.metadata.scope.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.AbstractXMLObjectMarshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.w3c.dom.Element;

import net.shibboleth.shared.xml.ElementSupport;
import se.swedenconnect.opensaml.saml2.metadata.scope.Scope;

/**
 * Marshaller for the {@link Scope} element.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ScopeMarshaller extends AbstractXMLObjectMarshaller {

  /** {@inheritDoc} */
  @Override
  protected void marshallAttributes(final XMLObject xmlObject, final Element domElement) throws MarshallingException {
    final Scope scope = (Scope) xmlObject;
    if (scope.getRegexpXSBoolean() != null) {
      domElement.setAttributeNS(null, Scope.REGEXP_ATTRIB_NAME, scope.getRegexpXSBoolean().toString());
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void marshallElementContent(final XMLObject xmlObject, final Element domElement)
      throws MarshallingException {
    final Scope shibMDScope = (Scope) xmlObject;
    ElementSupport.appendTextContent(domElement, shibMDScope.getValue());
  }
}