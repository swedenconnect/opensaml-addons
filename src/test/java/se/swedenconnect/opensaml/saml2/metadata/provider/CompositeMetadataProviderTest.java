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
package se.swedenconnect.opensaml.saml2.metadata.provider;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;
import se.swedenconnect.opensaml.OpenSAMLTestBase;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Test cases for the {@code CompositeMetadataProvider} class.
 *
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
public class CompositeMetadataProviderTest extends OpenSAMLTestBase {

  private static final String TEST_IDP = "https://idp.svelegtest.se/idp";
  private static final String TEST_SP = "https://eid.svelegtest.se/validation/testsp1";

  private static final Resource part1 = new ClassPathResource("/metadata/sveleg-fedtest-part1.xml");
  private static final Resource part2 = new ClassPathResource("/metadata/sveleg-fedtest-part2.xml");
  private static final Resource part3 = new ClassPathResource("/metadata/sveleg-fedtest-part3.xml");

  private static MetadataProvider entireMetadataProvider;

  /**
   * We use a simple StaticMetadataProvider to hold the entire metadata.
   *
   * @throws Exception for errors
   */
  @BeforeAll
  public static void setup() throws Exception {
    final Resource entireMetadata = new ClassPathResource("/metadata/sveleg-fedtest.xml");
    final Element entireMetadataDOM = XMLObjectProviderRegistrySupport.getParserPool()
        .parse(entireMetadata.getInputStream())
        .getDocumentElement();
    entireMetadataProvider = new StaticMetadataProvider(entireMetadataDOM);
    entireMetadataProvider.initialize();
  }

  /**
   * Destroys the metadata provider used.
   */
  @AfterAll
  public static void tearDown() {
    if (entireMetadataProvider != null && entireMetadataProvider.isInitialized()) {
      entireMetadataProvider.destroy();
    }
  }

