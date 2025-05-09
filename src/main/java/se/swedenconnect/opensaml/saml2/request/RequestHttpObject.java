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
package se.swedenconnect.opensaml.saml2.request;

import org.opensaml.saml.saml2.core.RequestAbstractType;

import java.util.Map;

/**
 * Defines an interface that represents an object that holds data necessary for the SP application to transmit a request
 * message to an IdP.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public interface RequestHttpObject<T extends RequestAbstractType> {

  /**
   * Returns the complete URL that the SP application should use when the user agent is sent to the Identity Provider.
   * <p>
   * For a redirect, this URL could look something like:
   * {@code https://www.theidp.com/auth?SAMLRequest=<encoded request>&RelayState=abcd}.
   * </p>
   * <p>
   * <b>Note:</b> Additional query parameters may be added to the URL by the using system.
   * </p>
   *
   * @return the URL to use when sending the user to the Identity Provider
   */
  String getSendUrl();

  /**
   * Returns the URL to where we are sending the request. If the method is "POST", this will be the same value as for
   * {@link #getSendUrl()}, and if the method is "GET", the value is just the destination and not the query parameters.
   *
   * @return the destination URL
   */
  String getDestinationUrl();

  /**
   * Returns the HTTP method that should be used to send the request, via the user agent, to the Identity Provider.
   * Possible values for this implementation is "GET" (redirect) and "POST".
   *
   * @return the HTTP method to use
   */
  String getMethod();

  /**
   * If the {@link #getMethod()} returns "POST" the request should be posted to the Identity Provider. The request
   * parameters are represented using a Map where the entries represent parameter names and values.
   * <p>
   * Note: for the "GET" method this method returns the query parameters that are part of {@link #getSendUrl()}.
   * </p>
   * <p>
   * The parameter values are not URL-encoded.
   * </p>
   *
   * @return a Map holding the request parameters (POST body or query parameters)
   */
  Map<String, String> getRequestParameters();

  /**
   * Returns a mapping of header names and values that should be used when sending the request.
   *
   * @return HTTP headers
   */
  Map<String, String> getHttpHeaders();

  /**
   * Returns the actual request (for easy access to its elements).
   *
   * @return the request
   */
  T getRequest();

}
