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

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;
import se.swedenconnect.opensaml.saml2.metadata.HolderOfKeyMetadataSupport;

/**
 * A builder for {@code AssertionConsumerService} elements.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class AssertionConsumerServiceBuilder extends AbstractSAMLObjectBuilder<AssertionConsumerService> {

  /**
   * Utility method that creates a builder.
   *
   * @return a builder
   */
  public static AssertionConsumerServiceBuilder builder() {
    return new AssertionConsumerServiceBuilder();
  }

  /** {@inheritDoc} */
  @Override
  protected Class<AssertionConsumerService> getObjectType() {
    return AssertionConsumerService.class;
  }

  /**
   * Assigns the location URI.
   *
   * @param location the URI
   * @return the builder
   */
  public AssertionConsumerServiceBuilder location(final String location) {
    this.object().setLocation(location);
    return this;
  }

  /**
   * Assigns the binding of the service
   *
   * @param binding the binding URI
   * @return the builder
   * @see #postBinding()
   * @see #redirectBinding()
   * @see #hokPostBinding()
   * @see #hokRedirectBinding()
   */
  public AssertionConsumerServiceBuilder binding(final String binding) {
    this.object().setBinding(binding);
    return this;
  }

  /**
   * Shortcut for assigning the SAML POST binding to the service.
   *
   * @return the builder
   * @see #binding(String)
   */
  public AssertionConsumerServiceBuilder postBinding() {
    return this.binding(SAMLConstants.SAML2_POST_BINDING_URI);
  }

  /**
   * Shortcut for assigning the SAML Redirect binding to the service.
   *
   * @return the builder
   * @see #binding(String)
   */
  public AssertionConsumerServiceBuilder redirectBinding() {
    return this.binding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
  }

  /**
   * If the SAML holder-of-key profile is used, the {@code Binding} attribute should be assigned
   * {@value HolderOfKeyMetadataSupport#HOK_WEBSSO_PROFILE_URI} and the actual binding should be assigned to the
   * {@code hoksso:ProtocolBinding} attribute. This method sets the {@code hoksso:ProtocolBinding} attribute to the
   * given binding.
   *
   * @param binding the binding URI
   * @return this builder
   * @see #hokPostBinding()
   * @see #hokRedirectBinding()
   */
  public AssertionConsumerServiceBuilder protocolBinding(final String binding) {
    this.object().getUnknownAttributes().put(HolderOfKeyMetadataSupport.HOK_PROTOCOL_BINDING_ATTRIBUTE, binding);
    return this;
  }

  /**
   * Shortcut to assign the {@code Binding} attribute to Holder-of-key and the {@code hoksso:ProtocolBinding} attribute
   * to the POST binding.
   *
   * @return this builder
   */
  public AssertionConsumerServiceBuilder hokPostBinding() {
    return this.binding(HolderOfKeyMetadataSupport.HOK_WEBSSO_PROFILE_URI)
        .protocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
  }

  /**
   * Shortcut to assign the {@code Binding} attribute to Holder-of-key and the {@code hoksso:ProtocolBinding} attribute
   * to the Redirect binding.
   *
   * @return this builder
   */
  public AssertionConsumerServiceBuilder hokRedirectBinding() {
    return this.binding(HolderOfKeyMetadataSupport.HOK_WEBSSO_PROFILE_URI)
        .protocolBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
  }

  /**
   * Assigns the index for the service.
   *
   * @param index the index
   * @return the builder
   */
  public AssertionConsumerServiceBuilder index(final Integer index) {
    this.object().setIndex(index);
    return this;
  }

  /**
   * Sets the {@code isDefault} attribute of the service.
   *
   * @param def the Boolean
   * @return the builder
   */
  public AssertionConsumerServiceBuilder isDefault(final Boolean def) {
    this.object().setIsDefault(def);
    return this;
  }

}
