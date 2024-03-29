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
package se.swedenconnect.opensaml.saml2.response;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;

/**
 * Implementation of the {@code ResponseProcessingResult} interface.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ResponseProcessingResultImpl implements ResponseProcessingResult {

  /** The response ID. */
  private final String responseId;

  /** The InResponseTo attribute of the response. */
  private final String inResponseTo;

  /** The issue instant. */
  private final Instant issueInstant;

  /** The assertion. */
  private final Assertion assertion;

  /**
   * Constructor.
   *
   * @param response the Response
   * @param assertion the Assertion
   */
  public ResponseProcessingResultImpl(final Response response, final Assertion assertion) {
    this.responseId = Optional.ofNullable(response).map(Response::getID)
        .orElseThrow(() -> new NullPointerException("response is required"));
    this.inResponseTo = Optional.ofNullable(response).map(Response::getInResponseTo)
        .orElseThrow(() -> new NullPointerException("response is required"));
    this.issueInstant = Optional.ofNullable(response).map(Response::getIssueInstant)
        .orElseThrow(() -> new NullPointerException("response is required"));
    this.assertion = Objects.requireNonNull(assertion, "assertion is required");
  }

  /** {@inheritDoc} */
  @Override
  public String getResponseId() {
    return this.responseId;
  }

  /** {@inheritDoc} */
  @Override
  public String getInResponseTo() {
    return this.inResponseTo;
  }

  /** {@inheritDoc} */
  @Override
  public Instant getIssueInstant() {
    return this.issueInstant;
  }

  /** {@inheritDoc} */
  @Override
  public Assertion getAssertion() {
    return this.assertion;
  }

  /** {@inheritDoc} */
  @Override
  public List<Attribute> getAttributes() {
    try {
      return Collections.unmodifiableList(this.assertion.getAttributeStatements().get(0).getAttributes());
    }
    catch (NullPointerException | IndexOutOfBoundsException e) {
      return Collections.emptyList();
    }
  }

  /** {@inheritDoc} */
  @Override
  public String getAuthnContextClassUri() {
    try {
      return this.assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getURI();
    }
    catch (NullPointerException e) {
      return null;
    }
  }

  /** {@inheritDoc} */
  @Override
  public Instant getAuthnInstant() {

    Instant authnInstant = this.assertion.getAuthnStatements().get(0).getAuthnInstant();

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
