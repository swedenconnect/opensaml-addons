/*
 * Copyright 2016-2026 Sweden Connect
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import se.swedenconnect.opensaml.OpenSAMLTestBase;
import se.swedenconnect.opensaml.saml2.metadata.build.AssertionConsumerServiceBuilder;
import se.swedenconnect.opensaml.saml2.metadata.build.EntityDescriptorBuilder;
import se.swedenconnect.opensaml.saml2.metadata.build.IDPSSODescriptorBuilder;
import se.swedenconnect.opensaml.saml2.metadata.build.SPSSODescriptorBuilder;
import se.swedenconnect.opensaml.saml2.metadata.build.SingleSignOnServiceBuilder;
import se.swedenconnect.opensaml.saml2.request.AuthnRequestGeneratorContext.HokRequirement;

/**
 * Test cases for {@link DefaultAuthnRequestGenerator}.
 *
 * @author Martin Lindström
 */
class DefaultAuthnRequestGeneratorTest extends OpenSAMLTestBase {

  private static final String SP_ENTITY_ID = "https://sp.example.com";
  private static final String SP_ACS = "https://sp.example.com/acs";
  private static final String IDP_SSO = "https://idp.example.com/sso";
  private static final String IDP_HOK_SSO = "https://idp.example.com/hok/sso";

  private static final AuthnRequestGeneratorContext IF_AVAILABLE_CONTEXT = new AuthnRequestGeneratorContext() {
    @Override
    public HokRequirement getHokRequirement() {
      return HokRequirement.IF_AVAILABLE;
    }
  };

  @Test
  void testHokIfAvailableSpWithoutHokEndpoint() throws Exception {
    final EntityDescriptor spMetadata = EntityDescriptorBuilder.builder()
        .entityID(SP_ENTITY_ID)
        .ssoDescriptor(SPSSODescriptorBuilder.builder()
            .assertionConsumerServices(
                AssertionConsumerServiceBuilder.builder().postBinding().location(SP_ACS).index(0).build())
            .build())
        .build();

    final EntityDescriptor idpMetadata = EntityDescriptorBuilder.builder()
        .entityID("https://idp.example.com")
        .ssoDescriptor(IDPSSODescriptorBuilder.builder()
            .singleSignOnServices(
                SingleSignOnServiceBuilder.builder().hokRedirectBinding().location(IDP_HOK_SSO).build(),
                SingleSignOnServiceBuilder.builder().redirectBinding().location(IDP_SSO).build())
            .build())
        .build();

    final DefaultAuthnRequestGenerator generator =
        new DefaultAuthnRequestGenerator(spMetadata, null, Mockito.mock(MetadataResolver.class));
    generator.initialize();

    final RequestHttpObject<AuthnRequest> request =
        generator.generateAuthnRequest(idpMetadata, null, IF_AVAILABLE_CONTEXT);
    Assertions.assertEquals(IDP_SSO, request.getRequest().getDestination());
    Assertions.assertEquals(SP_ACS, request.getRequest().getAssertionConsumerServiceURL());
  }

}
