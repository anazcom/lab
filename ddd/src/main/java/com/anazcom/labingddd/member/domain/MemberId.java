package com.anazcom.labingddd.member.domain;

import com.anazcom.labingddd.shared.domain.Ids;
import java.util.UUID;

public record MemberId(String value) {

  public MemberId {
    Ids.requireNonBlank(value, "MemberId");
  }

  public static MemberId generate() {
    return new MemberId(UUID.randomUUID().toString());
  }
}
