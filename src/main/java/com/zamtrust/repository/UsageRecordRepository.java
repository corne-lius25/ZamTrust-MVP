package com.zamtrust.repository;

import com.zamtrust.domain.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, Long> {

    Optional<UsageRecord> findByUserIdAndActionAndPeriodKey(
            Long userId, String action, String periodKey);

    Optional<UsageRecord> findByIpHashAndActionAndPeriodKey(
            String ipHash, String action, String periodKey);

    /**
     * Atomically increment the count if the row already exists.
     * Returns the number of rows updated (0 or 1).
     */
    @Modifying
    @Query("UPDATE UsageRecord r SET r.count = r.count + 1, r.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE r.userId = :userId AND r.action = :action AND r.periodKey = :periodKey")
    int incrementUserUsage(@Param("userId") Long userId,
                           @Param("action") String action,
                           @Param("periodKey") String periodKey);

    @Modifying
    @Query("UPDATE UsageRecord r SET r.count = r.count + 1, r.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE r.ipHash = :ipHash AND r.action = :action AND r.periodKey = :periodKey")
    int incrementAnonymousUsage(@Param("ipHash") String ipHash,
                                @Param("action") String action,
                                @Param("periodKey") String periodKey);
}
