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
package se.swedenconnect.opensaml.common.validation;

import org.opensaml.saml.common.assertion.ValidationContext;
import se.swedenconnect.opensaml.common.LibraryVersion;

import java.io.Serial;

/**
 * Generic exception class for validator errors.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ValidatorException extends Exception {

  /** For serializing. */
  @Serial
  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /**
   * Constructor that initializes based on the supplied {@link ValidationContext}.
   *
   * @param context validation context
   */
  public ValidatorException(final ValidationContext context) {
    super(String.join(" - ", context.getValidationFailureMessages()));
  }

  /**
   * Constructor accepting an error message.
   *
   * @param message the error message
   */
  public ValidatorException(final String message) {
    super(message);
  }

  /**
   * Constructor accepting an error message and the cause of the error.
   *
   * @param message the error message
   * @param cause the cause of the error
   */
  public ValidatorException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
