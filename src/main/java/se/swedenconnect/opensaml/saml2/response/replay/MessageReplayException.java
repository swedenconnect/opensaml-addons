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
package se.swedenconnect.opensaml.saml2.response.replay;

import se.swedenconnect.opensaml.common.LibraryVersion;

import java.io.Serial;

/**
 * Exception class that indicates a message replay attack.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class MessageReplayException extends Exception {

  /** For serializing. */
  @Serial
  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /**
   * Constructor taking an error message.
   *
   * @param message the error message
   */
  public MessageReplayException(final String message) {
    super(message);
  }

}
