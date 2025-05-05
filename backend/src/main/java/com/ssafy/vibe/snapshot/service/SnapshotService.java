package com.ssafy.vibe.snapshot.service;

import com.ssafy.vibe.snapshot.controller.request.CreateSnapshotRequest;
import com.ssafy.vibe.snapshot.controller.request.UpdateSnapshotRequest;
import com.ssafy.vibe.snapshot.controller.response.SnapshotResponse;

public interface SnapshotService {
	SnapshotResponse createSnapshot(Long userId, CreateSnapshotRequest request);

	SnapshotResponse updateSnapshot(Long userId, UpdateSnapshotRequest request);

	void deleteSnapshot(Long userId, Long snapshotId);

	SnapshotResponse getSnapshotDetail(Long userId, Long snapshotId);
}
