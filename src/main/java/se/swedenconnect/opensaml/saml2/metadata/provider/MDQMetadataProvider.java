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
import jakarta.annotation.Nullable;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.resolver.ResolverException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.persist.FilesystemLoadSaveManager;
import org.opensaml.core.xml.persist.MapLoadSaveManager;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.impl.FunctionDrivenDynamicHTTPMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

  /** Logger instance. */
  private static final Logger log = LoggerFactory.getLogger(MDQMetadataProvider.class);

  /** The underlying {@link MetadataResolver}. */
  private final MDQResolver resolver;

  /** The MDQ base URL. */
  private final String metadataBaseUrl;

  /** The {@code HttpClient} created by this provider (null if the client was supplied). Closed on destroy. */
  private final HttpClient defaultHttpClient;

  /** Metadata as last seen at a call to {@link #getMetadata()}. */
  private EntitiesDescriptor cachedMetadata;

  /** The resolved entity descriptors that {@link #cachedMetadata} was built from. */
  private List<EntityDescriptor> cachedDescriptors;

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

    this.metadataBaseUrl = metadataBaseUrl;
    this.defaultHttpClient = httpClient == null ? HTTPMetadataProvider.createDefaultHttpClient() : null;
    this.resolver = new MDQResolver(httpClient != null ? httpClient : this.defaultHttpClient);
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
   * Gets all {@link EntityDescriptor} objects resolved by the provider and adds copies of them to a
   * {@link EntitiesDescriptor} object.
   * <p>
   * Note: The {@link MDQMetadataProvider} version of this method does not return all metadata entries available at the
   * source. It only returns those that have been downloaded by the provider and are present in the cache.
   * </p>
   */
  @Override
  public synchronized XMLObject getMetadata() {
    final List<EntityDescriptor> descriptors = this.resolver.getResolvedEntityDescriptors();
    if (this.cachedMetadata != null && isSameInstances(descriptors, this.cachedDescriptors)) {
      return this.cachedMetadata;
    }

    final EntitiesDescriptor entities =
        (EntitiesDescriptor) XMLObjectSupport.buildXMLObject(EntitiesDescriptor.DEFAULT_ELEMENT_NAME);

    // The resolved objects belong to the resolver, so we add copies of them.
    for (final EntityDescriptor descriptor : descriptors) {
      try {
        entities.getEntityDescriptors().add(XMLObjectSupport.cloneXMLObject(descriptor));
      }
      catch (final MarshallingException | UnmarshallingException e) {
        log.warn("Failed to copy metadata for '{}' - it will not be included", descriptor.getEntityID(), e);
      }
    }

    this.cachedMetadata = entities;
    this.cachedDescriptors = descriptors;
    return entities;
  }

  /**
   * Tells whether two lists hold the same object instances, in the same order.
   *
   * @param list1 the first list
   * @param list2 the second list (may be null)
   * @return true if the lists hold the same instances, and false otherwise
   */
  private static boolean isSameInstances(final List<EntityDescriptor> list1, final List<EntityDescriptor> list2) {
    if (list2 == null || list1.size() != list2.size()) {
      return false;
    }
    for (int i = 0; i < list1.size(); i++) {
      if (list1.get(i) != list2.get(i)) {
        return false;
      }
    }
    return true;
  }

  /** {@inheritDoc} */
  @Override
  protected synchronized void setMetadata(final XMLObject metadata) {
    // The metadata is built from the resolved entities in getMetadata(), so we don't save anything here.
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
    HTTPMetadataProvider.warnIfUntrusted(this.metadataBaseUrl, this.defaultHttpClient != null,
        this.getSignatureVerificationCertificates() == null);
    this.resolver.initialize();
  }

  /** {@inheritDoc} */
  @Override
  protected void destroyMetadataResolver() {
    if (this.resolver != null) {
      this.resolver.destroy();
    }
    HTTPMetadataProvider.closeHttpClient(this.defaultHttpClient);
  }

  /**
   * MDQ resolver that gives access to the entity descriptors that have been resolved (after filtering).
   */
  private static class MDQResolver extends FunctionDrivenDynamicHTTPMetadataResolver {

    /**
     * Constructor.
     *
     * @param httpClient the HTTP client
     * @throws ResolverException for errors setting up the resolver
     */
    MDQResolver(final HttpClient httpClient) throws ResolverException {
      super(httpClient);
    }

    /**
     * Gets the entity descriptors that have been resolved, sorted by entityID.
     *
     * @return a list of entity descriptors
     */
    List<EntityDescriptor> getResolvedEntityDescriptors() {
      if (!this.isInitialized() || this.isDestroyed()) {
        return Collections.emptyList();
      }
      return this.getBackingStore().getIndexedDescriptors().values().stream()
          .flatMap(List::stream)
          .sorted(Comparator.comparing(EntityDescriptor::getEntityID))
          .toList();
    }
  }

}
