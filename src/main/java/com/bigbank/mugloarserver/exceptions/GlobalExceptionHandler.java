package com.bigbank.mugloarserver.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A global exception handler for handling exceptions thrown by the application controllers.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(DuplicateGameResultException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDuplicateGameResultException(DuplicateGameResultException ex, Model model) {
        String message = messageSource.getMessage(ex.getLocalizedMessage(), null, LocaleContextHolder.getLocale());
        LOGGER.error("DuplicateGameResultException: {}", message, ex);
        model.addAttribute("errorMessage", message);
        return "error";
    }

    @ExceptionHandler(GameOverException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleGameOverException(GameOverException ex, Model model) {
        String message = messageSource.getMessage(ex.getCode(), null, LocaleContextHolder.getLocale());
        LOGGER.error("GameOverException: {}", message, ex);
        model.addAttribute("errorMessage", message);
        return "error";
    }

    @ExceptionHandler(MugloarException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleMugloarException(MugloarException ex, Model model) {
        String message = messageSource.getMessage(ex.getCode(), null, LocaleContextHolder.getLocale());
        LOGGER.error("MugloarException: {}", message, ex);
        model.addAttribute("errorMessage", message);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex, Model model) {
        String message = messageSource.getMessage("error.unexpected", null, LocaleContextHolder.getLocale());
        LOGGER.error("Unexpected error: {}", message, ex);
        model.addAttribute("errorMessage", message);
        return "error";
    }
}
