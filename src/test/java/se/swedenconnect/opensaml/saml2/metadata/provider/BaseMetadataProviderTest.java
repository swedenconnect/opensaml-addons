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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

import net.shibboleth.shared.component.ComponentInitializationException;
import se.swedenconnect.opensaml.OpenSAMLTestBase;

/**
 * Base class for running tests for metadata providers.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public abstract class BaseMetadataProviderTest extends OpenSAMLTestBase {

  public static final String TEST_IDP = "https://idp.svelegtest.se/idp";
  public static final String TEST_SP = "https://eid.svelegtest.se/validation/testsp1";

  /**
   * Must be implemented by subclasses that creates a provider instance and assigns the metadata identified by the
   * supplied resource.
   *
   * @param resource metadata source
   * @return a provider instance
   * @throws Exception for errors
   */
  protected abstract AbstractMetadataProvider createMetadataProvider(final Resource resource) throws Exception;

  /**
   * Tests the methods that gets entity descriptors from a provider.
   *
   * @throws Exception for errors
   */
  @Test
  public void testGetMethods() throws Exception {

    final MetadataProvider provider =
        this.createMetadataProvider(new ClassPathResource("/metadata/sveleg-fedtest.xml"));

    try {
      provider.initialize();

      EntityDescriptor ed = provider.getEntityDescriptor(TEST_IDP);
      Assertions.assertNotNull(ed, String.format("EntityDescriptor for '%s' was not found", TEST_IDP));

      ed = provider.getEntityDescriptor(TEST_SP);
      Assertions.assertNotNull(ed, String.format("EntityDescriptor for '%s' was not found", TEST_SP));

      ed = provider.getEntityDescriptor(TEST_IDP, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
      Assertions.assertNotNull(ed, String.format("EntityDescriptor for '%s' was not found", TEST_IDP));

      ed = provider.getEntityDescriptor(TEST_SP, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
      Assertions.assertNotNull(ed, String.format("EntityDescriptor for '%s' was not found", TEST_SP));

      final List<EntityDescriptor> idps = provider.getIdentityProviders();
      Assertions.assertEquals(2, idps.size(), "Expected 2 IdPs");

      final List<EntityDescriptor> sps = provider.getServiceProviders();
      Assertions.assertEquals(43, sps.size(), "Expected 43 SPs");

      final XMLObject xmlObject = provider.getMetadata();
      Assertions.assertNotNull(xmlObject, "Could not get metadata XMLObject from provider");
      Assertions.assertTrue(xmlObject instanceof EntitiesDescriptor, "Expected EntitiesDescriptor");

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
   * Tests the iterator methods for the provider.
   *
   * @throws Exception for errors
   */
  @Test
  public void testIterators() throws Exception {

    // Try parsing a file with mixed EntityDescriptors and EntitiesDescriptors.
    final MetadataProvider provider =
        this.createMetadataProvider(new ClassPathResource("/metadata/sveleg-fedtest-complex.xml"));

    try {
      provider.initialize();

      final List<EntityDescriptor> list = new ArrayList<>();
      Iterable<EntityDescriptor> i = provider.iterator();
      i.forEach(list::add);
      Assertions.assertEquals(45, list.size(), "Expected 45 descriptors");

      list.clear();
      i = provider.iterator(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
      i.forEach(list::add);
      Assertions.assertEquals(2, list.size(), "Expected 2 descriptors");

      list.clear();
      i = provider.iterator(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
      i.forEach(list::add);
      Assertions.assertEquals(43, list.size(), "Expected 43 descriptors");
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
    final MetadataProvider provider =
        this.createMetadataProvider(new ClassPathResource("/metadata/sveleg-fedtest.xml"));
    try {
      provider.initialize();
      final Element dom = provider.getMetadataDOM();
      final EntitiesDescriptor ed =
          (EntitiesDescriptor) XMLObjectSupport.getUnmarshaller(dom).unmarshall(dom);
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

  /**
   * Tests schema validation filter.
   *
   * @throws Exception for errors
   */
  @Test
  public void testSchemaValidation() throws Exception {
    AbstractMetadataProvider provider =
        this.createMetadataProvider(new ClassPathResource("/metadata/sveleg-fedtest.xml"));

    try {
      provider.setFailFastInitialization(true);
      provider.setPerformSchemaValidation(true);
      provider.initialize();
      final Element dom = provider.getMetadataDOM();
      Assertions.assertNotNull(dom);
    }
    finally {
      if (provider.isInitialized()) {
        provider.destroy();
      }
    }

    provider = this.createMetadataProvider(new ClassPathResource("/metadata/sveleg-fedtest-badschema.xml"));
    try {
      provider.setFailFastInitialization(true);
      provider.setPerformSchemaValidation(true);
      provider.initialize();
      Assertions.fail("Expected schema validation error");
    }
    catch (final ComponentInitializationException e) {
    }
    finally {
      if (provider.isInitialized()) {
        provider.destroy();
      }
    }
  }

  /**
   * Tests filtering based on predicates.
   *
   * @throws Exception for errors
   */
  @ParameterizedTest
  @MethodSource("parametersFortestPredicates")
  public void testPredicates(final Predicate<EntityDescriptor> includePredicate, final Integer expectedMatches)
      throws Exception {

    final AbstractMetadataProvider provider =
        this.createMetadataProvider(new ClassPathResource("/metadata/sveleg-fedtest.xml"));
    try {
      provider.setInclusionPredicates(Collections.singletonList(includePredicate));
      provider.initialize();
      final List<EntityDescriptor> list = new ArrayList<>();
      final Iterable<EntityDescriptor> i = provider.iterator();
      i.forEach(list::add);
      Assertions.assertEquals(expectedMatches.intValue(), list.size(), String.format("Expected %d descriptors", expectedMatches));
    }
    finally {
      if (provider.isInitialized()) {
        provider.destroy();
      }
    }
  }

  private static Stream<Arguments> parametersFortestPredicates() {
    return Stream.of(
        Arguments.of(MetadataProviderPredicates.includeOnlyIDPs(), Integer.valueOf(2)),
        Arguments.of(MetadataProviderPredicates.includeOnlyIDPsAndMe(TEST_SP), Integer.valueOf(3)),
        Arguments.of(MetadataProviderPredicates.includeOnlySPs(), Integer.valueOf(43)));
  }

  /**
   * Tests for cases when we ask for non existing descriptors.
   *
   * @throws Exception for errors
   */
  @Test
  public void testNotFound() throws Exception {

    final MetadataProvider provider =
        this.createMetadataProvider(new ClassPathResource("/metadata/sveleg-fedtest.xml"));

    try {
      provider.initialize();

      EntityDescriptor ed = provider.getEntityDescriptor("http://not.an.entity");
      Assertions.assertNull(ed, "EntityDescriptor for 'http://not.an.entity' was found!!?");

      ed = provider.getEntityDescriptor(TEST_SP, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
      Assertions.assertNull(ed);

      ed = provider.getEntityDescriptor(TEST_IDP, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
      Assertions.assertNull(ed);
    }
    finally {
      if (provider.isInitialized()) {
        provider.destroy();
      }
    }
  }

}
