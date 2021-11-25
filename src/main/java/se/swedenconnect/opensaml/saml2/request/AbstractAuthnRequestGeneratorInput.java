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
import java.util.Optional;

/**
 * Abstract base class for authentication request generator input.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public class AbstractAuthnRequestGeneratorInput extends AbstractRequestGeneratorInput implements AuthnRequestGeneratorInput {

  /** The requirement for Holder of key. */
  private HokRequirement hokRequirement;
  
  /** {@inheritDoc} */
  @Override
  public List<String> getRequestedAuthnContextClassRefUris() {
    // TODO
    return null;
  }
  
  /** {@inheritDoc} */
  @Override
  public HokRequirement getHokRequirement() {
    return Optional.ofNullable(this.hokRequirement).orElse(HokRequirement.DONT_USE);
  }

  /**
   * Assigns the requirement for using holder-of-key.
   * 
   * @param hokRequirement
   *          the requirement
   */
  public void setHokRequirement(final HokRequirement hokRequirement) {
    this.hokRequirement = hokRequirement;
  }

}
