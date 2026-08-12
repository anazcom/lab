package com.anazcom.labingddd.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.anazcom.labingddd.shared.domain.DomainValidationException;

class EmailTest {

  @Test
  void validEmail_shouldNotThrow() {
    String value = "anazcom@email.com";
    Email email = new Email(value);

    assertThat(email.value()).isEqualTo(value);
  }

  @Test
  void email_shouldNotBeEmpty() {
    assertThatThrownBy(() -> new Email("")).isInstanceOf(DomainValidationException.class);
  }

  @Test
  void email_shouldNotBeNull() {
    assertThatThrownBy(() -> new Email(null)).isInstanceOf(DomainValidationException.class);
  }
}
