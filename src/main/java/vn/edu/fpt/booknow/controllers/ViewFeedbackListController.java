package vn.edu.fpt.booknow.controllers;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.booknow.model.dto.FeedbackListDTO;
import vn.edu.fpt.booknow.services.ViewFeedbackListService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/staff")
public class ViewFeedbackListController {

    private final ViewFeedbackListService viewFeedbackListService;

    public ViewFeedbackListController(ViewFeedbackListService viewFeedbackListService) {
        this.viewFeedbackListService = viewFeedbackListService;
    }

    // UC-14.1 View Feedback List
    @GetMapping("/feedback")
    public String viewFeedbackList(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Boolean hidden,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            Page<FeedbackListDTO> feedbackPage = viewFeedbackListService.getFeedbackList(rating, hidden, keyword, page,
                    size);

            model.addAttribute("feedbackList", feedbackPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", feedbackPage.getTotalPages());
            model.addAttribute("totalItems", feedbackPage.getTotalElements());

            model.addAttribute("rating", rating);
            model.addAttribute("hidden", hidden);
            model.addAttribute("keyword", keyword);

            return "private/Feedback_list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error-page";
        }
    }
}