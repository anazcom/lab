package com.anazcom.labingddd.lending.domain;

import com.anazcom.labingddd.member.domain.MemberId;

public record MemberBorrowingProfile(MemberId memberId, boolean canBorrow) {
}
