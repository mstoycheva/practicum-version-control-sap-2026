package com.example.documentmicroservice.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        String userId = request.getParameter("userId");
        String username = request.getParameter("username");

        redirectAttributes.addFlashAttribute("errorMessage", "File is too large! Maximum size is 10MB.");
        return "redirect:http://localhost:8080/document-microservice/documents?userId=" + userId + "&name=" + username + "&fileError=true";
    }
}