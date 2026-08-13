package com.anazcom.labingddd.member.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataMemberRepository extends JpaRepository<MemberJpaEntity, String> {
}