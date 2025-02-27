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
package se.swedenconnect.opensaml.saml2.metadata.build;

import org.opensaml.saml.saml2.metadata.RequestedAttribute;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;

/**
 * Builder for {@code md:RequestedAttribute} elements.
 * <p>
 * It is valid to add a value to a requested attribute but this rarely happens so this builder does not support that.
 * </p>
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class RequestedAttributeBuilder extends AbstractSAMLObjectBuilder<RequestedAttribute> {

  /**
   * Constructor setting the attribute name.
   *
   * @param name the attribute name
   */
  public RequestedAttributeBuilder(final String name) {
    super();
    if (name == null) {
      throw new IllegalArgumentException("name must not be null");
    }
    this.object().setName(name);
  }

  /**
   * Creates a builder.
   *
   * @param name the attribute name
   * @return a builder
   */
  public static RequestedAttributeBuilder builder(final String name) {
    return new RequestedAttributeBuilder(name);
  }

  /**
   * Assigns the attribute friendly name.
   *
   * @param friendlyName the friendly name
   * @return the builder
   */
  public RequestedAttributeBuilder friendlyName(final String friendlyName) {
    this.object().setFriendlyName(friendlyName);
    return this;
  }

  /**
   * Assigns the attribute name format.
   *
   * @param nameFormat the name format URI
   * @return the builder
   */
  public RequestedAttributeBuilder nameFormat(final String nameFormat) {
    this.object().setNameFormat(nameFormat);
    return this;
  }

  /**
   * Assigns the {@code isRequired} attribute value.
   *
   * @param required flag
   * @return the builder
   */
  public RequestedAttributeBuilder isRequired(final Boolean required) {
    this.object().setIsRequired(required);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  protected Class<RequestedAttribute> getObjectType() {
    return RequestedAttribute.class;
  }

}
