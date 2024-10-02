package com.tenco.bank.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.UserRepository;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	@Autowired
	private final UserRepository userRepository;
	@Autowired
	private final PasswordEncoder passwordEncoder;

	// 초기 파라미터 가져오는 방법
	@Value("${file.upload-dir}")
	private String uploadDir;

	/**
	 * 회원 등록 서비스 기능 트랜잭션 처리
	 * 
	 * @param dto
	 */
	@Transactional // 트랜잭션 처리는 반드시 습관화
	public void createUser(SignUpDTO dto) {

		int result = 0;

		if (dto.getMFile() != null && !dto.getMFile().isEmpty()) {
			// 파일 업로드 로직 구현

			String[] fileNames = uploadFile(dto.getMFile());
			dto.setOriginFileName(fileNames[0]);
			dto.setUploadFileName(fileNames[1]);
		}

		try {

			// 코드 추가 부분
			// 회원가입 요청시 사용자가 던진 비밀번호 값을 암호화 처리 해야 함
			String hashPwd = passwordEncoder.encode(dto.getPassword());
			System.err.println(hashPwd);
			dto.setPassword(hashPwd);

			result = userRepository.insert(dto.toUser());

		} catch (DataAccessException e) {
			throw new DataDeliveryException("중복된 이름을 사용할 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new DataDeliveryException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
		}
		if (result != 1) {
			throw new DataDeliveryException("회원가입 실패", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public User readUser(SignInDTO dto) {
		// 지역변수 활용
		User userEntity = null;

		try {
			userEntity = userRepository.findByUsername(dto.getUsername());

		} catch (DataAccessException e) {
			throw new DataDeliveryException("잘못된 처리 입니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
		}

		if (userEntity == null) {
			throw new DataDeliveryException("존재하지 않는 아이디 입니다", HttpStatus.BAD_REQUEST);
		}

		boolean isPwMatched = passwordEncoder.matches(dto.getPassword(), userEntity.getPassword());
		if (isPwMatched == false) {
			throw new DataDeliveryException("비밀번호가 잘못되었습니다", HttpStatus.BAD_REQUEST);
		}

		return userEntity;
	}

	private String[] uploadFile(MultipartFile mFile) {

		if (mFile.getSize() > Define.MAX_FILE_SIZE) {
			throw new DataDeliveryException("파일 크기는 20MB 이상 클 수 없습니다.", HttpStatus.BAD_REQUEST);
		}

		// 코드 수정
		// getAbsolutePath() : 파일 시스템의 절대경로를 나타냄
		// 리눅스 또는 MacOS에 맞춰서 절대 경로를 생성시킬 수 있다
		// String saveDirectory = new File(uploadDir).getAbsolutePath();
		String saveDirectory = uploadDir;
		System.err.println("saveDirectory : " + saveDirectory);

		// 파일 이름 생성(중복 이름 예방)
		String uploadFileName = UUID.randomUUID() + "_" + mFile.getOriginalFilename();
		// 파일 전체경로 + 새로생성한 파일명
		String uploadPath = saveDirectory + File.separator + uploadFileName;
		System.err.println("-------------------------------");
		System.err.println(uploadPath);
		System.err.println("-------------------------------");
		File destination = new File(uploadPath);

		// 반드시 수행
		try {
			mFile.transferTo(destination);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			throw new DataDeliveryException("파일 업로드 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new String[] { mFile.getOriginalFilename(), uploadFileName };
	}

	/**
	 * username 사용자 존재 여부 조회
	 * 
	 * @param String username
	 * @return User, null
	 */
	public User searchUsername(String username) {
		return userRepository.findByUsername(username);
	}
}
