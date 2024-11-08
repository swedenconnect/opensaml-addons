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
package se.swedenconnect.opensaml.saml2.metadata.build;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.XMLObject;

import se.swedenconnect.opensaml.OpenSAMLTestBase;
import se.swedenconnect.opensaml.saml2.metadata.scope.Scope;

/**
 * Test cases for {@link ScopeBuilder}.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ScopeBuilderTest extends OpenSAMLTestBase {

  @Test
  public void testBuild() throws Exception {

    final XMLObject object = ScopeBuilder.builder().regexp(false).value("example.com").build();
    Assertions.assertTrue(object instanceof Scope);
    final Scope scope = (Scope) object;
    Assertions.assertEquals(Boolean.FALSE, scope.getRegexp());
    Assertions.assertEquals("example.com", scope.getValue());
  }

}
