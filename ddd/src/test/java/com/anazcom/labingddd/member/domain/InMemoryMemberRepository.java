package com.anazcom.labingddd.member.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryMemberRepository implements MemberRepository {
  private final Map<MemberId, Member> members = new HashMap<>();

  @Override
  public Optional<Member> findById(MemberId id) {
    return Optional.ofNullable(this.members.get(id));
  }

  @Override
  public void save(Member member) {
    this.members.put(member.getId(), member);
  }
}
