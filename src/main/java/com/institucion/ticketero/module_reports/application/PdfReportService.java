package com.institucion.ticketero.module_reports.application;

import com.institucion.ticketero.module_tickets.domain.Ticket;
import com.institucion.ticketero.module_workday.domain.Workday;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generateWorkdayReport(Workday workday, List<Ticket> tickets) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                // Title
                contentStream.beginText();
                contentStream.setFont(fontBold, 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Reporte de Jornada Laboral");
                contentStream.endText();

                // Subtitle
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(50, 730);
                contentStream.showText("Inicio: " + workday.getStartTime().format(FORMATTER));
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Fin: " + (workday.getEndTime() != null ? workday.getEndTime().format(FORMATTER) : "EN CURSO"));
                contentStream.endText();

                // Table Header
                drawTableHeader(contentStream, fontBold);

                // Table Rows
                int y = 650;
                for (Ticket ticket : tickets) {
                    drawTicketRow(contentStream, font, y, ticket);
                    y -= 20; // Move to next row
                }
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    private void drawTableHeader(PDPageContentStream stream, PDType1Font font) throws IOException {
        stream.beginText();
        stream.setFont(font, 10);
        stream.newLineAtOffset(50, 680);
        stream.showText("N° Ticket");
        stream.newLineAtOffset(100, 0);
        stream.showText("Cliente (RUT)");
        stream.newLineAtOffset(100, 0);
        stream.showText("Estado");
        stream.newLineAtOffset(70, 0);
        stream.showText("Ejecutivo");
        stream.newLineAtOffset(100, 0);
        stream.showText("Inicio Atención");
        stream.newLineAtOffset(100, 0);
        stream.showText("Fin Atención");
        stream.endText();
    }

    private void drawTicketRow(PDPageContentStream stream, PDType1Font font, int y, Ticket ticket) throws IOException {
        stream.beginText();
        stream.setFont(font, 9);
        stream.newLineAtOffset(50, y);
        stream.showText(ticket.getTicketNumber() != null ? ticket.getTicketNumber() : "-");
        stream.newLineAtOffset(100, 0);
        stream.showText(ticket.getCustomerId() != null ? ticket.getCustomerId() : "-");
        stream.newLineAtOffset(100, 0);
        stream.showText(ticket.getStatus() != null ? ticket.getStatus().name() : "-");
        stream.newLineAtOffset(70, 0);
        stream.showText(ticket.getExecutive() != null ? ticket.getExecutive().getFullName() : "-");
        stream.newLineAtOffset(100, 0);
        stream.showText(ticket.getAttendedAt() != null ? ticket.getAttendedAt().format(FORMATTER) : "-");
        stream.newLineAtOffset(100, 0);
        stream.showText(ticket.getClosedAt() != null ? ticket.getClosedAt().format(FORMATTER) : "-");
        stream.endText();
    }
}
