package com.anazcom.labingddd.lending.domain;

import com.anazcom.labingddd.member.domain.MemberId;
import com.anazcom.labingddd.shared.domain.DomainStateException;

public class MemberCannotBorrowException extends DomainStateException {

    public MemberCannotBorrowException(MemberId memberId) {
        super("Member cannot borrow books: " + memberId.value());
    }

}
