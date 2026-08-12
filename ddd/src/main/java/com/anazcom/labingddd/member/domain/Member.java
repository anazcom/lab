package com.anazcom.labingddd.member.domain;

import com.anazcom.labingddd.shared.domain.DomainValidationException;

public class Member {

  private final MemberId id;
  private String name;
  private Email email;
  private MembershipStatus status;

  public Member(MemberId id, String name, Email email) {
    this.id = id;
    this.status = MembershipStatus.ACTIVE;

    changeEmail(email);
    changeName(name);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Member other)) return false;
    return this.id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return this.id.hashCode();
  }

  public void changeName(String name) {
    if (name == null || name.isEmpty()) {
      throw new DomainValidationException("Name cannot be blank or null");
    }
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public void changeEmail(Email newEmail) {
    if (newEmail == null) {
      throw new DomainValidationException("Email cannot be null");
    }
    this.email = newEmail;
  }

  public Email getEmail() {
    return this.email;
  }

  public MemberId getId() {
    return id;
  }

  public void suspend() {
    this.status = MembershipStatus.SUSPENDED;
  }

  public void reactivate() {
    this.status = MembershipStatus.ACTIVE;
  }

  public MembershipStatus getStatus() {
    return this.status;
  }

  public boolean canBorrow() {
    return this.status == MembershipStatus.ACTIVE;
  }
}
