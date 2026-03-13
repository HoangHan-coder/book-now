package vn.edu.fpt.booknow.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.services.admin.ReplyFeedbackService;

@Controller
@RequestMapping("/admin/feedback")
@RequiredArgsConstructor
public class ReplyFeedbackController {

    private final ReplyFeedbackService replyFeedbackService;

    /**
     * UC-14.4: Reply Feedback
     */
    @PostMapping("/reply")
    public String replyFeedback(@RequestParam Long feedbackId,
                                @RequestParam String replyContent,
                                RedirectAttributes redirectAttributes) {

        try {

            // A1 – Empty Reply Content
            if (replyContent == null || replyContent.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error",
                        "Reply content must not be empty.");
                return "redirect:/admin/feedback/detail/" + feedbackId;
            }

            // Main Flow
            replyFeedbackService.saveReply(feedbackId, replyContent);

            redirectAttributes.addFlashAttribute("success",
                    "Reply submitted successfully.");

        } catch (Exception e) {

            // E1 – Reply submission failure
            redirectAttributes.addFlashAttribute("error",
                    "Failed to submit reply.");
        }

        return "redirect:/admin/feedback/detail/" + feedbackId;
    }
}