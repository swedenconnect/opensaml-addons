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
package se.swedenconnect.opensaml.saml2.response;

import org.opensaml.saml.common.assertion.ValidationContext;

/**
 * Interface for a SAML response processor.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public interface ResponseProcessor {

  /**
   * Processes a SAML response including signature validation and assertion decryption.
   *
   * @param samlResponse the base64 encoded SAML response
   * @param relayState the received relay state
   * @param input the processing input
   * @param validationContext optional validation context for controlling the validation and assertion validation
   *          process
   * @return a result
   * @throws ResponseStatusErrorException if the response indicates a non-successful {@code Status}
   * @throws ResponseProcessingException for other processing errors
   */
  ResponseProcessingResult processSamlResponse(final String samlResponse, final String relayState,
      final ResponseProcessingInput input, final ValidationContext validationContext)
      throws ResponseStatusErrorException, ResponseProcessingException;

}
