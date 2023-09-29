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

import java.util.Arrays;
import java.util.List;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.metadata.Company;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.ContactPersonTypeEnumeration;
import org.opensaml.saml.saml2.metadata.EmailAddress;
import org.opensaml.saml.saml2.metadata.GivenName;
import org.opensaml.saml.saml2.metadata.SurName;
import org.opensaml.saml.saml2.metadata.TelephoneNumber;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;

/**
 * A builder for {@code ContactPerson} elements.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ContactPersonBuilder extends AbstractSAMLObjectBuilder<ContactPerson> {

  /**
   * Default constructor.
   */
  public ContactPersonBuilder() {
    super();
  }

  /**
   * Creates a builder from an object template.
   *
   * @param template the object template
   * @throws MarshallingException for marshalling errors
   * @throws UnmarshallingException for unmarshalling errors
   */
  public ContactPersonBuilder(final ContactPerson template) throws MarshallingException, UnmarshallingException {
    super(template);
  }

  /**
   * Creates a builder instance.
   *
   * @return a builder instance
   */
  public static ContactPersonBuilder builder() {
    return new ContactPersonBuilder();
  }

  /**
   * Creates a builder instance.
   *
   * @param template the object template
   * @return a builder instance
   * @throws MarshallingException for marshalling errors
   * @throws UnmarshallingException for unmarshalling errors
   */
  public static ContactPersonBuilder builder(final ContactPerson template)
      throws MarshallingException, UnmarshallingException {
    return new ContactPersonBuilder(template);
  }

  /**
   * Assigns the type of contact person.
   *
   * @param type the type
   * @return the builder
   */
  public ContactPersonBuilder type(final ContactPersonTypeEnumeration type) {
    this.object().setType(type);
    return this;
  }

  /**
   * Assigns the {@code Company} element.
   *
   * @param company the company
   * @return the builder
   */
  public ContactPersonBuilder company(final String company) {
    if (company != null) {
      final Company c = (Company) XMLObjectSupport.buildXMLObject(Company.DEFAULT_ELEMENT_NAME);
      c.setValue(company);
      this.object().setCompany(c);
    }
    return this;
  }

  /**
   * Assigns the {@code GivenName} element.
   *
   * @param givenName the name
   * @return the builder
   */
  public ContactPersonBuilder givenName(final String givenName) {
    if (givenName != null) {
      final GivenName gn = (GivenName) XMLObjectSupport.buildXMLObject(GivenName.DEFAULT_ELEMENT_NAME);
      gn.setValue(givenName);
      this.object().setGivenName(gn);
    }
    return this;
  }

  /**
   * Assigns the {@code SurName} element.
   *
   * @param surname the name
   * @return the builder
   */
  public ContactPersonBuilder surname(final String surname) {
    if (surname != null) {
      final SurName sn = (SurName) XMLObjectSupport.buildXMLObject(SurName.DEFAULT_ELEMENT_NAME);
      sn.setValue(surname);
      this.object().setSurName(sn);
    }
    return this;
  }

  /**
   * Assigns the {@code EmailAddress} elements.
   *
   * @param emailAddresses the email addresses
   * @return the builder
   */
  public ContactPersonBuilder emailAddresses(final List<String> emailAddresses) {
    if (emailAddresses != null) {
      for (final String e : emailAddresses) {
        final EmailAddress ea = (EmailAddress) XMLObjectSupport.buildXMLObject(EmailAddress.DEFAULT_ELEMENT_NAME);
        ea.setURI(e);
        this.object().getEmailAddresses().add(ea);
      }
    }
    return this;
  }

  /**
   * @see #emailAddresses(List)
   *
   * @param emailAddresses the email addresses
   * @return the builder
   */
  public ContactPersonBuilder emailAddresses(final String... emailAddresses) {
    return this.emailAddresses(emailAddresses != null ? Arrays.asList(emailAddresses) : null);
  }

  /**
   * Assigns the {@code TelephoneNumber} elements.
   *
   * @param telephoneNumbers the numbers to assign
   * @return the builder
   */
  public ContactPersonBuilder telephoneNumbers(final List<String> telephoneNumbers) {
    if (telephoneNumbers != null) {
      for (final String t : telephoneNumbers) {
        final TelephoneNumber tn =
            (TelephoneNumber) XMLObjectSupport.buildXMLObject(TelephoneNumber.DEFAULT_ELEMENT_NAME);
        tn.setValue(t);
        this.object().getTelephoneNumbers().add(tn);
      }
    }
    return this;
  }

  /**
   * @see #telephoneNumbers(List)
   *
   * @param telephoneNumbers the numbers to assign
   * @return the builder
   */
  public ContactPersonBuilder telephoneNumbers(final String... telephoneNumbers) {
    return this.telephoneNumbers(telephoneNumbers != null ? Arrays.asList(telephoneNumbers) : null);
  }

  /** {@inheritDoc} */
  @Override
  protected Class<ContactPerson> getObjectType() {
    return ContactPerson.class;
  }

}
