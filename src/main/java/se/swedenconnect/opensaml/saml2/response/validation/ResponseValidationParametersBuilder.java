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
package se.swedenconnect.opensaml.saml2.response.validation;

import org.opensaml.saml.common.assertion.ValidationContext;

/**
 * Builder class for building the {@link ValidationContext} object for use as validation input to the
 * {@link ResponseValidator}.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ResponseValidationParametersBuilder
    extends AbstractResponseValidationParametersBuilder<ResponseValidationParametersBuilder> {

  /**
   * Utility method that returns a builder instance.
   *
   * @return a builder
   */
  public static ResponseValidationParametersBuilder builder() {
    return new ResponseValidationParametersBuilder();
  }

  /** {@inheritDoc} */
  @Override
  protected ResponseValidationParametersBuilder getThis() {
    return this;
  }

}
