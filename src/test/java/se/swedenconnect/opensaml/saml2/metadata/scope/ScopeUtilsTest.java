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
package se.swedenconnect.opensaml.saml2.metadata.scope;

import org.junit.Assert;
import org.junit.Test;

import se.swedenconnect.opensaml.OpenSAMLTestBase;
import se.swedenconnect.opensaml.saml2.metadata.build.ScopeBuilder;

/**
 * Test cases for {@link ScopeUtils}.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public class ScopeUtilsTest extends OpenSAMLTestBase {

  @Test
  public void testNoRegexp() throws Exception {
    Assert.assertTrue(
      ScopeUtils.isMatch(ScopeBuilder.builder().value("example.com").build(), "kalle@example.com"));
    Assert.assertFalse(
      ScopeUtils.isMatch(ScopeBuilder.builder().value("example.com").build(), "example.com"));
    Assert.assertFalse(
      ScopeUtils.isMatch(ScopeBuilder.builder().value("example.com").build(), (String)null));
    Assert.assertFalse(
      ScopeUtils.isMatch(ScopeBuilder.builder().value("example.com").build(), ""));
    Assert.assertTrue(
      ScopeUtils.isMatch(ScopeBuilder.builder().value("example@.com").build(), "kalle@example@.com"));
  }
  
  @Test
  public void testRegexp() throws Exception {
    Assert.assertTrue(
      ScopeUtils.isMatch(ScopeBuilder.builder().regexp(true).value("^.*\\.com$").build(), "kalle@example.com"));
    Assert.assertFalse(
      ScopeUtils.isMatch(ScopeBuilder.builder().regexp(true).value("^.*\\.se$").build(), "kalle@example.com"));
  }

}
