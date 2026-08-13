package com.anazcom.labingddd.member.infrastructure.persistance;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.anazcom.labingddd.member.domain.Email;
import com.anazcom.labingddd.member.domain.Member;
import com.anazcom.labingddd.member.domain.MemberId;
import com.anazcom.labingddd.member.domain.MemberRepository;

@Repository
class MemberRepositoryAdapter implements MemberRepository {
  private final SpringDataMemberRepository members;

  public MemberRepositoryAdapter(SpringDataMemberRepository members) {
    this.members = members;
  }

  @Override
  public Optional<Member> findById(MemberId id) {
    return this.members.findById(id.value())
        .map(newMember -> new Member(id, newMember.getName(), new Email(newMember.getEmail())));
  }

  @Override
  public void save(Member member) {
    MemberJpaEntity memberEntity = new MemberJpaEntity();
    memberEntity.setId(member.getId().value());
    memberEntity.setName(member.getName());
    memberEntity.setEmail(member.getEmail().value());
    memberEntity.setStatus(member.getStatus());
    this.members.save(memberEntity);
  }
}