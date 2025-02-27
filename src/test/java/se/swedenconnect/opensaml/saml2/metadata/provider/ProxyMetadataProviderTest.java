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

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import org.springframework.core.io.Resource;

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

}
