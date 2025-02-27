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
package se.swedenconnect.opensaml.saml2.response;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of the {@code ResponseProcessingResult} interface.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ResponseProcessingResultImpl implements ResponseProcessingResult {

  /** The response. */
  private final Response response;

  /** The assertion. */
  private final Assertion assertion;

  /**
   * Constructor.
   *
   * @param response the Response
   * @param assertion the Assertion
   */
  public ResponseProcessingResultImpl(final Response response, final Assertion assertion) {
    this.response = Objects.requireNonNull(response, "response is required");
    this.assertion = Objects.requireNonNull(assertion, "assertion is required");
  }

  /** {@inheritDoc} */
  @Override
  public Response getResponse() {
    return this.response;
  }

  /** {@inheritDoc} */
  @Override
  public String getResponseId() {
    return this.response.getID();
  }

  /** {@inheritDoc} */
  @Override
  public String getInResponseTo() {
    return this.response.getInResponseTo();
  }

  /** {@inheritDoc} */
  @Override
  public Instant getIssueInstant() {
    return this.response.getIssueInstant();
  }

  /** {@inheritDoc} */
  @Override
  public Assertion getAssertion() {
    return this.assertion;
  }

  /** {@inheritDoc} */
  @Override
  public List<Attribute> getAttributes() {
    return Collections.unmodifiableList(this.assertion.getAttributeStatements().stream()
        .map(AttributeStatement::getAttributes)
        .findFirst()
        .orElseGet(Collections::emptyList));
  }

  /** {@inheritDoc} */
  @Override
  public String getAuthnContextClassUri() {
    return this.assertion.getAuthnStatements().stream()
        .map(AuthnStatement::getAuthnContext)
        .map(AuthnContext::getAuthnContextClassRef)
        .map(AuthnContextClassRef::getURI)
        .findFirst()
        .orElse(null);
  }

  /** {@inheritDoc} */
  @Override
  public Instant getAuthnInstant() {

    final Instant authnInstant = this.assertion.getAuthnStatements().stream()
        .map(AuthnStatement::getAuthnInstant)
        .findFirst()
        .orElseGet(Instant::now);

    // We have already checked the validity of the authentication instant, but if it is
    // after the current time it means that it is within the allowed clock skew. If so,
    // we set it to the current time (it's the best we can do).
    //
    if (authnInstant.isAfter(Instant.now())) {
      return Instant.now();
    }

    return authnInstant;
  }

  /** {@inheritDoc} */
  @Override
  public String getIssuer() {
    return Optional.ofNullable(this.assertion.getIssuer()).map(Issuer::getValue).orElse(null);
  }

  /** {@inheritDoc} */
  @Override
  public NameID getSubjectNameID() {
    return Optional.ofNullable(this.assertion.getSubject()).map(Subject::getNameID).orElse(null);
  }

}
