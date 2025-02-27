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
package se.swedenconnect.opensaml.saml2.metadata;

import net.shibboleth.shared.security.RandomIdentifierParameterSpec;
import net.shibboleth.shared.security.impl.RandomIdentifierGenerationStrategy;
import org.apache.commons.codec.binary.Hex;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.common.CacheableSAMLObject;
import org.opensaml.saml.saml2.common.TimeBoundSAMLObject;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import se.swedenconnect.opensaml.xmlsec.signature.support.SAMLObjectSigner;

import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Abstract base class for the {@link MetadataContainer} interface.
 *
 * @param <T> the contained type
 * @author Martin Lindström (martin@idsec.se)
 */
public abstract class AbstractMetadataContainer<T extends TimeBoundSAMLObject & SignableSAMLObject & CacheableSAMLObject>
    implements MetadataContainer<T> {

  /** The default validity for metadata - one week. */
  public static final Duration DEFAULT_VALIDITY = Duration.ofDays(7);

  /**
   * The default update factor for the metadata - 0,75 (75%), i.e. "update the metadata when less than 75% of its
   * original validity time remains".
   *
   * @see #getUpdateFactor()
   */
  public static final float DEFAULT_UPDATE_FACTOR = 0.75f;

  /** Default size for the ID attribute string. */
  public static final int DEFAULT_DESCRIPTOR_ID_SIZE = 32;

  /** Logging instance. */
  private static final Logger log = LoggerFactory.getLogger(AbstractMetadataContainer.class);

  /** The encapsulated descriptor element. */
  protected T descriptor;

  /** The validity time for created entries. */
  protected Duration validity = DEFAULT_VALIDITY;

  /** The update factor. */
  protected float updateFactor = DEFAULT_UPDATE_FACTOR;

  /** The size of the ID attribute string. */
  protected int idSize = DEFAULT_DESCRIPTOR_ID_SIZE;

  /** The signature credentials for signing the metadata entry. */
  protected X509Credential signatureCredentials;

  /** Optional signing configuration. */
  protected SignatureSigningConfiguration signingConfiguration;

  /**
   * Constructor assigning the encapsulated descriptor element.
   *
   * @param descriptor the descriptor object
   * @param signatureCredentials the signature credentials for signing the descriptor. May be null, but then no
   *     signing will be possible
   */
  public AbstractMetadataContainer(final T descriptor, final X509Credential signatureCredentials) {
    this.descriptor = descriptor;
    this.signatureCredentials = signatureCredentials;
  }

  /** {@inheritDoc} */
  @Override
  public T getDescriptor() {
    return this.descriptor;
  }

  /** {@inheritDoc} */
  @Override
  public T cloneDescriptor() throws MarshallingException, UnmarshallingException {
    return XMLObjectSupport.cloneXMLObject(this.descriptor);
  }

  /** {@inheritDoc} */
  @Override
  public boolean updateRequired(final boolean signatureRequired) {
    if (!this.descriptor.isValid() || this.descriptor.getValidUntil() == null) {
      return true;
    }
    if (signatureRequired && this.signatureCredentials != null && !this.descriptor.isSigned()) {
      return true;
    }

    final long expireInstant = this.descriptor.getValidUntil().toEpochMilli();
    final long now = System.currentTimeMillis();

    return this.updateFactor * this.validity.toMillis() > expireInstant - now;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized T update(final boolean sign) throws SignatureException, MarshallingException {

    // Reset the signature
    this.descriptor.setSignature(null);

    // Generate a new ID.
    final RandomIdentifierGenerationStrategy generator;
    try {
      generator = new RandomIdentifierGenerationStrategy(
          new RandomIdentifierParameterSpec(new SecureRandom(), this.idSize, new Hex()));
    }
    catch (final InvalidAlgorithmParameterException e) {
      throw new RuntimeException(e);
    }

    this.assignID(this.descriptor, generator.generateIdentifier(true));

    // Assign the validity.
    final Instant validUntil = Instant.now().plusSeconds((int) this.validity.getSeconds());
    this.descriptor.setValidUntil(validUntil);

    log.debug("Descriptor '{}' was updated with ID '{}' and validUntil '{}'",
        this.getLogString(this.descriptor), this.getID(this.descriptor), this.descriptor.getValidUntil().toString());

    return sign && this.signatureCredentials != null ? this.sign() : this.descriptor;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized T sign() throws SignatureException, MarshallingException {

    log.trace("Signing descriptor '{}' ...", this.getLogString(this.descriptor));

    if (this.getID(this.descriptor) == null || this.descriptor.getValidUntil() == null) {
      return this.update(true);
    }

    SAMLObjectSigner.sign(this.descriptor, this.signatureCredentials,
        Optional.ofNullable(this.signingConfiguration).orElseGet(
            SecurityConfigurationSupport::getGlobalSignatureSigningConfiguration));

    log.debug("Descriptor '{}' successfully signed.", this.getLogString(this.descriptor));

    return this.descriptor;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized Element marshall() throws MarshallingException {
    return XMLObjectSupport.marshall(this.descriptor);
  }

  /** {@inheritDoc} */
  @Override
  public Duration getValidity() {
    return this.validity;
  }

  /**
   * Assigns the duration of the validity that the encapsulated {@code EntityDescriptor} should have.
   * <p>
   * The default value is {@link #DEFAULT_VALIDITY}.
   * </p>
   *
   * @param validity the validity
   */
  public void setValidity(final Duration validity) {
    this.validity = validity;
  }

  /** {@inheritDoc} */
  @Override
  public float getUpdateFactor() {
    return this.updateFactor;
  }

  /**
   * Assigns the factor (between 0 and 1) that is used to compute whether it is time to update the contained
   * descriptor.
   * <p>
   * The default value is {@link #DEFAULT_UPDATE_FACTOR}.
   * </p>
   *
   * @param updateFactor the update factor
   * @see #getUpdateFactor()
   */
  public void setUpdateFactor(final float updateFactor) {
    if (updateFactor < 0 || updateFactor > 1) {
      throw new IllegalArgumentException("Supplied updateFactor must be greater than 0 and equal or less than 1");
    }
    this.updateFactor = updateFactor;
  }

  /**
   * Returns the size of the ID attribute that is generated.
   *
   * @return the size
   */
  public int getIdSize() {
    return this.idSize;
  }

  /**
   * Assigns the size of the ID attribute that is generated.
   *
   * <p>
   * The default value is {@link #DEFAULT_DESCRIPTOR_ID_SIZE}.
   * </p>
   *
   * @param idSize the size
   */
  public void setIdSize(final int idSize) {
    this.idSize = idSize;
  }

  /**
   * Assigns a custom {@link SignatureSigningConfiguration}.
   *
   * @param signingConfiguration a {@link SignatureSigningConfiguration}
   */
  public void setSigningConfiguration(final SignatureSigningConfiguration signingConfiguration) {
    this.signingConfiguration = signingConfiguration;
  }

  /**
   * Returns the ID attribute of the supplied descriptor.
   *
   * @param descriptor the descriptor
   * @return the ID attribute
   */
  protected abstract String getID(final T descriptor);

  /**
   * Assigns the supplied id to the ID attribute of the descriptor.
   *
   * @param descriptor the descriptor
   * @param id the ID attribute value
   */
  protected abstract void assignID(final T descriptor, final String id);

  /**
   * Returns a log string of the supplied descriptor.
   *
   * @param descriptor the descriptor
   * @return the log string
   */
  protected abstract String getLogString(final T descriptor);

}
