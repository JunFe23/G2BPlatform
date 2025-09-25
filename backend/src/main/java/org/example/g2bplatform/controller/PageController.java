package org.example.g2bplatform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/")
    public String home(Model model) {
        return "page1";
    }

    @GetMapping(value = "/settings")
    private String goToSettings(){
        return "settings.html";
    }

    @GetMapping("/page1")
    public String page1(Model model) {
        model.addAttribute("pageTitle", "Page 1");
        return "page1"; // templates/page1.html을 렌더링
    }

    @GetMapping("/page2")
    public String page2(Model model) {
        model.addAttribute("pageTitle", "Page 2");
        return "page2"; // templates/page2.html을 렌더링
    }

    @GetMapping("/page3")
    public String page3(Model model) {
        model.addAttribute("pageTitle", "Page 3");
        return "page3"; // templates/page3.html을 렌더링
    }

    @GetMapping("/page4")
    public String page4(Model model) {
        model.addAttribute("pageTitle", "Page 4");
        return "page4"; // templates/page4.html을 렌더링
    }

    @GetMapping("/page5")
    public String page5(Model model) {
        model.addAttribute("pageTitle", "Page 5");
        return "page5"; // templates/page5.html을 렌더링
    }

    @GetMapping("/page6")
    public String page6(Model model) {
        model.addAttribute("pageTitle", "Page 6");
        return "page6"; // templates/page6.html을 렌더링
    }
}
