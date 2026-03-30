package vn.edu.fpt.booknow.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.booknow.model.entities.Feedback;
import vn.edu.fpt.booknow.services.FeedbackDetailService;

@Controller
@RequestMapping("/staff/feedback")
@RequiredArgsConstructor
public class FeedbackDetailController {

    private final FeedbackDetailService feedbackDetailService;

    // UC-14.2: View Feedback Detail
    @GetMapping("/detail/{feedbackId}")
    public String viewFeedbackDetail(@PathVariable Long feedbackId, Model model) {

        Feedback feedback = feedbackDetailService.getFeedbackDetail(feedbackId);

        // E2 – Feedback not found
        if (feedback == null) {
            return "error/feedback-not-found";
        }

        // BR2 – Read-only display
        model.addAttribute("feedback", feedback);

        return "private/Feedback_detail";
    }
}