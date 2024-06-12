package dev.dltrx.chichat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChiChatController {

    private Map<String, Boolean> typingUsers = new ConcurrentHashMap<>();

    @Autowired
    private UserRepository userRepo;

    @GetMapping("")
    public String viewHomePage() {
        return "home";
    }

    @GetMapping("/chat")
    public String viewChat() {
        return "chat";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());

        return "signup_form";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> listUsers = userRepo.findAll();
        model.addAttribute("listUsers", listUsers);

        return "users";
    }

    @PostMapping("/process_register")
    public String processRegister(User user, Model model) {
        if (userRepo.findByEmail(user.getEmail()) != null) {
            // Email is already used, handle this case appropriately
            String errorCode = "Error";
            String quickMessage = "Email Already Registered!";
            String detailedMessage = "User with this email address is already exist. Please login or use a different email address to register.";
            model.addAttribute("errorCode", errorCode);
            model.addAttribute("quickMessage", quickMessage);
            model.addAttribute("detailedMessage", detailedMessage);
            return "error";
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userRepo.save(user);

        return "register_success";
    }

    @MessageMapping("chat.sendMessage")
    @SendTo("/topic/chat")
    public Message sendMessage(@Payload Message msg) {
        typingUsers.put(msg.getSender(), false); // User stops typing when message is sent
        return msg;
    }

    @MessageMapping("chat.addUser")
    @SendTo("/topic/chat")
    public Message addUser(@Payload Message msg, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", msg.getSender());
        return msg;
    }

    @MessageMapping("chat.typing")
    @SendTo("/topic/typing")
    public Map<String, Boolean> typing(@Payload TypingStatus typingStatus) {
        typingUsers.put(typingStatus.getSender(), typingStatus.isTyping());
        return typingUsers;
    }
}