package vn.edu.fpt.booknow.exceptions;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handle404(ResourceNotFoundException ex, Model model) {

        model.addAttribute("message", ex.getMessage());

        return "error/404";
    }

    @ExceptionHandler(ForbiddenException.class)
    public String handle403(ForbiddenException ex, Model model) {

        model.addAttribute("message", ex.getMessage());

        return "error/403";
    }

    @ExceptionHandler(UnauthorizedException.class)
    public String handle401(UnauthorizedException ex, Model model) {

        model.addAttribute("message", ex.getMessage());

        return "error/401";
    }

    @ExceptionHandler(Exception.class)
    public String handle500(Exception ex, Model model) {

        model.addAttribute("message", ex.getMessage());

        return "error/500";

    }
    // bắt lỗi logic
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntime(RuntimeException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error-page";
    }

    // bắt lỗi URL không tồn tại
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handle404(Model model) {
        model.addAttribute("errorMessage", "Trang không tồn tại");
        return "error-page";
    }
}