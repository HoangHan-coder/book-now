package vn.edu.fpt.booknow.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.services.FeedbackVisibilityService;

@Controller
@RequestMapping("/staff/feedback")
@RequiredArgsConstructor
public class FeedbackVisibilityController {

    private final FeedbackVisibilityService feedbackVisibilityService;

    /**
     * UC-14.3 Hide/Show Feedback
     */
    @PostMapping("/{id}/visibility")
    public String changeFeedbackVisibility(
            @PathVariable("id") Long feedbackId,
            @RequestParam("isHidden") Boolean isHidden,
            RedirectAttributes redirectAttributes) {

        try {

            feedbackVisibilityService.updateVisibility(feedbackId, isHidden);

            if (isHidden) {
                redirectAttributes.addFlashAttribute("message",
                        "Phản hồi đã được ẩn.");
            } else {
                redirectAttributes.addFlashAttribute("message",
                        "Phản hồi đã được hiển thị.");
            }

        } catch (Exception e) {

            // Exception Flow E1
            redirectAttributes.addFlashAttribute("error",
                    "Hệ thống không phản hồi vui lòng thử lại sau.");
        }

        return "redirect:/staff/feedback/detail/" + feedbackId;
    }
}