package org.example.g2bplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Page Views", description = "HTML 페이지 뷰 관련 컨트롤러")
@Controller
public class PageController {
    @Operation(summary = "홈 페이지", description = "애플리케이션의 홈 페이지를 반환합니다.")
    @GetMapping("/")
    public String home(Model model) {
        return "page1";
    }

    @Operation(summary = "설정 페이지", description = "설정 페이지를 반환합니다.")
    @GetMapping(value = "/settings")
    private String goToSettings(){
        return "settings.html";
    }

    @Operation(summary = "Page 1", description = "첫 번째 예시 페이지를 반환합니다.")
    @GetMapping("/page1")
    public String page1(Model model) {
        model.addAttribute("pageTitle", "Page 1");
        return "page1"; // templates/page1.html을 렌더링
    }

    @Operation(summary = "Page 2", description = "두 번째 예시 페이지를 반환합니다.")
    @GetMapping("/page2")
    public String page2(Model model) {
        model.addAttribute("pageTitle", "Page 2");
        return "page2"; // templates/page2.html을 렌더링
    }

    @Operation(summary = "Page 3", description = "세 번째 예시 페이지를 반환합니다.")
    @GetMapping("/page3")
    public String page3(Model model) {
        model.addAttribute("pageTitle", "Page 3");
        return "page3"; // templates/page3.html을 렌더링
    }

    @Operation(summary = "Page 4", description = "네 번째 예시 페이지를 반환합니다.")
    @GetMapping("/page4")
    public String page4(Model model) {
        model.addAttribute("pageTitle", "Page 4");
        return "page4"; // templates/page4.html을 렌더링
    }

    @Operation(summary = "Page 5", description = "다섯 번째 예시 페이지를 반환합니다.")
    @GetMapping("/page5")
    public String page5(Model model) {
        model.addAttribute("pageTitle", "Page 5");
        return "page5"; // templates/page5.html을 렌더링
    }

    @Operation(summary = "Page 6", description = "여섯 번째 예시 페이지를 반환합니다.")
    @GetMapping("/page6")
    public String page6(Model model) {
        model.addAttribute("pageTitle", "Page 6");
        return "page6"; // templates/page6.html을 렌더링
    }
}
