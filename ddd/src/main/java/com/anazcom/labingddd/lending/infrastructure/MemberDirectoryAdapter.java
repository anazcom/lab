package com.anazcom.labingddd.lending.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.anazcom.labingddd.lending.domain.MemberBorrowingProfile;
import com.anazcom.labingddd.lending.domain.MemberDirectory;
import com.anazcom.labingddd.member.domain.MemberId;
import com.anazcom.labingddd.member.domain.MemberRepository;

@Component
class MemberDirectoryAdapter implements MemberDirectory {

  private final MemberRepository members;

  MemberDirectoryAdapter(MemberRepository members) {
    this.members = members;
  }

  @Override
  public Optional<MemberBorrowingProfile> findBorrowingProfile(MemberId memberId) {
    return members.findById(memberId).map(member -> new MemberBorrowingProfile(memberId, member.canBorrow()));
  }

}
