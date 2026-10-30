/*
 * Copyright 2016-2026 Sweden Connect
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
package se.swedenconnect.opensaml.common.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.assertion.ValidationContext;

/**
 * Test cases for {@link AbstractValidationParametersBuilder}.
 *
 * @author Martin Lindström
 */
class AbstractValidationParametersBuilderTest {

  @Test
  void testParameters() {
    final TestBuilder builder = new TestBuilder();
    builder.staticParameter("static", "s").dynamicParameter("dynamic1", "d1");
    builder.addDynamicParameter("dynamic2", "d2");

    final ValidationContext context = builder.build();
    Assertions.assertEquals("s", context.getStaticParameters().get("static"));
    Assertions.assertEquals("d1", context.getDynamicParameters().get("dynamic1"));
    Assertions.assertEquals("d2", context.getDynamicParameters().get("dynamic2"));
    Assertions.assertFalse(context.getStaticParameters().containsKey("dynamic1"));
    Assertions.assertFalse(context.getStaticParameters().containsKey("dynamic2"));
  }

  private static class TestBuilder extends AbstractValidationParametersBuilder<TestBuilder> {

    @Override
    protected TestBuilder getThis() {
      return this;
    }
  }

}
