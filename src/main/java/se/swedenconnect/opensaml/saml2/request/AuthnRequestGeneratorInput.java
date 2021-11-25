/*
 * Copyright 2016-2021 Sweden Connect
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

import java.util.List;

/**
 * Extends the {@link RequestGeneratorInput} interface with input that are specific for building an
 * {@code AuthnRequest}.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public interface AuthnRequestGeneratorInput extends RequestGeneratorInput {

  /**
   * Enumeration that tells whether HoK is required, optional or not active.
   */
  public enum HokRequirement {
    REQUIRED, IF_AVAILABLE, DONT_USE
  };

  /**
   * Gets the requested authentication context class ref URI:s.
   * 
   * @return a list of URI:s, or null/empty list if no specific requirement is set
   */
  List<String> getRequestedAuthnContextClassRefUris();

  /**
   * Gets the SP requirement for using the Holder-of-key profile.
   * 
   * @return a HoK requirement
   */
  HokRequirement getHokRequirement();

}
