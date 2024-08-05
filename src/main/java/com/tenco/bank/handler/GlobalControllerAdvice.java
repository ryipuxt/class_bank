package com.tenco.bank.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalControllerAdvice {

	@ExceptionHandler(value = Exception.class)
	@ResponseBody // 데이터를 반환
	public ResponseEntity<Object> handleResourceNotFoundException(Exception e) {
		System.out.println("GlobalControllerAdvice: 요류 확인 : ");
		return new ResponseEntity<>("error", HttpStatus.NOT_FOUND);
	}

}
