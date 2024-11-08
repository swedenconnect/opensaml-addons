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
package se.swedenconnect.opensaml.common.builder;

import org.opensaml.core.xml.XMLObject;

/**
 * Interface for a builder pattern according to:
 *
 * <pre>
 * EntityDescriptorBuilder builder = new EntityDescriptorBuilder();
 * EntityDescriptor ed = builder.entityID("http://eid.idsec.se").entityCategories(...)[...].build();
 * </pre>
 *
 * @author Martin Lindström (martin@idsec.se)
 *
 * @param <T> the type
 */
public interface SAMLObjectBuilder<T extends XMLObject> {

  /**
   * Builds the {@code XMLObject}.
   * <p>
   * If invoked several times the method <b>must</b> return the same object.
   * </p>
   *
   * @return the built object
   */
  T build();

}
