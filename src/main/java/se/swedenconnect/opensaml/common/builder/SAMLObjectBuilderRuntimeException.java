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
package se.swedenconnect.opensaml.common.builder;

import se.swedenconnect.opensaml.common.LibraryVersion;

import java.io.Serial;

/**
 * Runtime exception class for errors when using builders.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class SAMLObjectBuilderRuntimeException extends RuntimeException {

  /** For serializing. */
  @Serial
  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /**
   * Constructor assigning the error message.
   *
   * @param message the error message
   */
  public SAMLObjectBuilderRuntimeException(final String message) {
    super(message);
  }

  /**
   * Constructor assigning the cause of the error
   *
   * @param cause the cause of the error
   */
  public SAMLObjectBuilderRuntimeException(final Throwable cause) {
    super(cause);
  }

  /**
   * Constructor assinging the error message and the cause of the error.
   *
   * @param message the error message
   * @param cause the cause of the error
   */
  public SAMLObjectBuilderRuntimeException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
