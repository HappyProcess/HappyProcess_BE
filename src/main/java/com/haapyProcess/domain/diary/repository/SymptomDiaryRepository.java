package com.haapyProcess.domain.diary.repository;

import com.haapyProcess.domain.diary.entity.SymptomDiary;
import com.haapyProcess.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SymptomDiaryRepository extends JpaRepository<SymptomDiary, Long> {

    @Query("""
            SELECT DISTINCT d FROM SymptomDiary d
            LEFT JOIN FETCH d.items i
            LEFT JOIN FETCH i.condition
            WHERE d.member = :member AND d.entryDate = :entryDate
            """)
    Optional<SymptomDiary> findByMemberAndEntryDate(@Param("member") Member member,
                                                    @Param("entryDate") LocalDate entryDate);

    @Query("""
            SELECT DISTINCT d FROM SymptomDiary d
            LEFT JOIN FETCH d.items i
            LEFT JOIN FETCH i.condition
            WHERE d.member = :member AND d.entryDate BETWEEN :from AND :to
            ORDER BY d.entryDate DESC
            """)
    List<SymptomDiary> findByMemberAndEntryDateBetween(@Param("member") Member member,
                                                       @Param("from") LocalDate from,
                                                       @Param("to") LocalDate to);
}
