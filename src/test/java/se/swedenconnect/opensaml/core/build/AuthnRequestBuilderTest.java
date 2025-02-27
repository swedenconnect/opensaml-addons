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
package se.swedenconnect.opensaml.core.build;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;

import se.swedenconnect.opensaml.OpenSAMLTestBase;
import se.swedenconnect.opensaml.saml2.core.build.AuthnRequestBuilder;
import se.swedenconnect.opensaml.saml2.core.build.NameIDPolicyBuilder;
import se.swedenconnect.opensaml.saml2.core.build.RequestedAuthnContextBuilder;
import se.swedenconnect.opensaml.saml2.core.build.ScopingBuilder;

/**
 * Test cases for the {@code AuthnRequestBuilder}.
 */
public class AuthnRequestBuilderTest extends OpenSAMLTestBase {

  @Test
  public void testBuildAuthnRequest() throws Exception {

    final String assertionConsumerServiceURL = "https://eid.litsec.se/svelegtest-sp/saml2/post/11";
    final String destination = "https://idp.svelegtest.se/idp/profile/SAML2/Redirect/SSO";
    final String id = "_4ugrqo5Eg4kFVBSgl6MHd1HqY2Dj35NY8HS2ut9H";
    final String issuer = "https://eid.litsec.se/sp/eidas";
    final String authnContext = "http://id.elegnamnden.se/loa/1.0/loa3";
    final Instant now = Instant.now();
    final String requesterID = "http://www.example.com/sp";

    final AuthnRequest request = AuthnRequestBuilder.builder()
      .assertionConsumerServiceURL(assertionConsumerServiceURL)
      .destination(destination)
      .forceAuthn(true)
      .isPassive(false)
      .id(id)
      .issueInstant(now)
      .postProtocolBinding()
      .issuer(issuer)
      .nameIDPolicy(
        NameIDPolicyBuilder.builder().allowCreate(true).format(NameID.PERSISTENT).build())
      .scoping(
        ScopingBuilder.builder().requesterIDs(requesterID).build())
      .requestedAuthnContext(RequestedAuthnContextBuilder.builder()
        .comparison(AuthnContextComparisonTypeEnumeration.EXACT)
        .authnContextClassRefs(authnContext)
        .build())
      .build();

    Assertions.assertEquals(id, request.getID());
    Assertions.assertEquals(assertionConsumerServiceURL, request.getAssertionConsumerServiceURL());
    Assertions.assertEquals(destination, request.getDestination());
    Assertions.assertEquals(issuer, request.getIssuer().getValue());
    Assertions.assertEquals(NameID.ENTITY, request.getIssuer().getFormat());
    Assertions.assertEquals(Boolean.TRUE, request.isForceAuthn());
    Assertions.assertEquals(Boolean.FALSE, request.isPassive());
    Assertions.assertEquals(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), request.getIssueInstant().toEpochMilli());
    Assertions.assertEquals(SAMLConstants.SAML2_POST_BINDING_URI, request.getProtocolBinding());
    Assertions.assertEquals(NameID.PERSISTENT, request.getNameIDPolicy().getFormat());
    Assertions.assertEquals(requesterID, request.getScoping().getRequesterIDs().get(0).getURI());
    Assertions.assertEquals(Boolean.TRUE, request.getNameIDPolicy().getAllowCreate());
    Assertions.assertEquals(AuthnContextComparisonTypeEnumeration.EXACT, request.getRequestedAuthnContext().getComparison());
    Assertions.assertEquals(List.of(authnContext),
      request.getRequestedAuthnContext().getAuthnContextClassRefs().stream()
        .map(AuthnContextClassRef::getURI)
        .collect(Collectors.toList()));

  }

}
