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
package se.swedenconnect.opensaml.saml2.request;

import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.ResolverException;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * A default implementation of the {@link AuthnRequestGenerator} where a metadata resolver is used to locate metadata.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class DefaultAuthnRequestGenerator extends AbstractAuthnRequestGenerator {

  /** Logging instance. */
  private static final Logger log = LoggerFactory.getLogger(DefaultAuthnRequestGenerator.class);

  /** The metadata resolver. */
  private final MetadataResolver metadataResolver;

  private EntityDescriptor spMetadata;

  /**
   * Constructor.
   *
   * @param spEntityID the SP entityID
   * @param signCredential the signing credential
   * @param metadataResolver the metadata resolver
   */
  public DefaultAuthnRequestGenerator(final String spEntityID, final X509Credential signCredential,
      final MetadataResolver metadataResolver) {
    super(spEntityID, signCredential);
    this.metadataResolver = Optional.ofNullable(metadataResolver).orElseThrow(
        () -> new IllegalArgumentException("metadataResolver must not be null"));
  }

  /**
   * Constructor.
   *
   * @param spMetadata the SP metadata
   * @param signCredential the signing credential
   * @param metadataResolver the metadata resolver
   */
  public DefaultAuthnRequestGenerator(final EntityDescriptor spMetadata, final X509Credential signCredential,
      final MetadataResolver metadataResolver) {
    super(spMetadata.getEntityID(), signCredential);
    this.metadataResolver = Optional.ofNullable(metadataResolver).orElseThrow(
        () -> new IllegalArgumentException("metadataResolver must not be null"));
    this.spMetadata = spMetadata;
  }

  /**
   * Gets the metadata resolver that this generator uses to find IdP (and SP) metadata.
   *
   * @return a metadata resolver
   */
  protected MetadataResolver getMetadataResolver() {
    return this.metadataResolver;
  }

  /** {@inheritDoc} */
  @Override
  protected EntityDescriptor getSpMetadata() {
    if (this.spMetadata != null) {
      return this.spMetadata;
    }
    try {
      final CriteriaSet criteria = new CriteriaSet();
      criteria.add(new EntityIdCriterion(this.getSpEntityID()));
      final EntityDescriptor ed = this.getMetadataResolver().resolveSingle(criteria);
      if (ed == null) {
        log.warn("Metadata for {} was not found", this.getSpEntityID());
        return null;
      }
      if (ed.getRoleDescriptors(SPSSODescriptor.DEFAULT_ELEMENT_NAME).isEmpty()) {
        log.warn("Metadata for {} was found, but is not valid metadata for a SP", this.getSpEntityID());
        return null;
      }
      return ed;
    }
    catch (final ResolverException e) {
      log.warn("Metadata for {} could not be resolved", this.getSpEntityID(), e);
      return null;
    }
  }

  /** {@inheritDoc} */
  @Override
  protected EntityDescriptor getIdpMetadata(final String idpEntityID) {
    try {
      final CriteriaSet criteria = new CriteriaSet();
      criteria.add(new EntityIdCriterion(idpEntityID));
      final EntityDescriptor ed = this.getMetadataResolver().resolveSingle(criteria);
      if (ed == null) {
        log.warn("Metadata for {} was not found", idpEntityID);
        return null;
      }
      if (ed.getRoleDescriptors(IDPSSODescriptor.DEFAULT_ELEMENT_NAME).isEmpty()) {
        log.warn("Metadata for {} was found, but is not valid metadata for an IdP", idpEntityID);
        return null;
      }
      return ed;
    }
    catch (final ResolverException e) {
      log.warn("Metadata for {} could not be resolved", idpEntityID, e);
      return null;
    }
  }

}
