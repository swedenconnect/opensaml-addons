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
import org.opensaml.saml.ext.saml2mdui.Logo;

import se.swedenconnect.opensaml.OpenSAMLTestBase;

/**
 * Tests for {@code LogoBuilder}.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class LogoBuilderTest extends OpenSAMLTestBase {

  @Test
  public void testBuild() {
    final String url = "http://www.litsec.se/img.jpg";
    final Integer height = 100;
    final Integer width = 50;

    Logo logo = LogoBuilder.builder().url(url).height(height).width(width).build();

    Assertions.assertEquals(url, logo.getURI());
    Assertions.assertNull(logo.getXMLLang());
    Assertions.assertEquals(height, logo.getHeight());
    Assertions.assertEquals(width, logo.getWidth());

    logo = LogoBuilder.builder().url(url).language("sv").height(height).width(width).build();

    Assertions.assertEquals(url, logo.getURI());
    Assertions.assertEquals("sv", logo.getXMLLang());
    Assertions.assertEquals(height, logo.getHeight());
    Assertions.assertEquals(width, logo.getWidth());
  }

}
