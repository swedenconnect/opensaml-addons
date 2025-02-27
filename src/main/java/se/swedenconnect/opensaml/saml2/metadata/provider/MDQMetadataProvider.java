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

import net.shibboleth.shared.collection.Pair;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.resolver.ResolverException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.persist.FilesystemLoadSaveManager;
import org.opensaml.core.xml.persist.MapLoadSaveManager;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.impl.FunctionDrivenDynamicHTTPMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * A {@link MetadataProvider} that supports the <a href="https://www.ietf.org/id/draft-young-md-query-17.html">MDQ
 * specification</a>.
 *
 * <p>
 * Note that {@link #getMetadata()}, {@link #getServiceProviders()} and {@link #getIdentityProviders()} will only return
 * those entities that have been fetched from the server using {@link #getEntityDescriptor(String)}.
 * </p>
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class MDQMetadataProvider extends AbstractMetadataProvider {

  /** The underlying {@link MetadataResolver}. */
  private final FunctionDrivenDynamicHTTPMetadataResolver resolver;

  /** Metadata as last seen at a call to {@link #getMetadata()}. */
  private EntitiesDescriptor cachedMetadata;

  /**
   * Constructor setting up a {@link MetadataProvider} that uses the MDQ protocol to download metadata for requested
   * entities.
   *
   * @param metadataBaseUrl the base metadata URL (must not end with a /)
   * @param httpClient the HTTP client instance to use, if null,
   *     {@link HTTPMetadataProvider#createDefaultHttpClient()} is used to create a default client
   * @param cacheBaseDir the base directory where caches will be stored, if null, the caches are kept in memory
   * @throws ResolverException for failures setting up the underlying {@link MetadataResolver}
   */
  public MDQMetadataProvider(
      @Nonnull final String metadataBaseUrl, @Nullable final HttpClient httpClient,
      @Nullable final String cacheBaseDir) throws ResolverException {

    this.resolver = new FunctionDrivenDynamicHTTPMetadataResolver(
        httpClient != null ? httpClient : HTTPMetadataProvider.createDefaultHttpClient());
    this.resolver.setRequestURLBuilder(new MDQRequestURLBuilder(metadataBaseUrl));

    if (cacheBaseDir != null) {
      this.resolver.setPersistentCacheManager(new FilesystemLoadSaveManager<>(cacheBaseDir));
    }
    else {
      this.resolver.setPersistentCacheManager(new MapLoadSaveManager<>());
    }
    this.resolver.setParserPool(XMLObjectProviderRegistrySupport.getParserPool());
    this.resolver.setId(metadataBaseUrl);
  }

  /** {@inheritDoc} */
  @Override
  public String getID() {
    return this.resolver.getId();
  }

  /**
   * Gets all {@link EntityDescriptor} objects available in the cache and adds them to a {@link EntitiesDescriptor}
   * object.
   * <p>
   * Note: The {@link MDQMetadataProvider} version of this method does not return all metadata entries available at the
   * source. It only returns those that have been downloaded by the provider and are present in the cache.
   * </p>
   */
  @Override
  public synchronized XMLObject getMetadata() {
    if (this.cachedMetadata != null) {
      return this.cachedMetadata;
    }
    try {
      final EntitiesDescriptor entities =
          (EntitiesDescriptor) XMLObjectSupport.buildXMLObject(EntitiesDescriptor.DEFAULT_ELEMENT_NAME);

      final Iterable<Pair<String, EntityDescriptor>> it = this.resolver.getPersistentCacheManager().listAll();
      it.forEach((e) -> entities.getEntityDescriptors().add(e.getSecond()));

      this.cachedMetadata = entities;
      return entities;
    }
    catch (final IOException e) {
      return null;
    }
  }

  /** {@inheritDoc} */
  @Override
  protected synchronized void setMetadata(final XMLObject metadata) {
    this.cachedMetadata = null;
    super.setMetadata(null);
  }

  /** {@inheritDoc} */
  @Override
  public MetadataResolver getMetadataResolver() {
    return this.resolver;
  }

  /** {@inheritDoc} */
  @Override
  protected void createMetadataResolver(final boolean requireValidMetadata, final boolean failFastInitialization,
      final MetadataFilter filter) throws ResolverException {
    this.resolver.setFailFastInitialization(failFastInitialization);
    this.resolver.setRequireValidMetadata(requireValidMetadata);
    this.resolver.setMetadataFilter(filter);
  }

  /** {@inheritDoc} */
  @Override
  protected void initializeMetadataResolver() throws ComponentInitializationException {
    this.resolver.initialize();
  }

  /** {@inheritDoc} */
  @Override
  protected void destroyMetadataResolver() {
    if (this.resolver != null) {
      this.resolver.destroy();
    }
  }

}
