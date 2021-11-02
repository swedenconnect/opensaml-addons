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
package se.swedenconnect.opensaml.saml2.metadata.build;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;
import se.swedenconnect.opensaml.saml2.metadata.HolderOfKeyMetadataSupport;

/**
 * A builder for {@code SingleSignOnService} elements.
 * 
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
public class SingleSignOnServiceBuilder extends AbstractSAMLObjectBuilder<SingleSignOnService> {

  /**
   * Utility method that creates a builder.
   * 
   * @return a builder
   */
  public static SingleSignOnServiceBuilder builder() {
    return new SingleSignOnServiceBuilder();
  }

  /**
   * Assigns the location URI.
   * 
   * @param location
   *          the URI
   * @return the builder
   */
  public SingleSignOnServiceBuilder location(final String location) {
    this.object().setLocation(location);
    return this;
  }

  /**
   * Assigns the binding of the service
   * 
   * @param binding
   *          the binding URI
   * @return the builder
   * @see #postBinding()
   * @see #redirectBinding()
   */
  public SingleSignOnServiceBuilder binding(final String binding) {
    this.object().setBinding(binding);
    return this;
  }

  /**
   * Shortcut for assigning the SAML POST binding to the service.
   * 
   * @return the builder
   * @see #binding(String)
   */
  public SingleSignOnServiceBuilder postBinding() {
    this.object().setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
    return this;
  }

  /**
   * Shortcut for assigning the SAML Redirect binding to the service.
   * 
   * @return the builder
   * @see #binding(String)
   */
  public SingleSignOnServiceBuilder redirectBinding() {
    this.object().setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
    return this;
  }

  /**
   * If the SAML holder-of-key profile is used, the {@code Binding} attribute should be assigned
   * {@value HolderOfKeyMetadataSupport#HOK_WEBSSO_PROFILE_URI} and the actual binding should be assigned to the
   * {@code hoksso:ProtocolBinding} attribute. This method sets the {@code hoksso:ProtocolBinding} attribute to the
   * given binding.
   * 
   * @param binding
   *          the binding URI
   * @return this builder
   * @see #hokPostBinding()
   * @see #hokRedirectBinding()
   */
  public SingleSignOnServiceBuilder protocolBinding(final String binding) {
    this.object().getUnknownAttributes().put(HolderOfKeyMetadataSupport.HOK_PROTOCOL_BINDING_ATTRIBUTE, binding);
    return this;
  }

  /**
   * Shortcut to assign the {@code Binding} attribute to Holder-of-key and the {@code hoksso:ProtocolBinding} attribute
   * to the POST binding.
   * 
   * @return this builder
   */
  public SingleSignOnServiceBuilder hokPostBinding() {
    return this.binding(HolderOfKeyMetadataSupport.HOK_WEBSSO_PROFILE_URI)
      .protocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
  }

  /**
   * Shortcut to assign the {@code Binding} attribute to Holder-of-key and the {@code hoksso:ProtocolBinding} attribute
   * to the Redirect binding.
   * 
   * @return this builder
   */
  public SingleSignOnServiceBuilder hokRedirectBinding() {
    return this.binding(HolderOfKeyMetadataSupport.HOK_WEBSSO_PROFILE_URI)
      .protocolBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
  }

  /** {@inheritDoc} */
  @Override
  protected Class<SingleSignOnService> getObjectType() {
    return SingleSignOnService.class;
  }

}
