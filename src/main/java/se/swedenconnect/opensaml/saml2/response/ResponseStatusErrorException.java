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

import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;

/**
 * Exception that indicates a non-successful status code received in a Response message.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ResponseStatusErrorException extends Exception {

  /** For serializing. */
  private static final long serialVersionUID = -8050896611037764108L;

  /** The SAML Status. */
  private final Status status;

  /** The Response ID. */
  private final String responseId;

  /** The entityID of the issuer of the response. */
  private final String issuer;

  /**
   * Constructor taking the error status and the response ID.
   *
   * @param status status
   * @param responseId the response ID
   * @param issuer the issuer of the response
   */
  public ResponseStatusErrorException(final Status status, final String responseId, final String issuer) {
    super(statusToString(status));
    this.status = status;
    this.responseId = responseId;
    this.issuer = issuer;

    if (StatusCode.SUCCESS.equals(status.getStatusCode().getValue())) {
      throw new IllegalArgumentException("Status is success - can not throw ResponseStatusErrorException");
    }
  }

  /**
   * Returns the status object.
   *
   * @return the status object
   */
  public Status getStatus() {
    return this.status;
  }

  /**
   * Returns the ID of the Response.
   *
   * @return the response ID
   */
  public String getResponseId() {
    return this.responseId;
  }

  /**
   * Gets the issuer of the response.
   *
   * @return the issuer entityID
   */
  public String getIssuer() {
    return this.issuer;
  }

  /**
   * Returns a textual representation of the status.
   *
   * @param status the Status to print
   * @return a status string
   */
  public static String statusToString(final Status status) {
    StringBuffer sb = new StringBuffer("Status: ");
    sb.append(status.getStatusCode().getValue());
    if (status.getStatusCode().getStatusCode() != null) {
      sb.append(", ").append(status.getStatusCode().getStatusCode().getValue());
    }
    if (status.getStatusMessage() != null && status.getStatusMessage().getValue() != null) {
      sb.append(" - ").append(status.getStatusMessage().getValue());
    }
    return sb.toString();
  }

}
