package ru.grobikon.templatewordtopdf.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.grobikon.templatewordtopdf.dto.PdfResponseDto;
import ru.grobikon.templatewordtopdf.dto.TestTemplateRequestDto;
import ru.grobikon.templatewordtopdf.service.PdfService;

@RestController
@RequestMapping("/v1")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/test-template-pdf")
    public PdfResponseDto createTestTemplatePdf(@RequestBody TestTemplateRequestDto request) {
        return  pdfService.createTestTemplatePdf(request);
    }
}
