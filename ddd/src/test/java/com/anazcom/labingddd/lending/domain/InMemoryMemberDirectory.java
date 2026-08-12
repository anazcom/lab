package com.anazcom.labingddd.lending.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.anazcom.labingddd.member.domain.MemberId;

public class InMemoryMemberDirectory implements MemberDirectory {
  private final Map<MemberId, MemberBorrowingProfile> profiles = new HashMap<>();

  @Override
  public Optional<MemberBorrowingProfile> findBorrowingProfile(MemberId memberId) {
    return Optional.ofNullable(this.profiles.get(memberId));
  }

  public void add(MemberId memberId, boolean canBorrow) {
    this.profiles.put(memberId, new MemberBorrowingProfile(memberId, canBorrow));
  }
}
