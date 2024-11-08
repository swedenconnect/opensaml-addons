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
package se.swedenconnect.opensaml.saml2.metadata.scope.impl;

import java.util.List;

import org.opensaml.core.xml.AbstractXMLObject;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSBooleanValue;

import se.swedenconnect.opensaml.saml2.metadata.scope.Scope;

/**
 * Implementation of the {@link Scope} element.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ScopeImpl extends AbstractXMLObject implements Scope {

  /** The regexp attribute value. */
  private XSBooleanValue regexp;

  /** The string content value. */
  private String scopeValue;

  /**
   * Constructor.
   *
   * @param namespaceURI the namespace the element is in
   * @param elementLocalName the local name of the XML element this Object represents
   * @param namespacePrefix the prefix for the given namespace
   */
  protected ScopeImpl(final String namespaceURI, final String elementLocalName, final String namespacePrefix) {
    super(namespaceURI, elementLocalName, namespacePrefix);
    this.regexp = null;
  }

  /** {@inheritDoc} */
  @Override
  public Boolean getRegexp() {
    return this.regexp != null ? this.regexp.getValue() : Boolean.FALSE;
  }

  /** {@inheritDoc} */
  @Override
  public void setRegexp(final Boolean newRegexp) {
    this.regexp = newRegexp != null
        ? this.prepareForAssignment(this.regexp, new XSBooleanValue(newRegexp, false))
        : this.prepareForAssignment(this.regexp, null);
  }

  /** {@inheritDoc} */
  @Override
  public XSBooleanValue getRegexpXSBoolean() {
    return this.regexp;
  }

  /** {@inheritDoc} */
  @Override
  public void setRegexp(final XSBooleanValue newRegexp) {
    this.regexp = this.prepareForAssignment(this.regexp, newRegexp);
  }

  /** {@inheritDoc} */
  @Override
  public String getValue() {
    return this.scopeValue;
  }

  /** {@inheritDoc} */
  @Override
  public void setValue(final String newScopeValue) {
    this.scopeValue = this.prepareForAssignment(this.scopeValue, newScopeValue);
  }

  /** {@inheritDoc} */
  @Override
  public List<XMLObject> getOrderedChildren() {
    return null;
  }
}
