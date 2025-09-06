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
package se.swedenconnect.opensaml.saml2.response.validation;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import se.swedenconnect.opensaml.common.LibraryVersion;
import se.swedenconnect.opensaml.saml2.response.ResponseProcessingException;

import java.io.Serial;
import java.util.Collections;
import java.util.List;

/**
 * Exception class for response validation errors.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ResponseValidationException extends ResponseProcessingException {

  /** For serializing. */
  @Serial
  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /** Validation errors. */
  private final List<String> validationErrors;

  /**
   * Constructor taking an error message.
   *
   * @param message the error message
   * @deprecated Also supply the response message
   */
  @Deprecated(forRemoval = true)
  public ResponseValidationException(@Nonnull final String message) {
    super(message);
    this.validationErrors = null;
  }

  /**
   * Constructor taking an error message and the response and assertion being processed.
   *
   * @param message the error message
   * @param response the response being processed
   * @param assertion the assertion being processed
   */
  public ResponseValidationException(@Nonnull final String message, @Nullable final Response response,
      @Nullable final Assertion assertion) {
    super(message, response, assertion);
    this.validationErrors = null;
  }

  /**
   * Constructor taking a list of validation messages and the response and assertion being processed.
   *
   * @param validationErrors the validation errors
   * @param response the response being processed
   * @param assertion the assertion being processed
   */
  public ResponseValidationException(@Nonnull final List<String> validationErrors, @Nullable final Response response,
      @Nullable final Assertion assertion) {
    super(buildExceptionMessage(validationErrors), response, assertion);
    this.validationErrors = Collections.unmodifiableList(validationErrors);
  }

  /**
   * Constructor taking an error message and the cause of the error.
   *
   * @param message the error message
   * @param cause the cause of the error
   * @deprecated Also supply the response message
   */
  @Deprecated(forRemoval = true)
  public ResponseValidationException(@Nonnull final String message, @Nullable final Throwable cause) {
    super(message, cause);
    this.validationErrors = null;
  }

  /**
   * Constructor taking an error message, the cause of the error and the response and assertion being processed.
   *
   * @param message the error message
   * @param cause the cause of the error
   * @param response the response being processed
   * @param assertion the assertion being processed
   */
  public ResponseValidationException(@Nonnull final String message, @Nullable final Throwable cause,
      @Nullable final Response response, @Nullable final Assertion assertion) {
    super(message, cause, response, assertion);
    this.validationErrors = null;
  }

  /**
   * Constructor taking a list of validation messages, the cause of the error and the response and assertion being
   * processed.
   *
   * @param validationErrors the validation errors
   * @param cause the cause of the error
   * @param response the response being processed
   * @param assertion the assertion being processed
   */
  public ResponseValidationException(@Nonnull final List<String> validationErrors, @Nullable final Throwable cause,
      @Nullable final Response response, @Nullable final Assertion assertion) {
    super(buildExceptionMessage(validationErrors), cause, response, assertion);
    this.validationErrors = validationErrors;
  }

  /**
   * Gets the list of validation errors (may be {@code null})
   *
   * @return a list of validation errors, or {@code null}
   */
  @Nullable
  public List<String> getValidationErrors() {
    return this.validationErrors;
  }

  private static String buildExceptionMessage(final List<String> validationErrors) {
    if (validationErrors == null || validationErrors.isEmpty()) {
      return "Validation error";
    }
    return String.join(" - ", validationErrors);
  }

}
