package com.ssafy.vibe.common.util;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Aes256Util {
	@Value("${AES_ALGORITHM}")
	private String AES_ALGORITHM;

	@Value("${AES_ALGORITHM_KEY}")
	private String SECRET_KEY; // 32자

	// 1. 암호화
	public String encrypt(String plainText) {
		try {
			// 랜덤 IV 생성
			byte[] iv = new byte[16];
			new SecureRandom().nextBytes(iv);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			// 키 설정
			SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

			// Cipher 설정
			Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

			byte[] encrypted = cipher.doFinal(plainText.getBytes());

			// 🔁 IV + 암호문을 합쳐서 Base64 인코딩 (IV는 복호화 때 필요하니까 같이 보관)
			byte[] combined = new byte[iv.length + encrypted.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

			return Base64.getEncoder().encodeToString(combined);
		} catch (Exception e) {
			throw new RuntimeException("암호화 실패", e);
		}
	}

	// 2. 복호화
	public String decrypt(String cipherText) {
		try {
			byte[] combined = Base64.getDecoder().decode(cipherText);

			// IV + 암호문 분리
			byte[] iv = new byte[16];
			byte[] encrypted = new byte[combined.length - 16];
			System.arraycopy(combined, 0, iv, 0, 16);
			System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

			Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

			byte[] decrypted = cipher.doFinal(encrypted);
			return new String(decrypted);
		} catch (Exception e) {
			throw new RuntimeException("복호화 실패", e);
		}
	}
}

