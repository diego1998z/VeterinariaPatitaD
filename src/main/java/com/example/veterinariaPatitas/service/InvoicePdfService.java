package com.example.veterinariaPatitas.service;

import com.example.veterinariaPatitas.dto.InvoiceDownloadRequest;
import com.example.veterinariaPatitas.dto.InvoiceItemRequest;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class InvoicePdfService {

    public byte[] generateInvoicePdf(InvoiceDownloadRequest request, String username) {
        // El PDF se construye en memoria para retornarlo directamente en HTTP.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("Clinica Veterinaria PATITAS", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("RUC: 20512345678 | Villa El Salvador, Lima", normalFont);
            subtitle.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph(" "));

            String customer = (username == null || username.isBlank()) ? "Cliente General" : username.trim();
            document.add(new Paragraph("Factura de: " + customer, sectionFont));
            document.add(new Paragraph("Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")), normalFont));
            document.add(new Paragraph("Boleta de venta: " + generateInvoiceNumber(), normalFont));
            document.add(new Paragraph(" "));

            // Tabla de detalle: cantidad, descripcion, precio unitario y total por item.
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 5.2f, 2.0f, 2.0f});

            addHeaderCell(table, "Cant.");
            addHeaderCell(table, "Descripcion");
            addHeaderCell(table, "P. Unit.");
            addHeaderCell(table, "Total");

            List<InvoiceItemRequest> items = request.getItems();
            if (items != null) {
                for (InvoiceItemRequest item : items) {
                    // Normaliza valores para evitar nulls o negativos en el PDF.
                    int qty = safeInt(item.getQuantity());
                    double price = safeDouble(item.getPrice());
                    double lineTotal = qty * price;

                    table.addCell(bodyCell(String.valueOf(qty)));
                    table.addCell(bodyCell(safeText(item.getName())));
                    table.addCell(bodyCell(formatMoney(price)));
                    table.addCell(bodyCell(formatMoney(lineTotal)));
                }
            }

            document.add(table);
            document.add(new Paragraph(" "));

            double subtotal = safeDouble(request.getSubtotal());
            double tax = safeDouble(request.getTax());
            double total = safeDouble(request.getTotal());

            // Resumen final de importes.
            document.add(new Paragraph("Subtotal: S/. " + formatMoney(subtotal), sectionFont));
            document.add(new Paragraph("IGV: S/. " + formatMoney(tax), sectionFont));
            document.add(new Paragraph("TOTAL: S/. " + formatMoney(total), titleFont));
            document.add(new Paragraph(" "));

            Paragraph thanks = new Paragraph("Gracias por su compra.", normalFont);
            thanks.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(thanks);

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException ex) {
            // Se propaga como RuntimeException para que el controlador responda error.
            throw new RuntimeException("No se pudo generar el PDF de boleta", ex);
        }
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private PdfPCell bodyCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setPadding(6);
        return cell;
    }

    private String generateInvoiceNumber() {
        // Correlativo simple para demo (timestamp modulo 1_000_000).
        long value = System.currentTimeMillis() % 1_000_000;
        return String.format("0001-%06d", value);
    }

    private String safeText(String value) {
        if (value == null) return "";
        return value.trim();
    }

    private int safeInt(Integer value) {
        if (value == null || value < 0) return 0;
        return value;
    }

    private double safeDouble(Double value) {
        if (value == null || !Double.isFinite(value)) return 0.0;
        return value;
    }

    private String formatMoney(double value) {
        // Formato peruano: miles con punto y decimales con coma.
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("es-PE"));
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);
        return formatter.format(value);
    }
}
