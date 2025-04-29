package com.ssafy.vibe.snapshot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.vibe.common.exception.ExceptionCode;
import com.ssafy.vibe.common.exception.NotFoundException;
import com.ssafy.vibe.snapshot.controller.request.CreateSnapshotRequest;
import com.ssafy.vibe.snapshot.controller.request.UpdateSnapshotRequest;
import com.ssafy.vibe.snapshot.controller.response.SnapshotResponse;
import com.ssafy.vibe.snapshot.domain.SnapshotEntity;
import com.ssafy.vibe.snapshot.repository.SnapshotRepository;
import com.ssafy.vibe.template.domain.TemplateEntity;
import com.ssafy.vibe.template.repository.TemplateRepository;
import com.ssafy.vibe.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional
@RequiredArgsConstructor
@Slf4j
@Service
public class SnapshotServiceImpl implements SnapshotService {

	private final SnapshotRepository snapshotRepository;
	private final TemplateRepository templateRepository;
	private final UserRepository userRepository;

	@Override
	public SnapshotResponse createSnapshot(Long userId, CreateSnapshotRequest request) {
		TemplateEntity template = templateRepository.findByIdAndActive(userId, request.templateId())
			.orElseThrow(() -> new NotFoundException(ExceptionCode.TEMPLATE_NOT_FOUND));

		SnapshotEntity snapshot = SnapshotEntity.createSnapshot(template, request.snapshotName(),
			request.snapshotType(),
			request.content());
		snapshotRepository.save(snapshot);

		return SnapshotResponse.from(snapshot);
	}

	@Override
	public SnapshotResponse updateSnapshot(Long userId, UpdateSnapshotRequest request) {
		SnapshotEntity snapshot = snapshotRepository.findByIdAndActive(userId, request.snapshotId())
			.orElseThrow(() -> new NotFoundException(ExceptionCode.SNAPSHOT_NOT_FOUND));
		snapshot.updateSnapshotName(request.snapshotName());
		snapshotRepository.save(snapshot);

		return SnapshotResponse.from(snapshot);
	}

	@Override
	public void deleteSnapshot(Long userId, Long snapshotId) {
		SnapshotEntity snapshot = snapshotRepository.findByIdAndActive(userId, snapshotId)
			.orElseThrow(() -> new NotFoundException(ExceptionCode.SNAPSHOT_NOT_FOUND));
		snapshot.setIsActive(false);
		snapshotRepository.save(snapshot);
	}

	@Override
	public SnapshotResponse getSnapshotDetail(Long userId, Long snapshotId) {
		SnapshotEntity snapshot = snapshotRepository.findByIdAndActive(userId, snapshotId)
			.orElseThrow(() -> new NotFoundException(ExceptionCode.SNAPSHOT_NOT_FOUND));

		return SnapshotResponse.from(snapshot);
	}
}
