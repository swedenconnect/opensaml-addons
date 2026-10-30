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
package se.swedenconnect.opensaml.saml2.metadata.provider;

import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterContext;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test cases for the {@code ProxyMetadataProvider}.
 * <p>
 * See {@link BaseMetadataProviderTest} for test cases.
 * </p>
 */
public class ProxyMetadataProviderTest extends BaseMetadataProviderTest {

  /** {@inheritDoc} */
  @Override
  protected AbstractMetadataProvider createMetadataProvider(final Resource resource) throws Exception {
    final FilesystemMetadataResolver resolver = new FilesystemMetadataResolver(resource.getFile());
    resolver.setId(resource.getFilename());
    resolver.setParserPool(XMLObjectProviderRegistrySupport.getParserPool());
    return new ProxyMetadataProvider(resolver);
  }

  @Test
  void testInstalledFilter() throws Exception {
    this.testInstalledFilter(false);
  }

  @Test
  void testInstalledFilterChain() throws Exception {
    this.testInstalledFilter(true);
  }

  private void testInstalledFilter(final boolean installChain) throws Exception {
    final Resource resource = new ClassPathResource("/metadata/sveleg-fedtest.xml");
    final FilesystemMetadataResolver resolver = new FilesystemMetadataResolver(resource.getFile());
    resolver.setId(resource.getFilename());
    resolver.setParserPool(XMLObjectProviderRegistrySupport.getParserPool());

    final AtomicBoolean installedFilterCalled = new AtomicBoolean(false);
    final MetadataFilter installedFilter = new MetadataFilter() {
      @Override
      @Nonnull
      public String getType() {
        return "TestFilter";
      }

      @Override
      public XMLObject filter(final XMLObject metadata, @Nonnull final MetadataFilterContext context) {
        installedFilterCalled.set(true);
        return metadata;
      }
    };
    if (installChain) {
      final MetadataFilterChain chain = new MetadataFilterChain();
      chain.setFilters(List.of(installedFilter));
      resolver.setMetadataFilter(chain);
    }
    else {
      resolver.setMetadataFilter(installedFilter);
    }

    final ProxyMetadataProvider provider = new ProxyMetadataProvider(resolver);
    try {
      provider.initialize();
      Assertions.assertNotNull(provider.getMetadata(), "The provider's own filters were not installed");
      Assertions.assertTrue(installedFilterCalled.get(), "The installed filter was not called");
    }
    finally {
      if (provider.isInitialized()) {
        provider.destroy();
      }
    }
  }

}
