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

import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;

import se.swedenconnect.opensaml.common.LibraryVersion;
import se.swedenconnect.opensaml.common.utils.SerializableOpenSamlObject;

/**
 * Exception that indicates a non-successful status code received in a Response message.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ResponseStatusErrorException extends Exception {

  /** For serializing. */
  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /** The response. */
  private final SerializableOpenSamlObject<Response> response;

  /**
   * Constructor.
   *
   * @param response the response
   */
  public ResponseStatusErrorException(final Response response) {
    super(statusToString(response.getStatus()));
    this.response = new SerializableOpenSamlObject<Response>(response);

    if (StatusCode.SUCCESS.equals(response.getStatus().getStatusCode().getValue())) {
      throw new IllegalArgumentException("Status is success - can not throw ResponseStatusErrorException");
    }
  }

  /**
   * Constructor taking the error status and the response ID.
   *
   * @param status status
   * @param responseId the response ID
   * @param issuer the issuer of the response
   */
  @Deprecated(forRemoval = true)
  public ResponseStatusErrorException(final Status status, final String responseId, final String issuer) {
    super(statusToString(status));

    try {
      final Response responseObj = (Response) XMLObjectSupport.buildXMLObject(Response.DEFAULT_ELEMENT_NAME);
      responseObj.setStatus(XMLObjectSupport.cloneXMLObject(status));
      responseObj.setID(responseId);
      final Issuer issuerObj = (Issuer) XMLObjectSupport.buildXMLObject(Issuer.DEFAULT_ELEMENT_NAME);
      issuerObj.setValue(issuer);
      responseObj.setIssuer(issuerObj);
      this.response = new SerializableOpenSamlObject<Response>(responseObj);
    }
    catch (final Exception e) {
      throw new RuntimeException(e);
    }

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
    return this.response.get().getStatus();
  }

  /**
   * Returns the ID of the Response.
   *
   * @return the response ID
   */
  public String getResponseId() {
    return this.response.get().getID();
  }

  /**
   * Gets the issuer of the response.
   *
   * @return the issuer entityID
   */
  public String getIssuer() {
    return Optional.ofNullable(this.response.get().getIssuer())
        .map(Issuer::getValue)
        .orElse(null);
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
