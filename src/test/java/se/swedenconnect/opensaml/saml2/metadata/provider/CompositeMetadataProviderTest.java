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
package se.swedenconnect.opensaml.saml2.metadata.provider;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
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
   * @throws Exception
   *           for errors
   */
  @BeforeClass
  public static void setup() throws Exception {
    Resource entireMetadata = new ClassPathResource("/metadata/sveleg-fedtest.xml");
    Element entireMetadataDOM = XMLObjectProviderRegistrySupport.getParserPool()
      .parse(entireMetadata.getInputStream())
      .getDocumentElement();
    entireMetadataProvider = new StaticMetadataProvider(entireMetadataDOM);
    entireMetadataProvider.initialize();
  }

  /**
   * Destroys the metadata provider used.
   * 
   * @throws Exception
   *           for errors
   */
  @AfterClass
  public static void tearDown() throws Exception {
    if (entireMetadataProvider != null && entireMetadataProvider.isInitialized()) {
      entireMetadataProvider.destroy();
    }
  }

  /**
   * We split /metadata/sveleg-fedtest.xml into three parts and verify the the {@code CompositeMetadataProvider} can
   * access all metadata using three different underlying providers.
   * 
   * @throws Exception
   *           for errors
   */
  @Test
  public void testCompositeBasic() throws Exception {

    // Setup the composite provider with three providers (for the three parts)
    //
    CompositeMetadataProvider provider =
        new CompositeMetadataProvider("MetadataService", Arrays.asList(new FilesystemMetadataProvider(part1.getFile()),
          new FilesystemMetadataProvider(part2.getFile()), new FilesystemMetadataProvider(part3.getFile())));

    try {
      provider.initialize();

      EntityDescriptor ed = provider.getEntityDescriptor(TEST_IDP);
      Assert.assertNotNull(String.format("EntityDescriptor for '%s' was not found", TEST_IDP), ed);

      ed = provider.getEntityDescriptor(TEST_SP);
      Assert.assertNotNull(String.format("EntityDescriptor for '%s' was not found", TEST_SP), ed);

      ed = provider.getEntityDescriptor(TEST_IDP, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
      Assert.assertNotNull(String.format("IDPSSODescriptor for '%s' was not found", TEST_IDP), ed);

      ed = provider.getEntityDescriptor(TEST_SP, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
      Assert.assertNotNull(String.format("SPSSODescriptor for '%s' was not found", TEST_SP), ed);

      List<EntityDescriptor> idps = provider.getIdentityProviders();
      Assert.assertEquals("Expected 2 IdPs", 2, idps.size());

      List<EntityDescriptor> sps = provider.getServiceProviders();
      Assert.assertEquals("Expected 43 SPs", 43, sps.size());

      XMLObject xmlObject = provider.getMetadata();
      Assert.assertNotNull("Could not get metadata XMLObject from provider", xmlObject);
      Assert.assertTrue("Expected EntitiesDescriptor", xmlObject instanceof EntitiesDescriptor);

      // Make sure that no signature is there, and so on
      EntitiesDescriptor metadata = (EntitiesDescriptor) xmlObject;
      Assert.assertNull("Expected no signature", metadata.getSignature());
      Assert.assertEquals(provider.getID(), metadata.getName());
      Assert.assertNotNull("Expected ID to be assigned", metadata.getID());

      Element xml = provider.getMetadataDOM();
      Assert.assertNotNull("Could not get metadata DOM from provider", xml);
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
   * @throws Exception
   *           for errors
   */
  @Test
  public void testDOM() throws Exception {
    CompositeMetadataProvider provider =
        new CompositeMetadataProvider("MetadataService", Arrays.asList(new FilesystemMetadataProvider(part1.getFile()),
          new FilesystemMetadataProvider(part2.getFile()), new FilesystemMetadataProvider(part3.getFile())));

    try {
      provider.initialize();
      Element dom = provider.getMetadataDOM();
      EntitiesDescriptor ed = EntitiesDescriptor.class.cast(XMLObjectSupport.getUnmarshaller(dom).unmarshall(dom));
      for (EntityDescriptor e : ed.getEntityDescriptors()) {
        EntityDescriptor e2 = provider.getEntityDescriptor(e.getEntityID());
        Assert.assertNotNull(String.format("EntityDescriptor for '%s' was not found", e.getEntityID()), e2);
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

    final Instant shortestValidity = Instant.parse("2025-01-01T12:00:00.00Z");
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
      Assert.assertEquals(shortestValidity, metadata.getValidUntil());
      Assert.assertEquals(shortestCacheDuration, metadata.getCacheDuration());      
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
      Assert.assertTrue(metadata.getValidUntil().isBefore(now.plus(twoDays).plus(10, ChronoUnit.SECONDS))); 
      Assert.assertEquals(twoDays, metadata.getCacheDuration());      
    }
    finally {
      if (provider2.isInitialized()) {
        provider2.destroy();
      }
    }
  }

}
