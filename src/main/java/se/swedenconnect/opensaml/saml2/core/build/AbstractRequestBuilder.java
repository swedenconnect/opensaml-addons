/*
 * Copyright 2016-2023 Sweden Connect
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
package se.swedenconnect.opensaml.saml2.core.build;

import java.time.Instant;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.RequestAbstractType;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;

/**
 * Abstract builder class for building request messages.
 *
 * @author Martin Lindström (martin@idsec.se)
 *
 * @param <T> the type of request message
 * @param <BUILDER> the builder type
 */
public abstract class AbstractRequestBuilder<T extends RequestAbstractType, BUILDER extends AbstractSAMLObjectBuilder<T>>
    extends AbstractSAMLObjectBuilder<T> {

  /** {@inheritDoc} */
  @Override
  public T build() {
    if (this.object().getVersion() == null) {
      this.object().setVersion(SAMLVersion.VERSION_20);
    }
    return super.build();
  }

  /**
   * Assigns the version attribute for the request.
   * <p>
   * If not assigned, the {@link SAMLVersion#VERSION_20} will be assigned as a default.
   * </p>
   *
   * @param major major version
   * @param minor minor version
   * @return the builder
   */
  public BUILDER version(final int major, final int minor) {
    this.object().setVersion(SAMLVersion.valueOf(major, minor));
    return this.getThis();
  }

  /**
   * Assigns the version attribute for the request.
   * <p>
   * If not assigned, the {@link SAMLVersion#VERSION_20} will be assigned as a default.
   * </p>
   *
   * @param version the versions
   * @return the builder
   */
  public BUILDER version(final String version) {
    this.object().setVersion(SAMLVersion.valueOf(version));
    return this.getThis();
  }

  /**
   * Assigns the {@code ID} attribute of the request.
   *
   * @param id the ID
   * @return the builder
   */
  public BUILDER id(final String id) {
    this.object().setID(id);
    return this.getThis();
  }

  /**
   * Assigns the issue instant.
   *
   * @param instant the issue instant
   * @return the builder
   */
  public BUILDER issueInstant(final Instant instant) {
    this.object().setIssueInstant(instant);
    return this.getThis();
  }

  /**
   * Assigns the {@code Destination} attribute of the request.
   *
   * @param destination the destination URI
   * @return the builder
   */
  public BUILDER destination(final String destination) {
    this.object().setDestination(destination);
    return this.getThis();
  }

  /**
   * Assigns the {@code Consent} attribute of the request.
   *
   * @param consent the consent string
   * @return the builder
   */
  public BUILDER consent(final String consent) {
    this.object().setConsent(consent);
    return this.getThis();
  }

  /**
   * Assigns the {@code Issuer} element of the request by adding an {@code Issuer} element having the nameID format
   * {@code urn:oasis:names:tc:SAML:2.0:nameid-format:entity}.
   *
   * @param issuer the entityID of the issuer
   * @return the builder
   * @see #issuer(Issuer)
   */
  public BUILDER issuer(final String issuer) {
    final Issuer issuerElement = (Issuer) XMLObjectSupport.buildXMLObject(Issuer.DEFAULT_ELEMENT_NAME);
    issuerElement.setValue(issuer);
    issuerElement.setFormat(NameID.ENTITY);
    this.object().setIssuer(issuerElement);
    return this.getThis();
  }

  /**
   * Assigns the {@code Issuer} element of the request.
   *
   * @param issuer the issuer (will be cloned before assignment)
   * @return the builder
   */
  public BUILDER issuer(final Issuer issuer) {
    try {
      this.object().setIssuer(XMLObjectSupport.cloneXMLObject(issuer));
    }
    catch (MarshallingException | UnmarshallingException e) {
      throw new RuntimeException(e);
    }
    return this.getThis();
  }

  /**
   * Assigns an {@code Extensions} element to the request.
   *
   * @param extensions the extensions element to add
   * @return the builder
   */
  public BUILDER extensions(final Extensions extensions) {
    this.object().setExtensions(extensions);
    return this.getThis();
  }

  /**
   * In order for us to be able to make chaining calls we need to return the concrete type of the builder.
   *
   * @return the concrete type of the builder
   */
  protected abstract BUILDER getThis();

}
