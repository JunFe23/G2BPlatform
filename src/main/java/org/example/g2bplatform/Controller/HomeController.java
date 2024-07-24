package org.example.g2bplatform.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        return "main";
    }

    @PostMapping("/fetch")
    public String fetch(Model model) {
        // 여기서 필요한 로직을 수행합니다.
        model.addAttribute("message", "데이터를 가져왔습니다!");
        return "main";
    }
}
