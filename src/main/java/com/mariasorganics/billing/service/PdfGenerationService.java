package com.mariasorganics.billing.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    public byte[] generatePdfFromHtml(String htmlContent, String baseUri) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
            
            builder.withHtmlContent(document.html(), baseUri);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
