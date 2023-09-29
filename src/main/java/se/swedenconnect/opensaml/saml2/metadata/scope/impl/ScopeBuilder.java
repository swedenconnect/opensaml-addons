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

import org.opensaml.core.xml.AbstractXMLObjectBuilder;

import se.swedenconnect.opensaml.saml2.metadata.scope.Scope;

/**
 * Builder for {@link Scope} elements.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ScopeBuilder extends AbstractXMLObjectBuilder<Scope> {

  /** {@inheritDoc} */
  @Override
  public Scope buildObject(final String namespaceURI, final String localName, final String namespacePrefix) {
    return new ScopeImpl(namespaceURI, localName, namespacePrefix);
  }

  /**
   * Builds a {@code Scope} element with the default namespace prefix and element name.
   *
   * @return a Scope object
   */
  public Scope buildObject() {
    return buildObject("urn:mace:shibboleth:metadata:1.0", Scope.DEFAULT_ELEMENT_LOCAL_NAME, "shibmd");
  }
}