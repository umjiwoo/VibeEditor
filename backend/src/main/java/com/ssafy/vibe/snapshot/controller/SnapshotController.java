package com.ssafy.vibe.snapshot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.vibe.auth.domain.UserPrincipal;
import com.ssafy.vibe.common.schema.BaseResponse;
import com.ssafy.vibe.snapshot.controller.request.CreateSnapshotRequest;
import com.ssafy.vibe.snapshot.controller.request.UpdateSnapshotRequest;
import com.ssafy.vibe.snapshot.controller.response.SnapshotResponse;
import com.ssafy.vibe.snapshot.service.SnapshotServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/snapshot")
public class SnapshotController {

	private final SnapshotServiceImpl snapshotService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<?> createSnapshot(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@Valid @RequestBody CreateSnapshotRequest request) {
		SnapshotResponse response = snapshotService.createSnapshot(userPrincipal.getUserId(), request);
		return ResponseEntity.ok(BaseResponse.success(response));
	}

	@PutMapping
	public ResponseEntity<?> updateSnapshot(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@Valid @RequestBody UpdateSnapshotRequest request) {
		SnapshotResponse response = snapshotService.updateSnapshot(userPrincipal.getUserId(), request);
		return ResponseEntity.ok(BaseResponse.success(response));
	}

	@DeleteMapping("/{snapshotId}")
	public ResponseEntity<?> deleteTemplate(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable("snapshotId") Long snapshotId) {
		snapshotService.deleteSnapshot(userPrincipal.getUserId(), snapshotId);
		return ResponseEntity.ok(BaseResponse.success(null));
	}

	@GetMapping("/{snapshotId}")
	public ResponseEntity<?> getSnapshotDetail(
		@AuthenticationPrincipal UserPrincipal userPrincipal,
		@PathVariable("snapshotId") Long snapshotId) {
		SnapshotResponse response = snapshotService.getSnapshotDetail(userPrincipal.getUserId(), snapshotId);
		return ResponseEntity.ok(BaseResponse.success(response));
	}
}
