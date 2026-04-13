package com.example.authmicroservice.controllers;

import com.example.authmicroservice.dto.LoginRequest;
import com.example.authmicroservice.dto.UserDto;
import com.example.authmicroservice.models.User;
import com.example.authmicroservice.services.JwtService;
import com.example.authmicroservice.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String viewRegister(Model model) {
        model.addAttribute("user", new UserDto());
        return "register";
    }

    @PostMapping(value = "/register", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String register(@Valid @ModelAttribute("user") UserDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        boolean isUsernameAvailable = userService.isUsernameAvailable(userDto.getUsername());
        boolean isEmailAvailable = userService.isEmailAvailable(userDto.getEmail());
        boolean isPasswordValid = userService.isPasswordValid(userDto.getPassword());

        if (!isUsernameAvailable) {
            bindingResult.rejectValue("username", "error.user", "Username is already in use");
        }
        if (!isEmailAvailable) {
            bindingResult.rejectValue("email", "error.user", "Email is already in use");
        }

        if(!isPasswordValid) {
            bindingResult.rejectValue("password", "error.password", "Use letters (A-z) and numbers " +
                    "(min. 6 chars)");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.addUser(userDto);

        return "redirect:./";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest request, HttpServletResponse response) {
        User user = userService.findByUsername(request.getUsername());
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String token = jwtService.generateToken(user);
            Cookie jwtCookie = new Cookie("token", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(3600);
            response.addCookie(jwtCookie);

            return "redirect:http://localhost:8080/document-microservice/documents?userId="
                    + user.getId()
                    + "&name=" + user.getUsername();
        } else {
            return "redirect:./?error";
        }
    }
}