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
package se.swedenconnect.opensaml.saml2.request;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.SignatureSigningConfiguration;

import se.swedenconnect.opensaml.saml2.core.build.NameIDPolicyBuilder;
import se.swedenconnect.opensaml.saml2.core.build.RequestedAuthnContextBuilder;

/**
 * Defines a context which can be used to control how
 * {@link AuthnRequestGenerator#generateAuthnRequest(String, String, AuthnRequestGeneratorContext)} creates an
 * authentication request.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public interface AuthnRequestGeneratorContext {

  /**
   * Gets the binding to use when generating a request (redirect/POST).
   * <p>
   * This binding will be used in all cases except when the IdP does not support the binding.
   * </p>
   * <p>
   * The default implementation returns {@value SAMLConstants#SAML2_REDIRECT_BINDING_URI}.
   * </p>
   * 
   * @return the preferred binding
   */
  default String getPreferredBinding() {
    return SAMLConstants.SAML2_REDIRECT_BINDING_URI;
  }

  /**
   * Gets the SP requirement for using the Holder-of-key profile. The default is that the SP does not support HoK.
   * 
   * @return a HoK requirement
   */
  default HokRequirement getHokRequirement() {
    return HokRequirement.DONT_USE;
  }

  /**
   * Gets the {@code ForceAuthn} attribute value.
   * <p>
   * The default implementation returns {@code true}.
   * </p>
   * <p>
   * If {@code null} is returned, the {@code ForceAuthn} attribute will not be included.
   * </p>
   * 
   * @return the ForceAuthn attribute
   */
  default Boolean getForceAuthnAttribute() {
    return Boolean.TRUE;
  }

  /**
   * Gets the {@code IsPassive} attribute value.
   * <p>
   * The default implementation returns {@code null}.
   * </p>
   * <p>
   * If {@code null} is returned, the {@code IsPassive} attribute will not be included.
   * </p>
   * 
   * @return the IsPassive attribute
   */
  default Boolean getIsPassiveAttribute() {
    return null;
  }

  /**
   * If a signature configuration, other than the default
   * ({@link SecurityConfigurationSupport#getGlobalSignatureSigningConfiguration()}) should be used to sign the request
   * this method should return this configuration.
   * <p>
   * The default implementation returns {@code null}.
   * </p>
   * 
   * @return the signature configuration, or null
   */
  default SignatureSigningConfiguration getSignatureSigningConfiguration() {
    return null;
  }

  /**
   * Gets the {@link AssertionConsumerServiceResolver} to use for resolving which {@link AssertionConsumerService} to
   * use and whether to produce an {@code AssertionConsumerServiceURL} or {@code AssertionConsumerServiceIndex}
   * attribute.
   * <p>
   * The default implementation will return a {@code AssertionConsumerServiceURL} based on (1) {@code isDefault}
   * attribute and (2) the lowest {@code Index}.
   * </p>
   * 
   * @return a function for resolving AssertionConsumerService elements
   */
  default AssertionConsumerServiceResolver getAssertionConsumerServiceResolver() {
    return (list) -> list.stream()
      .filter(a -> a.isDefault())
      .map(AssertionConsumerService::getLocation)
      .findFirst()
      .orElse(list.stream()
        .min(Comparator.comparing(a -> a.getIndex() != null ? a.getIndex() : Integer.MAX_VALUE))
        .map(AssertionConsumerService::getLocation)
        .orElse(list.get(0).getLocation()));
  }

  /**
   * Gets the resolver function for determining how to create the {@code AttributeConsumingServiceIndex} attribute.
   * <p>
   * The default implementation returns {@code null}, meaning that no attribute is added.
   * </p>
   * 
   * @return a resolver function
   */
  default AttributeConsumingServiceIndexResolver getAttributeConsumingServiceIndexResolver() {
    return (list) -> null;
  }

  /**
   * Gets the builder for creating a {@code NameIDPolicy} element.
   * <p>
   * The default implementation will use the first {@code NameIDFormat} in the list and create a {@code NameIDPolicy}
   * element with this value as the {@code Format} attribute and the {@code AllowCreate} set to true. If the supplied
   * list is empty, the format will be set to {@code urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified}.
   * </p>
   * 
   * @return a builder function
   */
  default NameIDPolicyBuilderFunction getNameIDPolicyBuilderFunction() {
    return list -> NameIDPolicyBuilder.builder()
      .allowCreate(true)
      .format(list.isEmpty() ? NameID.UNSPECIFIED : list.get(0).getURI())
      .build();
  }

  /**
   * Gets the builder function for creating the {@code RequestedAuthnContext} element to add to the
   * {@code AuthnRequest}.
   * <p>
   * The default implementation will create a {@code RequestedAuthnContext} object with exact matching and all assurance
   * certification URI:s included in the supplied list.
   * </p>
   * 
   * @return a function that returns a RequestedAuthnContext (or null).
   */
  default RequestedAuthnContextBuilderFunction getRequestedAuthnContextBuilderFunction() {
    return (list, hok) -> !list.isEmpty()
        ? RequestedAuthnContextBuilder.builder()
          .comparison(AuthnContextComparisonTypeEnumeration.EXACT)
          .authnContextClassRefs(list)
          .build()
        : null;
  }

  /**
   * Gets the {@link AuthnRequestCustomizer}.
   * <p>
   * The default implementation returns a NO-OP consumer, meaning no customizations are done.
   * </p>
   * 
   * @return a consumer working on the AuthnRequest object being built
   */
  default AuthnRequestCustomizer getAuthnRequestCustomizer() {
    return (a) -> {
    };
  }

  /**
   * The {@link AuthnRequestGenerator} is normally configured with a signing credential
   * (AuthnRequestGenerator#getSignCredential()}. This method exist so that we may override the default credential.
   * Mainly for testing purposes.
   * <p>
   * The default implementation returns {@code null}.
   * </p>
   * 
   * @return the signing credential to use, or null if no override should be done
   */
  default X509Credential getOverrideSignCredential() {
    return null;
  }

  /**
   * When the generator is about to add the {@code AssertionConsumerServiceURL} or {@code AssertionConsumerServiceIndex}
   * attribute it will invoke the {@link AuthnRequestGeneratorContext#getAssertionConsumerServiceResolver()} method in
   * order to get a function that given all possible {@code AssertionConsumerService} elements (found in the SP
   * metadata) will either return a {@code String} (holding the {@code AssertionConsumerServiceURL} to use) or an
   * {@code Integer} (holding the {@code AssertionConsumerServiceIndex} to use).
   * <p>
   * Note: The function will be called even if there is only one possible {@code AssertionConsumerService}. In those
   * cases it is up to the function to decide whether to return a {@code String} or an {@code Integer}.
   * </p>
   */
  public interface AssertionConsumerServiceResolver extends Function<List<AssertionConsumerService>, Object> {
  }

  /**
   * If the SP metadata contains one or more {@code AttributeConsumingService} elements, the generator needs to know
   * whether to include the {@code AttributeConsumingServiceIndex} attribute, and if so, which index to use. The
   * generator gets this resolver by invoking
   * {@link AuthnRequestGeneratorContext#getAttributeConsumingServiceIndexResolver()}.
   * <p>
   * If this function returns {@code null}, no {@code AttributeConsumingServiceIndex} attribute will be included.
   * </p>
   */
  public interface AttributeConsumingServiceIndexResolver extends Function<List<AttributeConsumingService>, Integer> {
  }

  /**
   * A {@code NameIDPolicyBuilderFunction} is used by the generator to create the {@code NameIDPolicy} element. As input
   * to the function the generator supplies a list of {@code NameIDFormat} elements. This list is the intersection of
   * what is supported by the SP and the IdP (found in metadata). Note that this list may be empty.
   * <p>
   * The {@code NameIDPolicyResolver} will be invoked with a list where the order is the same order as the
   * {@code NameIDFormat} elements appear in the SP metadata.
   * </p>
   * <p>
   * If the resolver returns {@code null}, no {@code NameIDPolicy} will be included in the {@code AuthnRequest}.
   * </p>
   */
  public interface NameIDPolicyBuilderFunction extends Function<List<NameIDFormat>, NameIDPolicy> {
  }

  /**
   * The generator will need to know how to build the {@code RequestedAuthnContext} element that is to be included in
   * the {@code AuthnRequest}. The {@link AuthnRequestGeneratorContext#getRequestedAuthnContextBuilderFunction()} method
   * returns the function, that given a list of IdP assurance certification URI:s and a boolean that tells whether the
   * Holder-of-key profile is active or not (could affect which URI that is chosen), creates a
   * {@code RequestedAuthnContext} element.
   * <p>
   * If the function returns {@code null} no {@code RequestedAuthnContext} is added to the {@code AuthnRequest}.
   * </p>
   */
  public interface RequestedAuthnContextBuilderFunction extends BiFunction<List<String>, Boolean, RequestedAuthnContext> {
  }

  /**
   * When the generator is done building the {@code AuthnRequest}, but before it is signed, it will ask the
   * {@link AuthnRequestGeneratorContext#getAuthnRequestCustomizer()} method for the customizer that may operate and add
   * customizations to the request object.
   */
  public interface AuthnRequestCustomizer extends Consumer<AuthnRequest> {
  }

  /**
   * Enumeration that tells whether the Holder-of-key WebSSO profile is required, optional or not active.
   */
  public enum HokRequirement {

    /**
     * The SP will always use HoK - A call to
     * {@link AuthnRequestGenerator#generateAuthnRequest(String, String, AuthnRequestGeneratorContext)} for an IdP that
     * does not support this profile will fail.
     */
    REQUIRED,

    /**
     * The Holder-of-key profile will be used if the IdP supports it.
     */
    IF_AVAILABLE,

    /**
     * The holder-of-key profile will never be used.
     */
    DONT_USE
  };

}
