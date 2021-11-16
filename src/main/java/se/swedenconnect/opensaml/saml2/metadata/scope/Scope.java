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

import javax.xml.namespace.QName;

import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.core.xml.schema.XSString;

/** 
 * XMLObject for the Shibboleth Scope metadata extension. 
 * */

/**
 * The Shibboleth Scope metadata extension.
 * <p>
 * Note that this class is also defined in Shibboleth's idp-saml-api and idp-saml-impl libraries. Only if Shibboleth is
 * not on the classpath will the marshaller and unmarshaller for Scope be loaded.
 * </p>
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public interface Scope extends XSString {

  /** Element local name. */
  String DEFAULT_ELEMENT_LOCAL_NAME = "Scope";

  /** Default element name. */
  QName DEFAULT_ELEMENT_NAME = new QName("urn:mace:shibboleth:metadata:1.0", DEFAULT_ELEMENT_LOCAL_NAME, "shibmd");

  /** The regexp attribute name. */
  String REGEXP_ATTRIB_NAME = "regexp";

  /**
   * Gets the {@code regexp} attribute value.
   * 
   * @return the regexp attribute value
   */
  Boolean getRegexp();

  /**
   * Gets the {@code regexp} attribute value.
   * 
   * @return the regexp attribute value
   */
  XSBooleanValue getRegexpXSBoolean();

  /**
   * Sets the {@code regexp} attribute value.
   * 
   * @param newRegexp
   *          the new regexp attribute value
   */
  void setRegexp(final Boolean newRegexp);

  /**
   * Set the {@code regexp} attribute value.
   * 
   * @param newRegexp
   *          the new regexp attribute value
   */
  void setRegexp(final XSBooleanValue newRegexp);

}
