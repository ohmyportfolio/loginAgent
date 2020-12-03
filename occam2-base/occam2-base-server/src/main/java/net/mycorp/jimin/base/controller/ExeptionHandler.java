package net.mycorp.jimin.base.controller;

import java.io.IOException;

import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
class GlobalExceptionHandler {

	@ExceptionHandler(ScriptException.class)
	public void handleScriptException(HttpServletRequest request, HttpServletResponse response, ScriptException ex) throws IOException {	    
	    if(ex.getCause() != null && ex.getCause().getMessage().startsWith("$"))
	    	response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getCause().getMessage());
	}
}