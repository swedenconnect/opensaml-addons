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
package se.swedenconnect.opensaml.common.utils;

import net.shibboleth.shared.xml.SerializeSupport;
import net.shibboleth.shared.xml.XMLParserException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import se.swedenconnect.opensaml.common.LibraryVersion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Utility class for storing OpenSAML objects in a serializable manner.
 *
 * @param <T> the type of object being stored
 * @author Martin Lindström
 */
public class SerializableOpenSamlObject<T extends XMLObject> implements Serializable {

  @Serial
  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /** The object that we wrap. */
  private T object;

  /**
   * Constructor.
   *
   * @param object the object
   */
  public SerializableOpenSamlObject(final T object) {
    this.object = Objects.requireNonNull(object, "OpenSAML object to serialize must not be null");
  }

  /**
   * Gets the OpenSAML object.
   *
   * @return the OpenSAML object
   */
  public T get() {
    return this.object;
  }

  @Serial
  private void writeObject(final ObjectOutputStream out) throws IOException {
    try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      SerializeSupport.writeNode(XMLObjectSupport.marshall(this.object), bos);
      out.writeObject(bos.toByteArray());
    }
    catch (final MarshallingException e) {
      throw new IOException("Failed to marshall OpenSAML object", e);
    }
  }

  @Serial
  private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    final byte[] bytes = (byte[]) in.readObject();
    try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
      this.object =
          (T) XMLObjectSupport.unmarshallFromInputStream(XMLObjectProviderRegistrySupport.getParserPool(), bis);
    }
    catch (final UnmarshallingException | XMLParserException e) {
      throw new IOException("Could not unmarshall OpenSAML object", e);
    }
  }

}
