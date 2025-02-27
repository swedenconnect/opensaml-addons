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
package se.swedenconnect.opensaml.saml2.assertion.validation;

import org.opensaml.saml.common.assertion.ValidationContext;
import org.opensaml.saml.saml2.assertion.SAML2AssertionValidationParameters;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.swedenconnect.opensaml.common.validation.CoreValidatorParameters;
import se.swedenconnect.opensaml.saml2.response.validation.AbstractResponseValidationParametersBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract builder class for building the {@link ValidationContext} object for use as validation input to the
 * {@link AssertionValidator}.
 *
 * <p>
 * The reason for all fiddling with generics is that we want to be able to subclass the builder classes.
 * </p>
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public abstract class AbstractAssertionValidationParametersBuilder<T extends AbstractAssertionValidationParametersBuilder<T>>
    extends AbstractResponseValidationParametersBuilder<T> {

  /** Logging instance. */
  private static final Logger log = LoggerFactory.getLogger(AbstractAssertionValidationParametersBuilder.class);

  /**
   * Adds default settings before invoking the super implementation.
   */
  @Override
  public ValidationContext build() {
    this.addStaticParameterIfMissing(SAML2AssertionValidationParameters.SC_CHECK_ADDRESS, Boolean.FALSE);
    this.addStaticParameterIfMissing(SAML2AssertionValidationParameters.STMT_AUTHN_CHECK_ADDRESS, Boolean.FALSE);

    return super.build();
  }

  /**
   * Assigns the issue instant from the Response message that contained the assertion being validated.
   *
   * @param instant the response issue instant
   * @return the builder
   */
  public T responseIssueInstant(final Instant instant) {
    return this.staticParameter(AssertionValidator.RESPONSE_ISSUE_INSTANT, instant);
  }

  /**
   * Assigns the issue instant from the Response message that contained the assertion being validated.
   *
   * @param instant the response issue instant
   * @return the builder
   */
  public T responseIssueInstant(final long instant) {
    return this.responseIssueInstant(Instant.ofEpochMilli(instant));
  }

  /**
   * Assigns the valid URLs for the intended recipients.
   *
   * @param recipients one or more URLs
   * @return the builder
   */
  public T validRecipients(final String... recipients) {
    if (recipients != null) {
      final Set<String> set = new HashSet<>();
      Collections.addAll(set, recipients);
      this.staticParameter(SAML2AssertionValidationParameters.SC_RECIPIENT_REQUIRED, Boolean.TRUE);
      return this.staticParameter(SAML2AssertionValidationParameters.SC_VALID_RECIPIENTS, set);
    }
    else {
      return this.getThis();
    }
  }

  /**
   * Assigns the valid addresses that we allow the user agent to have.
   * <p>
   * See also {@link #subjectConfirmationCheckAddess(boolean)} and {@link #subjectLocalityCheckAddress(boolean)}.
   * </p>
   *
   * @param addresses one or more IP addresses
   * @return the builder
   */
  public T validAddresses(final InetAddress... addresses) {
    if (addresses != null) {
      final Set<InetAddress> set = new HashSet<>();
      Collections.addAll(set, addresses);
      if (!set.isEmpty()) {
        this.staticParameter(SAML2AssertionValidationParameters.SC_VALID_ADDRESSES, set);
        this.staticParameter(SAML2AssertionValidationParameters.STMT_AUTHN_VALID_ADDRESSES, set);
      }
    }
    return this.getThis();
  }

  /**
   * Assigns the valid addresses that we allow the user agent to have.
   *
   * @param addresses one or more IP addresses (in string format)
   * @return the builder
   */
  public T validAddresses(final String... addresses) {
    if (addresses == null) {
      return this.getThis();
    }
    final List<InetAddress> _addresses = new ArrayList<>();
    for (final String a : addresses) {
      try {
        _addresses.add(InetAddress.getByName(a));
      }
      catch (final UnknownHostException e) {
        log.error("Invalid IP address - {}", a, e);
        return this.getThis();
      }
    }
    return this.validAddresses(_addresses.toArray(new InetAddress[0]));
  }

  public T subjectConfirmationCheckAddess(final boolean flag) {
    return this.staticParameter(SAML2AssertionValidationParameters.SC_CHECK_ADDRESS, flag);
  }

  public T subjectLocalityCheckAddress(final boolean flag) {
    return this.staticParameter(SAML2AssertionValidationParameters.STMT_AUTHN_CHECK_ADDRESS, flag);
  }

  /**
   * Assigns the entityID:s of the valid audiences.
   *
   * @param audiences the audiences
   * @return the builder
   */
  public T validAudiences(final String... audiences) {
    if (audiences != null) {
      final Set<String> set = new HashSet<>();
      Collections.addAll(set, audiences);
      return this.staticParameter(SAML2AssertionValidationParameters.COND_VALID_AUDIENCES, set);
    }
    else {
      return this.getThis();
    }
  }

  /**
   * Assigns the {@code ForceAuthn} flag from the corresponding {@code AuthnRequest}.
   *
   * @param forceAuthn true/false
   * @return the builder
   */
  public T authnRequestForceAuthn(final Boolean forceAuthn) {
    return this.staticParameter(AuthnStatementValidator.AUTHN_REQUEST_FORCE_AUTHN, forceAuthn);
  }

  /**
   * Assigns the issuance time for the corresponding {@code AuthnRequest} when validating an assertion.
   *
   * @param issueInstant issue time
   * @return the builder
   */
  public T authnRequestIssueInstant(final Instant issueInstant) {
    return this.staticParameter(AuthnStatementValidator.AUTHN_REQUEST_ISSUE_INSTANT, issueInstant);
  }

  /**
   * Assigns the issuance time for the corresponding {@code AuthnRequest} when validating an assertion.
   *
   * @param issueInstant issue time (in milliseconds since epoch)
   * @return the builder
   */
  public T authnRequestIssueInstant(final long issueInstant) {
    return this.authnRequestIssueInstant(Instant.ofEpochMilli(issueInstant));
  }

  /**
   * Assigns the maximum session time that we, as an SP, can accept when receiving assertions based on older
   * authentications (SSO).
   *
   * @param duration milliseconds
   * @return the builder
   */
  public T maxAcceptedSsoSessionTime(final long duration) {
    return this.maxAcceptedSsoSessionTime(Duration.ofMillis(duration));
  }

  /**
   * Assigns the maximum session time that we, as an SP, can accept when receiving assertions based on older
   * authentications (SSO).
   *
   * @param duration max session time
   * @return the builder
   */
  public T maxAcceptedSsoSessionTime(final Duration duration) {
    return this.staticParameter(AuthnStatementValidator.MAX_ACCEPTED_SSO_SESSION_TIME, duration);
  }

  public T inResponseTo(final String id) {
    this.addStaticParameter(SAML2AssertionValidationParameters.SC_IN_RESPONSE_TO_REQUIRED, Boolean.TRUE);
    return this.staticParameter(SAML2AssertionValidationParameters.SC_VALID_IN_RESPONSE_TO, id);
  }

  /**
   * Assigns the client certificate to be used for HoK validation.
   *
   * @param clientCertificate the client certificate
   * @return the builder
   */
  public T clientCertificate(final X509Certificate clientCertificate) {
    return this.staticParameter(SAML2AssertionValidationParameters.SC_HOK_PRESENTER_CERT, clientCertificate);
  }

  /**
   * Assigns the SP metadata.
   *
   * @param metadata the SP metadata
   * @return the builder
   */
  public T spMetadata(final EntityDescriptor metadata) {
    return this.staticParameter(CoreValidatorParameters.SP_METADATA, metadata);
  }

  /**
   * Assigns the SP metadata.
   *
   * @param metadata the SP metadata
   * @return the builder
   */
  public T idpMetadata(final EntityDescriptor metadata) {
    return this.staticParameter(CoreValidatorParameters.IDP_METADATA, metadata);
  }

}
