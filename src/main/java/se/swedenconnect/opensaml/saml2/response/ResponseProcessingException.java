/*
 * Copyright 2016-2024 Sweden Connect
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

import java.util.Optional;

import org.opensaml.saml.saml2.core.Response;

import se.swedenconnect.opensaml.common.LibraryVersion;
import se.swedenconnect.opensaml.common.utils.SerializableOpenSamlObject;

/**
 * Exception class for the SAML response processor.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ResponseProcessingException extends Exception {

  /** For serializing. */
  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /** The response. */
  private final SerializableOpenSamlObject<Response> response;

  /**
   * Constructor taking an error message.
   *
   * @param message the error message
   */
  public ResponseProcessingException(final String message) {
    this(message, null, null);
  }

  /**
   * Constructor taking an error message and the response.
   *
   * @param message the error message
   * @param response the response that was processed when the error was reported
   */
  public ResponseProcessingException(final String message, final Response response) {
    this(message, null, response);
  }

  /**
   * Constructor taking an error message and the cause of the error.
   *
   * @param message the error message
   * @param cause the cause of the error
   */
  public ResponseProcessingException(final String message, final Throwable cause) {
    this(message, cause, null);
  }

  /**
   * Constructor taking an error message, the cause of the error and the response.
   *
   * @param message the error message
   * @param cause the cause of the error
   * @param response the response that was processed when the error was reported
   */
  public ResponseProcessingException(final String message, final Throwable cause, final Response response) {
    super(message, cause);
    this.response = response != null ? new SerializableOpenSamlObject<Response>(response) : null;
  }

  /**
   * Gets the {@link Response} that was processed when the error was reported.
   *
   * @return the {@link Response} or {@code null} if no response was assigned
   */
  public Response getResponse() {
    return Optional.ofNullable(this.response)
        .map(SerializableOpenSamlObject::get)
        .orElse(null);
  }

}
