package com.ssafy.vibe.snapshot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ssafy.vibe.snapshot.domain.SnapshotEntity;

@Repository
public interface SnapshotRepository extends JpaRepository<SnapshotEntity, Long> {
	Optional<SnapshotEntity> findById(Long snapshotId);
}
