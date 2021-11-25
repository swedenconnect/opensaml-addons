/*
 * Copyright 2021 Sweden Connect
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
package se.swedenconnect.opensaml.saml2.metadata;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;

/**
 * Support methods for holder-of-key specific metadata elements.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public class HolderOfKeyMetadataSupport {

  /** URI identifier for the Holder-of-key profile URI. */
  public static final String HOK_WEBSSO_PROFILE_URI = "urn:oasis:names:tc:SAML:2.0:profiles:holder-of-key:SSO:browser";

  /** The QName for the HoK ProtocolBinding attribute. */
  public static final QName HOK_PROTOCOL_BINDING_ATTRIBUTE = new QName(HOK_WEBSSO_PROFILE_URI, "ProtocolBinding", "hoksso");

  /**
   * Given an {@link IDPSSODescriptor} element the method locates all {@code SingleSignOnService} elements that have a
   * {@code Binding} attribute set to {@value HOK_WEBSSO_PROFILE_URI}, i.e., a service element for Holder-of-key.
   * 
   * @param ssoDescriptor
   *          the IDPSSODescriptor
   * @return a (possible empty) list of matching SingleSignOnService objects
   */
  public static List<SingleSignOnService> getHokSingleSignOnServices(final IDPSSODescriptor ssoDescriptor) {
    return ssoDescriptor.getSingleSignOnServices().stream()
      .filter(s -> HOK_WEBSSO_PROFILE_URI.equals(s.getBinding()))
      .collect(Collectors.toList());
  }

  /**
   * Given an {@link IDPSSODescriptor} element and a binding (redirect/post), the method locates a matching
   * {@code SingleSignOnService} Holder-of-key element.
   * 
   * @param ssoDescriptor
   *          the IDPSSODescriptor
   * @param binding
   *          the actual binding URI
   * @return a SingleSignOnService or null if no matching element is found
   */
  public static SingleSignOnService getHoKSingleSignOnService(final IDPSSODescriptor ssoDescriptor, final String binding) {
    for (final SingleSignOnService sso : getHokSingleSignOnServices(ssoDescriptor)) {
      final String protocolBinding = sso.getUnknownAttributes().get(HOK_PROTOCOL_BINDING_ATTRIBUTE);
      if (binding.equals(protocolBinding)) {
        return sso;
      }
    }
    return null;
  }

  /**
   * Predicate that tells if the supplied {@code SingleSignOnService} is a HoK endpoint.
   * 
   * @param sso
   *          the SingleSignOnService to test
   * @return true if the supplied object is a HoK endpoint and false otherwise
   */
  public static boolean isHoKSingleSignOnService(final SingleSignOnService sso) {
    return HOK_WEBSSO_PROFILE_URI.equals(sso.getBinding());
  }

  /**
   * Given an {@link SPSSODescriptor} element the method locates all {@code AssertionConsumerService} elements that have
   * a {@code Binding} attribute set to {@value HOK_WEBSSO_PROFILE_URI}, i.e., an endpoint for Holder-of-key.
   * 
   * @param ssoDescriptor
   *          the SPSSODescriptor
   * @return a (possible empty) list of matching AssertionConsumerService objects
   */
  public static List<AssertionConsumerService> getHokAssertionConsumerServices(final SPSSODescriptor ssoDescriptor) {
    return ssoDescriptor.getAssertionConsumerServices().stream()
      .filter(a -> HOK_WEBSSO_PROFILE_URI.equals(a.getBinding()))
      .collect(Collectors.toList());
  }

  /**
   * Given an {@link SPSSODescriptor} element and a binding URI, the method locates a matching
   * {@code AssertionConsumerService} Holder-of-key element.
   * 
   * @param ssoDescriptor
   *          the SPSSODescriptor
   * @param binding
   *          the actual binding URI
   * @return a AssertionConsumerService or null if no matching element is found
   */
  public static AssertionConsumerService getHokAssertionConsumerService(final SPSSODescriptor ssoDescriptor, final String binding) {
    for (final AssertionConsumerService acs : getHokAssertionConsumerServices(ssoDescriptor)) {
      final String protocolBinding = acs.getUnknownAttributes().get(HOK_PROTOCOL_BINDING_ATTRIBUTE);
      if (binding.equals(protocolBinding)) {
        return acs;
      }
    }
    return null;
  }
  
  /**
   * Predicate that tells if the supplied {@code AssertionConsumerService} is a HoK endpoint.
   * 
   * @param acs
   *          the AssertionConsumerService to test
   * @return true if the supplied object is a HoK endpoint and false otherwise
   */
  public static boolean isHoKAssertionConsumerService(final AssertionConsumerService acs) {
    return HOK_WEBSSO_PROFILE_URI.equals(acs.getBinding());
  }

  private HolderOfKeyMetadataSupport() {
  }

}
