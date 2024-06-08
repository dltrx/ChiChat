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
        return "index_2";
    }

    @GetMapping("/chat")
    public String viewChat() {
        return "index";
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
    public String processRegister(User user) {
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