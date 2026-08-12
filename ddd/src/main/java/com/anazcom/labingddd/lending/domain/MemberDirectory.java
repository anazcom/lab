package com.anazcom.labingddd.lending.domain;

import java.util.Optional;

import com.anazcom.labingddd.member.domain.MemberId;

public interface MemberDirectory {
  Optional<MemberBorrowingProfile> findBorrowingProfile(MemberId memberId);
}