  /**
   * We split /metadata/sveleg-fedtest.xml into three parts and verify the the {@code CompositeMetadataProvider} can
   * access all metadata using three different underlying providers.
   *
   * @throws Exception for errors
   */
  @Test
  public void testCompositeBasic() throws Exception {

    // Setup the composite provider with three providers (for the three parts)
    //
    final CompositeMetadataProvider provider =
        new CompositeMetadataProvider("MetadataService", Arrays.asList(new FilesystemMetadataProvider(part1.getFile()),
            new FilesystemMetadataProvider(part2.getFile()), new FilesystemMetadataProvider(part3.getFile())));

    try {
      provider.initialize();

      EntityDescriptor ed = provider.getEntityDescriptor(TEST_IDP);
      Assertions.assertNotNull(ed, String.format("EntityDescriptor for '%s' was not found", TEST_IDP));

      ed = provider.getEntityDescriptor(TEST_SP);
      Assertions.assertNotNull(ed, String.format("EntityDescriptor for '%s' was not found", TEST_SP));

      ed = provider.getEntityDescriptor(TEST_IDP, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
      Assertions.assertNotNull(ed, String.format("IDPSSODescriptor for '%s' was not found", TEST_IDP));

      ed = provider.getEntityDescriptor(TEST_SP, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
      Assertions.assertNotNull(ed, String.format("SPSSODescriptor for '%s' was not found", TEST_SP));

      final List<EntityDescriptor> idps = provider.getIdentityProviders();
      Assertions.assertEquals(2, idps.size(), "Expected 2 IdPs");

      final List<EntityDescriptor> sps = provider.getServiceProviders();
      Assertions.assertEquals(43, sps.size(), "Expected 43 SPs");

      final XMLObject xmlObject = provider.getMetadata();
      Assertions.assertNotNull(xmlObject, "Could not get metadata XMLObject from provider");
      Assertions.assertTrue(xmlObject instanceof EntitiesDescriptor, "Expected EntitiesDescriptor");

      // Make sure that no signature is there, and so on
      final EntitiesDescriptor metadata = (EntitiesDescriptor) xmlObject;
      Assertions.assertNull(metadata.getSignature(), "Expected no signature");
      Assertions.assertEquals(provider.getID(), metadata.getName());
      Assertions.assertNotNull("Expected ID to be assigned", metadata.getID());

      final Element xml = provider.getMetadataDOM();
      Assertions.assertNotNull(xml, "Could not get metadata DOM from provider");
    }
    finally {
      if (provider.isInitialized()) {
        provider.destroy();
      }
    }
  }

  /**
   * Tests getting the DOM of the entire metadata held by the provider.
   *
   * @throws Exception for errors
   */
  @Test
  public void testDOM() throws Exception {
    final CompositeMetadataProvider provider =
        new CompositeMetadataProvider("MetadataService", Arrays.asList(new FilesystemMetadataProvider(part1.getFile()),
            new FilesystemMetadataProvider(part2.getFile()), new FilesystemMetadataProvider(part3.getFile())));

    try {
      provider.initialize();
      final Element dom = provider.getMetadataDOM();
      final EntitiesDescriptor ed = (EntitiesDescriptor) XMLObjectSupport.getUnmarshaller(dom).unmarshall(dom);
      for (final EntityDescriptor e : ed.getEntityDescriptors()) {
        final EntityDescriptor e2 = provider.getEntityDescriptor(e.getEntityID());
        Assertions.assertNotNull(e2, String.format("EntityDescriptor for '%s' was not found", e.getEntityID()));
      }
    }
    finally {
      if (provider.isInitialized()) {
        provider.destroy();
      }
    }
  }

  @Test
  public void testValidUntil() throws Exception {

    final Instant shortestValidity = Instant.parse("2028-01-01T12:00:00.00Z");
    final Duration shortestCacheDuration = Duration.of(7, ChronoUnit.DAYS);

    final EntitiesDescriptor one = unmarshall(part1.getInputStream(), EntitiesDescriptor.class);
    one.setValidUntil(shortestValidity.plus(365, ChronoUnit.DAYS));
    one.setCacheDuration(shortestCacheDuration.plus(1, ChronoUnit.DAYS));

    final EntitiesDescriptor two = unmarshall(part2.getInputStream(), EntitiesDescriptor.class);
    two.setValidUntil(shortestValidity);
    two.setCacheDuration(shortestCacheDuration.plus(2, ChronoUnit.DAYS));

    final EntitiesDescriptor three = unmarshall(part3.getInputStream(), EntitiesDescriptor.class);
    three.setValidUntil(shortestValidity.plus(2 * 365, ChronoUnit.DAYS));
    three.setCacheDuration(shortestCacheDuration);

    final CompositeMetadataProvider provider = new CompositeMetadataProvider("MetadataService",
        Arrays.asList(
            new StaticMetadataProvider(one),
            new StaticMetadataProvider(two),
            new StaticMetadataProvider(three)));

    try {
      provider.initialize();

      final EntitiesDescriptor metadata = (EntitiesDescriptor) provider.getMetadata();
      Assertions.assertEquals(shortestValidity, metadata.getValidUntil());
      Assertions.assertEquals(shortestCacheDuration, metadata.getCacheDuration());
    }
    finally {
      if (provider.isInitialized()) {
        provider.destroy();
      }
    }

    final CompositeMetadataProvider provider2 = new CompositeMetadataProvider("MetadataService",
        Arrays.asList(
            new StaticMetadataProvider(one),
            new StaticMetadataProvider(two),
            new StaticMetadataProvider(three)));

    final Duration twoDays = Duration.of(2, ChronoUnit.DAYS);
    provider2.setValidity(twoDays);
    provider2.setCacheDuration(twoDays);

    try {
      provider2.initialize();
      final Instant now = Instant.now();

      final EntitiesDescriptor metadata = (EntitiesDescriptor) provider2.getMetadata();
      Assertions.assertTrue(metadata.getValidUntil().isBefore(now.plus(twoDays).plus(10, ChronoUnit.SECONDS)));
      Assertions.assertEquals(twoDays, metadata.getCacheDuration());
    }
    finally {
      if (provider2.isInitialized()) {
        provider2.destroy();
      }
    }
  }

}
