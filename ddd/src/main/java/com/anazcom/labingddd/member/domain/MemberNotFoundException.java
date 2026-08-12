package com.anazcom.labingddd.member.domain;

public class MemberNotFoundException extends RuntimeException {

  public MemberNotFoundException(MemberId id) {
    super("Member not found: " + id.value());
  }
}
