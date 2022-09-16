package ru.grobikon.templatewordtopdf.service;

import ru.grobikon.templatewordtopdf.dto.PdfResponseDto;
import ru.grobikon.templatewordtopdf.dto.TestTemplateRequestDto;

public interface PdfService {
    PdfResponseDto createTestTemplatePdf(TestTemplateRequestDto request);
}
