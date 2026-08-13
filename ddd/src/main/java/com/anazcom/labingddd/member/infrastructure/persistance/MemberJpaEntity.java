package com.anazcom.labingddd.member.infrastructure.persistance;

import com.anazcom.labingddd.member.domain.MembershipStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
class MemberJpaEntity {
  @Id
  String id;
  private String name;
  private String email;
  @Enumerated(EnumType.STRING)
  private MembershipStatus status;

  public MemberJpaEntity() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public MembershipStatus getStatus() {
    return status;
  }

  public void setStatus(MembershipStatus status) {
    this.status = status;
  }
}
