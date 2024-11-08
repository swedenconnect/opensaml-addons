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
package se.swedenconnect.opensaml.saml2.metadata.build;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.idpdisco.DiscoveryResponse;
import org.opensaml.saml.ext.saml2alg.DigestMethod;
import org.opensaml.saml.ext.saml2alg.SigningMethod;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.ext.saml2mdui.Logo;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.ContactPersonTypeEnumeration;
import org.opensaml.saml.saml2.metadata.EncryptionMethod;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

import net.shibboleth.shared.xml.SerializeSupport;
import se.swedenconnect.opensaml.OpenSAMLTestBase;
import se.swedenconnect.opensaml.common.utils.LocalizedString;
import se.swedenconnect.opensaml.saml2.attribute.AttributeConstants;
import se.swedenconnect.opensaml.saml2.attribute.AttributeUtils;
import se.swedenconnect.opensaml.saml2.metadata.EntityDescriptorUtils;

/**
 * Test cases for building a complete metadata file.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class EntityDescriptorBuilderTest extends OpenSAMLTestBase {

  private static final String SP_ENTITY_ID = "https://eid.litsec.se/sp/eidas";

  /**
   * Tests building an {@code EntityDescriptor} for a Service Provider from scratch.
   */
  @Test
  public void testBuildSpMetadata() throws Exception {

    final String metadataId = "MLoeU7ALIHTZ61ibqZdJ";
    final Instant validUntil = Instant.now();
    validUntil.plus(7, ChronoUnit.DAYS);
    final long cacheDuration = 3600000L;

    final DigestMethod[] digestMethods = {
        DigestMethodBuilder.builder().algorithm(SignatureConstants.ALGO_ID_DIGEST_SHA256).build(),
        DigestMethodBuilder.builder().algorithm(SignatureConstants.ALGO_ID_DIGEST_SHA384).build(),
        DigestMethodBuilder.builder().algorithm(SignatureConstants.ALGO_ID_DIGEST_SHA512).build() };

    final SigningMethod[] signingMethods = {
        SigningMethodBuilder.builder().algorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256).minKeySize(2048).build(),
        SigningMethodBuilder.builder().algorithm(SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA512).build() };

    final EncryptionMethod[] encryptionMethods = {
        EncryptionMethodBuilder.builder().algorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256).build(),
        EncryptionMethodBuilder.builder().algorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128).build(),
        EncryptionMethodBuilder.builder().algorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP).build(),
        EncryptionMethodBuilder.builder().algorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSA15).build()
    };

    final String[] entityCategories = { "http://id.elegnamnden.se/ec/1.0/loa3-pnr", "http://id.elegnamnden.se/ec/1.0/eidas-naturalperson" };

    final LocalizedString[] uiDisplayNames = {
        new LocalizedString("E-legitimationsnämndens Test-SP för eIDAS", "sv"),
        new LocalizedString("The e-Identification Board Test SP for eIDAS", "en")
    };

    final LocalizedString[] uiDescriptions = {
        new LocalizedString(
          "E-legitimationsnämndens e-tjänst (Service Provider) för test- och referensändamål - Konfigurerad for legitimering enligt Svensk e-legitimation/eIDAS",
          "sv"),
        new LocalizedString(
          "The Swedish e-Identification Board Service Provider for test and reference - Configured for authentication according to Swedish eID/eIDAS",
          "en")
    };

    final Logo[] uiLogos = {
        LogoBuilder.logo("https://eid.litsec.se/svelegtest-sp/img/elegnamnden_114x100.png", 100, 114),
        LogoBuilder.logo("https://eid.litsec.se/svelegtest-sp/img/elegnamnden_logo_160x90.png", 90, 160),
        LogoBuilder.logo("https://eid.litsec.se/svelegtest-sp/img/elegnamnden_notext_16x16.png", 16, 16),
        LogoBuilder.logo("https://eid.litsec.se/svelegtest-sp/img/elegnamnden_notext_68x67.png", 67, 68)
    };

    final DiscoveryResponse[] discoveryResponses = {
        DiscoveryResponseBuilder.builder("https://eid.litsec.se/svelegtest-sp/authnrequest/disco/11", 1).build(),
        DiscoveryResponseBuilder.builder("https://localhost:8443/svelegtest-sp/authnrequest/disco/11", 2).build(),
    };

    final Resource signatureCertificateResource = new ClassPathResource("/credentials/litsec_sign.crt");
    final X509Certificate encryptionCertificate = decodeCertificate((new ClassPathResource(
      "/credentials/litsec_auth.crt")).getInputStream());

    final String[] nameIDFormats = { NameID.PERSISTENT, NameID.TRANSIENT };

    final AssertionConsumerService[] assertionConsumerServices = {
        AssertionConsumerServiceBuilder.builder()
          .postBinding()
          .isDefault(true)
          .index(0)
          .location("https://eid.litsec.se/svelegtest-sp/saml2/post/11")
          .build(),
        AssertionConsumerServiceBuilder.builder()
          .postBinding()
          .index(1)
          .location("https://localhost:8443/svelegtest-sp/saml2/post/11")
          .build()
    };

    final List<String> requestedAttributes = Arrays.asList("urn:oid:1.2.752.29.4.13", "urn:oid:1.2.752.201.3.7", "urn:oid:1.2.752.201.3.4");

    final LocalizedString[] serviceNames = {
        new LocalizedString("E-legitimationsnämndens Test-SP för eIDAS", "sv"),
        new LocalizedString("The e-Identification Board Test SP for eIDAS", Locale.ENGLISH)
    };

    final LocalizedString[] organizationNames = {
        new LocalizedString("E-legitimationsnämnden", "sv"), new LocalizedString("Swedish e-Identification Board", "en")
    };

    final LocalizedString[] organizationDisplayNames = {
        new LocalizedString("E-legitimationsnämnden", "sv"), new LocalizedString("Swedish e-Identification Board", "en")
    };

    final LocalizedString organizationURL = new LocalizedString("http://www.elegnamnden.se", "sv");

    final ContactPerson contactPersonTemplate = ContactPersonBuilder.builder()
      .company("Litsec AB")
      .givenName("Martin")
      .surname("Lindström")
      .emailAddresses("martin.lindstrom@litsec.se")
      .telephoneNumbers("+46 (0)70 361 98 80")
      .build();

    final EntityDescriptor ed = EntityDescriptorBuilder.builder()
      .entityID(SP_ENTITY_ID)
      .id(metadataId)
      .cacheDuration(cacheDuration)
      .validUntil(validUntil)
      .extensions(ExtensionsBuilder.builder()
        .extension(EntityAttributesBuilder.builder()
          .entityCategoriesAttribute(entityCategories)
          .build())
        .extension(digestMethods)
        .build())
      .ssoDescriptor(SPSSODescriptorBuilder.builder()
        .wantAssertionsSigned(true)
        .authnRequestsSigned(true)
        .extensions(ExtensionsBuilder.builder()
          .extension(UIInfoBuilder.builder()
            .displayNames(uiDisplayNames)
            .descriptions(uiDescriptions)
            .logos(uiLogos)
            .build())
          .extension(signingMethods)
          .extension(discoveryResponses)
          .build())
        .keyDescriptors(
          KeyDescriptorBuilder.builder()
            .use(UsageType.SIGNING)
            .keyName("Litsec Signing")
            .certificate(signatureCertificateResource.getInputStream())
            .build(),
          KeyDescriptorBuilder.builder()
            .use(UsageType.ENCRYPTION)
            .keyName("Litsec Encrypt")
            .certificate(encryptionCertificate)
            .encryptionMethods(encryptionMethods)
            .build())
        .nameIDFormats(nameIDFormats)
        .assertionConsumerServices(assertionConsumerServices)
        .attributeConsumingServices(
          AttributeConsumingServiceBuilder.builder()
            .isDefault(true)
            .index(0)
            .serviceNames(serviceNames)
            .requestedAttributes(requestedAttributes.stream()
              .map(a -> RequestedAttributeBuilder.builder(a).isRequired(false).build())
              .collect(Collectors.toList()))
            .build())
        .build())
      .organization(
        OrganizationBuilder.builder()
          .organizationNames(organizationNames)
          .organizationDisplayNames(organizationDisplayNames)
          .organizationURLs(organizationURL)
          .build())
      .contactPersons(
        ContactPersonBuilder.builder(contactPersonTemplate).type(ContactPersonTypeEnumeration.TECHNICAL).build(),
        ContactPersonBuilder.builder(contactPersonTemplate).type(ContactPersonTypeEnumeration.SUPPORT).build())
      .build();

    // System.out.println(SerializeSupport.prettyPrintXML(XMLObjectSupport.marshall(ed)));

    Assertions.assertEquals(metadataId, ed.getID());
    Assertions.assertEquals((Long) cacheDuration, (Long) ed.getCacheDuration().toMillis());
    Assertions.assertEquals(validUntil.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), ed.getValidUntil().toEpochMilli());

    final EntityAttributes entityAttributes = EntityDescriptorUtils.getMetadataExtension(ed.getExtensions(), EntityAttributes.class);
    Assertions.assertNotNull(entityAttributes);
    final Attribute entityCategoryAttribute = AttributeUtils.getAttribute(
      AttributeConstants.ENTITY_CATEGORY_ATTRIBUTE_NAME, entityAttributes.getAttributes());
    Assertions.assertNotNull(entityCategoryAttribute);
    Assertions.assertEquals(Arrays.asList(entityCategories), AttributeUtils.getAttributeStringValues(entityCategoryAttribute));

    Assertions.assertEquals(3, EntityDescriptorUtils.getDigestMethods(ed).size());
    Assertions.assertEquals(3, ed.getExtensions().getUnknownXMLObjects(DigestMethod.DEFAULT_ELEMENT_NAME).size());

    Assertions.assertEquals(2, EntityDescriptorUtils.getSigningMethods(ed).size());
    Assertions.assertTrue(ed.getExtensions().getUnknownXMLObjects(SigningMethod.DEFAULT_ELEMENT_NAME).isEmpty());

    final List<DigestMethod> digestList = EntityDescriptorUtils.getDigestMethods(ed);
    Assertions.assertEquals(digestMethods[0].getAlgorithm(), digestList.get(0).getAlgorithm());
    Assertions.assertEquals(digestMethods[1].getAlgorithm(), digestList.get(1).getAlgorithm());
    Assertions.assertEquals(digestMethods[2].getAlgorithm(), digestList.get(2).getAlgorithm());

    final SPSSODescriptor ssoDescriptor = ed.getSPSSODescriptor(SAMLConstants.SAML20P_NS);

    Assertions.assertEquals(2, ssoDescriptor.getExtensions().getUnknownXMLObjects(SigningMethod.DEFAULT_ELEMENT_NAME).size());
    Assertions.assertTrue(ssoDescriptor.getExtensions().getUnknownXMLObjects(DigestMethod.DEFAULT_ELEMENT_NAME).isEmpty());

    final List<SigningMethod> signingList = EntityDescriptorUtils.getSigningMethods(ed);
    Assertions.assertEquals(signingMethods[0].getAlgorithm(), signingList.get(0).getAlgorithm());
    Assertions.assertEquals(signingMethods[0].getMinKeySize(), signingList.get(0).getMinKeySize());
    Assertions.assertEquals(signingMethods[1].getAlgorithm(), signingList.get(1).getAlgorithm());

    Assertions.assertTrue(ssoDescriptor.isAuthnRequestsSigned());
    Assertions.assertTrue(ssoDescriptor.getWantAssertionsSigned());

    final UIInfo uiInfo = EntityDescriptorUtils.getMetadataExtension(ssoDescriptor.getExtensions(), UIInfo.class);
    Assertions.assertNotNull(uiInfo);
    Assertions.assertEquals(Arrays.asList(uiDisplayNames),
      uiInfo.getDisplayNames().stream().map(dn -> new LocalizedString(dn.getValue(), dn.getXMLLang())).collect(Collectors.toList()));
    Assertions.assertEquals(Arrays.asList(uiDescriptions),
      uiInfo.getDescriptions().stream().map(d -> new LocalizedString(d.getValue(), d.getXMLLang())).collect(Collectors.toList()));
    // The Logo class is buggy. Its hashcode assumes that there is a language set, so we'll have to check all
    // attributes.
    Assertions.assertEquals(Arrays.asList(uiLogos).stream().map(l -> l.getURI()).collect(Collectors.toList()),
      uiInfo.getLogos().stream().map(l -> l.getURI()).collect(Collectors.toList()));
    Assertions.assertEquals(Arrays.asList(uiLogos).stream().map(l -> l.getHeight()).collect(Collectors.toList()),
      uiInfo.getLogos().stream().map(l -> l.getHeight()).collect(Collectors.toList()));
    Assertions.assertEquals(Arrays.asList(uiLogos).stream().map(l -> l.getWidth()).collect(Collectors.toList()),
      uiInfo.getLogos().stream().map(l -> l.getWidth()).collect(Collectors.toList()));

    final List<DiscoveryResponse> discoResponses =
        EntityDescriptorUtils.getMetadataExtensions(ssoDescriptor.getExtensions(), DiscoveryResponse.class);
    Assertions.assertTrue(discoResponses.size() == 2);
    Assertions.assertEquals(1, (int) discoResponses.get(0).getIndex());
    Assertions.assertEquals(discoveryResponses[0].getLocation(), discoResponses.get(0).getLocation());
    Assertions.assertEquals(2, (int) discoResponses.get(1).getIndex());
    Assertions.assertEquals(discoveryResponses[1].getLocation(), discoResponses.get(1).getLocation());

    Assertions.assertEquals(UsageType.SIGNING, ssoDescriptor.getKeyDescriptors().get(0).getUse());
    Assertions.assertEquals("Litsec Signing", ssoDescriptor.getKeyDescriptors().get(0).getKeyInfo().getKeyNames().get(0).getValue());
    Assertions.assertTrue(ssoDescriptor.getKeyDescriptors().get(0).getKeyInfo().getX509Datas().get(0).getX509Certificates().size() == 1);

    Assertions.assertEquals(UsageType.ENCRYPTION, ssoDescriptor.getKeyDescriptors().get(1).getUse());
    Assertions.assertEquals("Litsec Encrypt", ssoDescriptor.getKeyDescriptors().get(1).getKeyInfo().getKeyNames().get(0).getValue());
    Assertions.assertTrue(ssoDescriptor.getKeyDescriptors().get(1).getKeyInfo().getX509Datas().get(0).getX509Certificates().size() == 1);
    Assertions.assertEquals(4, ssoDescriptor.getKeyDescriptors().get(1).getEncryptionMethods().size());

    Assertions.assertEquals(Arrays.asList(nameIDFormats), ssoDescriptor.getNameIDFormats().stream().map(n -> n.getURI()).collect(Collectors
      .toList()));

    Assertions.assertEquals(2, ssoDescriptor.getAssertionConsumerServices().size());

    Assertions.assertEquals(Boolean.TRUE, ssoDescriptor.getAssertionConsumerServices().get(0).isDefault());
    Assertions.assertNull(ssoDescriptor.getAssertionConsumerServices().get(1).isDefaultXSBoolean());
    Assertions.assertEquals(SAMLConstants.SAML2_POST_BINDING_URI, ssoDescriptor.getAssertionConsumerServices().get(0).getBinding());
    Assertions.assertEquals(SAMLConstants.SAML2_POST_BINDING_URI, ssoDescriptor.getAssertionConsumerServices().get(1).getBinding());
    Assertions.assertEquals(0, (int) ssoDescriptor.getAssertionConsumerServices().get(0).getIndex());
    Assertions.assertEquals(1, (int) ssoDescriptor.getAssertionConsumerServices().get(1).getIndex());
    Assertions.assertEquals("https://eid.litsec.se/svelegtest-sp/saml2/post/11", ssoDescriptor.getAssertionConsumerServices()
      .get(0)
      .getLocation());
    Assertions.assertEquals("https://localhost:8443/svelegtest-sp/saml2/post/11", ssoDescriptor.getAssertionConsumerServices()
      .get(1)
      .getLocation());

    Assertions.assertEquals(requestedAttributes,
      ssoDescriptor.getAttributeConsumingServices().get(0).getRequestedAttributes().stream().map(a -> a.getName()).collect(Collectors
        .toList()));
    Assertions.assertEquals(Arrays.asList(serviceNames),
      ssoDescriptor.getAttributeConsumingServices()
        .get(0)
        .getNames()
        .stream()
        .map(s -> new LocalizedString(s.getValue(), s.getXMLLang()))
        .collect(Collectors.toList()));

    Assertions.assertEquals(Arrays.asList(organizationNames),
      ed.getOrganization().getOrganizationNames().stream().map(n -> new LocalizedString(n.getValue(), n.getXMLLang())).collect(Collectors
        .toList()));
    Assertions.assertEquals(Arrays.asList(organizationDisplayNames),
      ed.getOrganization().getDisplayNames().stream().map(n -> new LocalizedString(n.getValue(), n.getXMLLang())).collect(Collectors
        .toList()));
    Assertions.assertEquals(List.of(organizationURL),
      ed.getOrganization().getURLs().stream().map(n -> new LocalizedString(n.getURI(), n.getXMLLang())).collect(Collectors.toList()));

    Assertions.assertEquals(2, ed.getContactPersons().size());
    Assertions.assertEquals(ContactPersonTypeEnumeration.TECHNICAL, ed.getContactPersons().get(0).getType());
    Assertions.assertEquals(ContactPersonTypeEnumeration.SUPPORT, ed.getContactPersons().get(1).getType());
    for (int i = 0; i < 2; i++) {
      Assertions.assertEquals("Litsec AB", ed.getContactPersons().get(i).getCompany().getValue());
      Assertions.assertEquals("Martin", ed.getContactPersons().get(i).getGivenName().getValue());
      Assertions.assertEquals("Lindström", ed.getContactPersons().get(i).getSurName().getValue());
      Assertions.assertEquals(List.of("martin.lindstrom@litsec.se"),
        ed.getContactPersons().get(i).getEmailAddresses().stream().map(m -> m.getURI()).collect(Collectors.toList()));
      Assertions.assertEquals(List.of("+46 (0)70 361 98 80"),
        ed.getContactPersons().get(i).getTelephoneNumbers().stream().map(t -> t.getValue()).collect(Collectors.toList()));
    }
  }

  /**
   * Tests building an {@code EntityDescriptor} for an Identity Provider from scratch.
   */
  @Test
  public void testBuildIdpMetadata() throws Exception {

    final String metadataId = "MLoeU7ALIHTZ61ibqZdJ";
    final Instant validUntil = Instant.now();
    validUntil.plus(7, ChronoUnit.DAYS);
    final long cacheDuration = 3600000L;

    final String[] assuranceCertificationUris = {
        "http://id.elegnamnden.se/loa/1.0/loa2",
        "http://id.elegnamnden.se/loa/1.0/loa3",
        "http://id.elegnamnden.se/loa/1.0/loa4"
    };

    final String[] entityCategories = {
        "http://id.elegnamnden.se/ec/1.0/loa3-pnr",
        "http://id.elegnamnden.se/ec/1.0/loa4-pnr"
    };

    final LocalizedString[] uiDisplayNames = {
        new LocalizedString("E-legitimationsnämndens Legitimeringstjänst för test", "sv"),
        new LocalizedString("The e-Identification Board Test Identity Provider", "en")
    };

    final LocalizedString[] uiDescriptions = {
        new LocalizedString("Referens-Legitimeringstjänst för Sveleg testfederation", "sv"),
        new LocalizedString("Reference Identity Provider for Sveleg test federation", "en")
    };

    final Logo[] uiLogos = {
        LogoBuilder.logo("https://eid.svelegtest.se/logos/elegnamnden_114x100.png", 100, 114),
        LogoBuilder.logo("https://eid.svelegtest.se/logos/elegnamnden_logo_160x90.png", 90, 160),
        LogoBuilder.logo("https://eid.svelegtest.se/logos/elegnamnden_notext_16x16.png", 16, 16),
        LogoBuilder.logo("https://eid.svelegtest.se/logos/elegnamnden_notext_68x67.png", 67, 68)
    };

    final Resource signatureCertificateResource = new ClassPathResource("/credentials/litsec_sign.crt");
    final X509Certificate encryptionCertificate = decodeCertificate((new ClassPathResource(
      "/credentials/litsec_auth.crt")).getInputStream());

    final String[] nameIDFormats = { NameID.PERSISTENT, NameID.TRANSIENT };

    final SingleSignOnService[] singleSignOnServices = {
        SingleSignOnServiceBuilder.builder().redirectBinding().location("https://idp.svelegtest.se/idp/profile/SAML2/Redirect/SSO").build(),
        SingleSignOnServiceBuilder.builder().postBinding().location("https://idp.svelegtest.se/idp/profile/SAML2/POST/SSO").build()
    };

    final LocalizedString[] organizationNames = {
        new LocalizedString("E-legitimationsnämnden", "sv"), new LocalizedString("Swedish e-Identification Board", "en")
    };

    final LocalizedString[] organizationDisplayNames = {
        new LocalizedString("E-legitimationsnämnden", "sv"), new LocalizedString("Swedish e-Identification Board", "en")
    };

    final LocalizedString organizationURL = new LocalizedString("http://www.elegnamnden.se", "sv");

    final ContactPerson contactPersonTemplate = ContactPersonBuilder.builder()
      .company("E-legitimationsnämnden")
      .emailAddresses("stefan@aaa-sec.com")
      .build();

    final EntityDescriptor ed = EntityDescriptorBuilder.builder()
      .entityID(SP_ENTITY_ID)
      .id(metadataId)
      .cacheDuration(cacheDuration)
      .validUntil(validUntil)
      .extensions(ExtensionsBuilder.builder()
        .extension(EntityAttributesBuilder.builder()
          .assuranceCertificationAttribute(assuranceCertificationUris)
          .entityCategoriesAttribute(entityCategories)
          .build())
        .build())
      .ssoDescriptor(IDPSSODescriptorBuilder.builder()
        .wantAuthnRequestsSigned(true)
        .extensions(ExtensionsBuilder.builder()
          .extension(UIInfoBuilder.builder()
            .displayNames(uiDisplayNames)
            .descriptions(uiDescriptions)
            .logos(uiLogos)
            .build())
          .build())
        .keyDescriptors(
          KeyDescriptorBuilder.builder()
            .use(UsageType.SIGNING)
            .certificate(signatureCertificateResource.getInputStream())
            .build(),
          KeyDescriptorBuilder.builder()
            .use(UsageType.ENCRYPTION)
            .certificate(encryptionCertificate)
            .build())
        .nameIDFormats(nameIDFormats)
        .singleSignOnServices(singleSignOnServices)
        .build())
      .organization(
        OrganizationBuilder.builder()
          .organizationNames(organizationNames)
          .organizationDisplayNames(organizationDisplayNames)
          .organizationURLs(organizationURL)
          .build())
      .contactPersons(
        ContactPersonBuilder.builder(contactPersonTemplate).type(ContactPersonTypeEnumeration.TECHNICAL).build(),
        ContactPersonBuilder.builder(contactPersonTemplate).type(ContactPersonTypeEnumeration.SUPPORT).build())
      .build();

    final Element elm = XMLObjectSupport.marshall(ed);
    System.out.println(SerializeSupport.prettyPrintXML(elm));

    Assertions.assertEquals(metadataId, ed.getID());
    Assertions.assertEquals((Long) cacheDuration, (Long) ed.getCacheDuration().toMillis());
    Assertions.assertEquals(validUntil.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), ed.getValidUntil().toEpochMilli());

    final EntityAttributes entityAttributes = EntityDescriptorUtils.getMetadataExtension(ed.getExtensions(), EntityAttributes.class);
    Assertions.assertNotNull(entityAttributes);

    final Attribute entityCategoryAttribute = AttributeUtils.getAttribute(
      AttributeConstants.ENTITY_CATEGORY_ATTRIBUTE_NAME, entityAttributes.getAttributes());
    Assertions.assertNotNull(entityCategoryAttribute);
    Assertions.assertEquals(Arrays.asList(entityCategories), AttributeUtils.getAttributeStringValues(entityCategoryAttribute));

    final Attribute assuranceCertificationAttribute = AttributeUtils.getAttribute(
      AttributeConstants.ASSURANCE_CERTIFICATION_ATTRIBUTE_NAME, entityAttributes.getAttributes());
    Assertions.assertNotNull(assuranceCertificationAttribute);
    Assertions.assertEquals(Arrays.asList(assuranceCertificationUris), AttributeUtils.getAttributeStringValues(assuranceCertificationAttribute));

    final IDPSSODescriptor ssoDescriptor = ed.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);

    Assertions.assertEquals(Boolean.TRUE, ssoDescriptor.getWantAuthnRequestsSigned());

    final UIInfo uiInfo = EntityDescriptorUtils.getMetadataExtension(ssoDescriptor.getExtensions(), UIInfo.class);
    Assertions.assertNotNull(uiInfo);
    Assertions.assertEquals(Arrays.asList(uiDisplayNames),
      uiInfo.getDisplayNames().stream().map(dn -> new LocalizedString(dn.getValue(), dn.getXMLLang())).collect(Collectors.toList()));
    Assertions.assertEquals(Arrays.asList(uiDescriptions),
      uiInfo.getDescriptions().stream().map(d -> new LocalizedString(d.getValue(), d.getXMLLang())).collect(Collectors.toList()));
    // The Logo class is buggy. Its hashcode assumes that there is a language set, so we'll have to check all
    // attributes.
    Assertions.assertEquals(Arrays.asList(uiLogos).stream().map(l -> l.getURI()).collect(Collectors.toList()),
      uiInfo.getLogos().stream().map(l -> l.getURI()).collect(Collectors.toList()));
    Assertions.assertEquals(Arrays.asList(uiLogos).stream().map(l -> l.getHeight()).collect(Collectors.toList()),
      uiInfo.getLogos().stream().map(l -> l.getHeight()).collect(Collectors.toList()));
    Assertions.assertEquals(Arrays.asList(uiLogos).stream().map(l -> l.getWidth()).collect(Collectors.toList()),
      uiInfo.getLogos().stream().map(l -> l.getWidth()).collect(Collectors.toList()));

    Assertions.assertEquals(UsageType.SIGNING, ssoDescriptor.getKeyDescriptors().get(0).getUse());
    Assertions.assertTrue(ssoDescriptor.getKeyDescriptors().get(0).getKeyInfo().getX509Datas().get(0).getX509Certificates().size() == 1);

    Assertions.assertEquals(UsageType.ENCRYPTION, ssoDescriptor.getKeyDescriptors().get(1).getUse());
    Assertions.assertTrue(ssoDescriptor.getKeyDescriptors().get(1).getKeyInfo().getX509Datas().get(0).getX509Certificates().size() == 1);

    Assertions.assertEquals(Arrays.asList(nameIDFormats),
      ssoDescriptor.getNameIDFormats().stream().map(n -> n.getURI()).collect(Collectors.toList()));

    Assertions.assertEquals(SAMLConstants.SAML2_REDIRECT_BINDING_URI, ssoDescriptor.getSingleSignOnServices().get(0).getBinding());
    Assertions.assertEquals("https://idp.svelegtest.se/idp/profile/SAML2/Redirect/SSO",
      ssoDescriptor.getSingleSignOnServices().get(0).getLocation());
    Assertions.assertEquals(SAMLConstants.SAML2_POST_BINDING_URI, ssoDescriptor.getSingleSignOnServices().get(1).getBinding());
    Assertions.assertEquals("https://idp.svelegtest.se/idp/profile/SAML2/POST/SSO",
      ssoDescriptor.getSingleSignOnServices().get(1).getLocation());

    Assertions.assertEquals(Arrays.asList(organizationNames),
      ed.getOrganization().getOrganizationNames().stream().map(n -> new LocalizedString(n.getValue(), n.getXMLLang())).collect(Collectors
        .toList()));
    Assertions.assertEquals(Arrays.asList(organizationDisplayNames),
      ed.getOrganization().getDisplayNames().stream().map(n -> new LocalizedString(n.getValue(), n.getXMLLang())).collect(Collectors
        .toList()));
    Assertions.assertEquals(List.of(organizationURL),
      ed.getOrganization().getURLs().stream().map(n -> new LocalizedString(n.getURI(), n.getXMLLang())).collect(Collectors.toList()));

    Assertions.assertEquals(2, ed.getContactPersons().size());
    Assertions.assertEquals(ContactPersonTypeEnumeration.TECHNICAL, ed.getContactPersons().get(0).getType());
    Assertions.assertEquals(ContactPersonTypeEnumeration.SUPPORT, ed.getContactPersons().get(1).getType());
    for (int i = 0; i < 2; i++) {
      Assertions.assertEquals("E-legitimationsnämnden", ed.getContactPersons().get(i).getCompany().getValue());
      Assertions
          .assertEquals(List.of("stefan@aaa-sec.com"),
        ed.getContactPersons().get(i).getEmailAddresses().stream().map(m -> m.getURI()).collect(Collectors.toList()));
    }
  }

}
