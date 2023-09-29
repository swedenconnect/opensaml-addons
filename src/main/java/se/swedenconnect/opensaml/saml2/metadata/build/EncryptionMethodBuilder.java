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
package se.swedenconnect.opensaml.saml2.metadata.build;

import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.metadata.EncryptionMethod;
import org.opensaml.xmlsec.encryption.KeySize;
import org.opensaml.xmlsec.encryption.OAEPparams;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;

/**
 * A builder for {@code md:EncryptionMethod} elements.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class EncryptionMethodBuilder extends AbstractSAMLObjectBuilder<EncryptionMethod> {

  /**
   * Creates a new {@code EncryptionMethodBuilder} instance.
   *
   * @return a EncryptionMethodBuilder instance
   */
  public static EncryptionMethodBuilder builder() {
    return new EncryptionMethodBuilder();
  }

  /**
   * Utility method that creates a {@code EncryptionMethod} element having only its {@code Algorithm} attribute
   * assigned.
   *
   * @param algorithm the algorithm
   * @return a EncryptionMethod instance
   */
  public static EncryptionMethod encryptionMethod(final String algorithm) {
    return builder().algorithm(algorithm).build();
  }

  /**
   * Utility method that creates a {@code EncryptionMethod} element.
   *
   * @param algorithm the algorithm
   * @param keySize key size (in bits)
   * @return a EncryptionMethod instance
   */
  public static EncryptionMethod encryptionMethod(final String algorithm, final Integer keySize) {
    return builder()
        .algorithm(algorithm)
        .keySize(keySize)
        .build();
  }

  /** {@inheritDoc} */
  @Override
  protected Class<EncryptionMethod> getObjectType() {
    return EncryptionMethod.class;
  }

  /**
   * Assigns the algorithm.
   *
   * @param algorithm the algorithm
   * @return the builder
   */
  public EncryptionMethodBuilder algorithm(final String algorithm) {
    this.object().setAlgorithm(algorithm);
    return this;
  }

  /**
   * Assigns the size in bits for the key.
   *
   * @param keySize key size
   * @return the builder
   */
  public EncryptionMethodBuilder keySize(final Integer keySize) {
    KeySize size = (KeySize) XMLObjectSupport.buildXMLObject(KeySize.DEFAULT_ELEMENT_NAME);
    this.object().setKeySize(size);
    return this;
  }

  /**
   * Assigns the OAEP parameters.
   *
   * @param base64Encoding the parameters in base64
   * @return the builder
   */
  public EncryptionMethodBuilder oAEPparams(final String base64Encoding) {
    OAEPparams p = (OAEPparams) XMLObjectSupport.buildXMLObject(OAEPparams.DEFAULT_ELEMENT_NAME);
    this.object().setOAEPparams(p);
    return this;
  }

}
